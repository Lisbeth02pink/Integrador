import { CommonModule } from '@angular/common';
import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import * as faceapi from 'face-api.js';
import {
  AsistenciaPerfil,
  AsistenciaRegistro,
  AsistenciaService,
} from '../../../core/services/asistencia';
import { EmployeeControl, EmployeesService } from '../../../core/services/employees';

@Component({
  selector: 'app-asistencia-page',
  imports: [CommonModule, FormsModule],
  templateUrl: './asistencia-page.html',
  styleUrl: './asistencia-page.css',
})
export class AsistenciaPage implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('videoElement') videoElement?: ElementRef<HTMLVideoElement>;
  @ViewChild('canvasElement') canvasElement?: ElementRef<HTMLCanvasElement>;

  readonly modelPath = 'assets/models/face-api';

  modelStatus = 'Cargando modelos...';
  cameraStatus = 'Inicializando camara...';
  actionStatus = 'Listo para reconocimiento facial';

  modelsLoaded = false;
  cameraReady = false;
  processing = false;

  registeredFaces: AsistenciaPerfil[] = [];
  attendanceLogs: AsistenciaRegistro[] = [];
  employeeControl: EmployeeControl[] = [];
  employeeError = '';

  private stream: MediaStream | null = null;
  private animationFrameId: any;
  private lastPreviewAt = 0;

  constructor(
    private asistenciaService: AsistenciaService,
    private employeesService: EmployeesService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadSavedData();
  }

  async ngAfterViewInit() {
    await this.loadModels();
    await this.startCamera();
    this.startDetectionLoop();
  }

  ngOnDestroy() {
    this.stopCamera();
    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId);
    }
  }

  private loadSavedData() {
    this.asistenciaService.listProfiles().subscribe({
      next: (profiles) => {
        this.registeredFaces = profiles;
        this.cdr.detectChanges();
      },
    });

    this.asistenciaService.listLogs().subscribe({
      next: (logs) => {
        this.attendanceLogs = logs;
        this.cdr.detectChanges();
      },
    });

    this.employeesService.list().subscribe({
      next: (employees) => {
        this.employeeControl = employees;
        this.cdr.detectChanges();
      },
      error: () => {
        this.employeeError = 'No se pudo cargar el control de empleados.';
        this.cdr.detectChanges();
      },
    });
  }

  private async loadModels() {
    try {
      await Promise.all([
        faceapi.nets.tinyFaceDetector.loadFromUri(this.modelPath),
        faceapi.nets.faceLandmark68Net.loadFromUri(this.modelPath),
        faceapi.nets.faceRecognitionNet.loadFromUri(this.modelPath),
      ]);
      this.modelsLoaded = true;
      this.modelStatus = 'Modelos cargados';
      this.cdr.detectChanges();
    } catch {
      this.modelStatus = 'Error cargando modelos';
      this.cdr.detectChanges();
    }
  }

  private async startCamera() {
    try {
      this.stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: false });
      const video = this.videoElement?.nativeElement;
      if (!video) return;
      video.srcObject = this.stream;
      await new Promise<void>((resolve) => {
        video.onloadedmetadata = () => { video.play(); resolve(); };
      });
      this.cameraReady = true;
      this.cameraStatus = 'Camara activa';
      this.cdr.detectChanges();
    } catch {
      this.cameraStatus = 'Error al abrir camara';
      this.cdr.detectChanges();
    }
  }

  private stopCamera() {
    this.stream?.getTracks().forEach((t) => t.stop());
    this.stream = null;
  }

  private startDetectionLoop() {
    const detect = async () => {
      if (!this.modelsLoaded || !this.cameraReady) {
        this.animationFrameId = requestAnimationFrame(detect);
        return;
      }
      const video = this.videoElement?.nativeElement;
      if (!video) return;

      const now = performance.now();
      if (now - this.lastPreviewAt < 280) {
        this.animationFrameId = requestAnimationFrame(detect);
        return;
      }
      this.lastPreviewAt = now;

      const result = await faceapi
        .detectSingleFace(video, new faceapi.TinyFaceDetectorOptions())
        .withFaceLandmarks();

      this.drawDetection(result);

      if (result) {
        this.actionStatus = 'Rostro detectado';
        this.cdr.detectChanges();
      }

      this.animationFrameId = requestAnimationFrame(detect);
    };
    detect();
  }

  async validateAttendance(tipo: 'ENTRADA' | 'SALIDA') {
    if (!this.registeredFaces.length) {
      this.actionStatus = 'No hay rostros registrados';
      return;
    }
    const result = await this.captureDescriptor();
    if (!result) return;

    const labeled = this.registeredFaces.map(
      (item) => new faceapi.LabeledFaceDescriptors(item.codigo, [new Float32Array(item.descriptor)])
    );
    const matcher = new faceapi.FaceMatcher(labeled, 0.5);
    const bestMatch = matcher.findBestMatch(result.descriptor);

    if (bestMatch.label === 'unknown') {
      this.actionStatus = 'Rostro no reconocido';
      return;
    }

    const matched = this.registeredFaces.find((item) => item.codigo === bestMatch.label);
    if (!matched?.userId) {
      this.actionStatus = 'El rostro no esta vinculado a un usuario valido';
      return;
    }

    this.asistenciaService
      .saveLog({ userId: matched.userId, coincidencia: Number((1 - bestMatch.distance).toFixed(4)), tipo })
      .subscribe({
        next: (entry) => {
          this.attendanceLogs = [entry, ...this.attendanceLogs].slice(0, 20);
          this.actionStatus = `${tipo === 'ENTRADA' ? 'Entrada' : 'Salida'} registrada para ${matched.nombre}`;
          this.reloadEmployeeControl();
          this.cdr.detectChanges();
        },
        error: (error) => {
          this.actionStatus = error?.error?.message || 'Error al registrar asistencia';
          this.cdr.detectChanges();
        },
      });
  }

  private reloadEmployeeControl() {
    this.employeesService.list().subscribe({
      next: (employees) => { this.employeeControl = employees; this.cdr.detectChanges(); },
    });
  }

  private async captureDescriptor() {
    if (!this.modelsLoaded || !this.cameraReady) return null;
    const video = this.videoElement?.nativeElement;
    if (!video) return null;
    return await faceapi
      .detectSingleFace(video, new faceapi.TinyFaceDetectorOptions())
      .withFaceLandmarks()
      .withFaceDescriptor();
  }

  private drawDetection(result: any) {
    const video = this.videoElement?.nativeElement;
    const canvas = this.canvasElement?.nativeElement;
    if (!video || !canvas) return;

    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    const displaySize = { width: canvas.width, height: canvas.height };
    faceapi.matchDimensions(canvas, displaySize);

    const resized = result ? faceapi.resizeResults(result, displaySize) : null;
    const ctx = canvas.getContext('2d');
    ctx?.clearRect(0, 0, canvas.width, canvas.height);
    if (resized) {
      faceapi.draw.drawDetections(canvas, resized);
      faceapi.draw.drawFaceLandmarks(canvas, resized);
    }
  }

  removeFace(record: AsistenciaPerfil) {
    this.asistenciaService.deleteProfile(record.id).subscribe({
      next: () => {
        this.registeredFaces = this.registeredFaces.filter((item) => item.id !== record.id);
        this.cdr.detectChanges();
      },
    });
  }

  // ─────────────────────────────────────────────
  // EXPORTAR PDF — genera un HTML estilizado y lo
  // abre en una ventana nueva lista para imprimir
  // ─────────────────────────────────────────────
  exportEmployeeReport() {
    const fecha = new Date().toLocaleDateString('es-PE', {
      day: '2-digit', month: 'long', year: 'numeric',
    });

    const estadoColor = (estado: string) => {
      if (estado === 'Presente') return '#188038';
      if (estado === 'Tarde')    return '#d17a00';
      if (estado === 'Falta')    return '#e02929';
      return '#555';
    };

    const estadoBg = (estado: string) => {
      if (estado === 'Presente') return '#e8f7ea';
      if (estado === 'Tarde')    return '#fff0d8';
      if (estado === 'Falta')    return '#ffe1e1';
      return '#f0f0f0';
    };

    const filas = this.employeeControl.map((e) => `
      <tr>
        <td>${e.nombre ?? '--'}</td>
        <td>${e.cargo ?? '--'}</td>
        <td>${e.entrada ?? '--'}</td>
        <td>${e.salida ?? '--'}</td>
        <td style="text-align:center">${e.tardanzas ?? 0}</td>
        <td style="text-align:center">${e.faltas ?? 0}</td>
        <td style="text-align:center">${e.asistencias ?? 0}</td>
        <td>
          <span style="
            display:inline-block;
            padding:4px 12px;
            border-radius:999px;
            font-size:11px;
            font-weight:700;
            background:${estadoBg(e.estado)};
            color:${estadoColor(e.estado)};
          ">${e.estado ?? '--'}</span>
        </td>
      </tr>
    `).join('');

    const html = `
      <!DOCTYPE html>
      <html lang="es">
      <head>
        <meta charset="UTF-8"/>
        <title>Reporte de Asistencia</title>
        <style>
          * { box-sizing: border-box; margin: 0; padding: 0; }
          body {
            font-family: 'Segoe UI', Arial, sans-serif;
            color: #241b35;
            padding: 40px;
            background: #fff;
          }
          .header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 32px;
            padding-bottom: 20px;
            border-bottom: 3px solid #8e1b8f;
          }
          .header h1 {
            font-size: 22px;
            font-weight: 900;
            color: #241b35;
          }
          .header .kicker {
            font-size: 11px;
            font-weight: 700;
            letter-spacing: 0.16em;
            text-transform: uppercase;
            color: #8e1b8f;
            margin-bottom: 6px;
          }
          .header .meta {
            font-size: 12px;
            color: #666;
            margin-top: 4px;
          }
          .logo {
            font-size: 28px;
            font-weight: 900;
            color: #8e1b8f;
            letter-spacing: -0.04em;
          }
          table {
            width: 100%;
            border-collapse: collapse;
            font-size: 13px;
          }
          thead th {
            padding: 12px 10px;
            text-align: left;
            background: #f5f0fb;
            color: #8e1b8f;
            font-size: 10px;
            font-weight: 700;
            letter-spacing: 0.1em;
            text-transform: uppercase;
            border-bottom: 2px solid #ddd4f0;
          }
          tbody td {
            padding: 14px 10px;
            border-bottom: 1px solid #f0ecf8;
            color: #241b35;
            font-weight: 500;
          }
          tbody tr:last-child td { border-bottom: none; }
          tbody tr:hover td { background: #faf7ff; }
          .footer {
            margin-top: 32px;
            font-size: 11px;
            color: #999;
            text-align: right;
          }
          @media print {
            body { padding: 20px; }
            button { display: none; }
          }
        </style>
      </head>
      <body>
        <div class="header">
          <div>
            <div class="kicker">Control de Empleados</div>
            <h1>Reporte de Asistencia, Tardanzas y Faltas</h1>
            <div class="meta">Generado el ${fecha} &nbsp;·&nbsp; ${this.employeeControl.length} empleado(s)</div>
          </div>
          <div class="logo">Tambo</div>
        </div>

        <table>
          <thead>
            <tr>
              <th>Empleado</th>
              <th>Cargo</th>
              <th>Entrada</th>
              <th>Salida</th>
              <th>Tardanzas</th>
              <th>Faltas</th>
              <th>Asistencias</th>
              <th>Estado</th>
            </tr>
          </thead>
          <tbody>${filas}</tbody>
        </table>

        <div class="footer">Tambo &copy; ${new Date().getFullYear()} &nbsp;·&nbsp; Reporte generado automáticamente</div>
      </body>
      </html>
    `;

    const win = window.open('', '_blank');
    if (!win) return;
    win.document.write(html);
    win.document.close();
    win.focus();
    // Pequeño delay para que el HTML cargue antes de imprimir
    setTimeout(() => { win.print(); }, 400);
  }

  // ─────────────────────────────────────────────
  // EXPORTAR EXCEL — CSV con BOM, separador punto
  // y coma (compatible con Excel latinoamérica),
  // cabeceras en mayúscula y fecha en el nombre
  // ─────────────────────────────────────────────
  exportEmployeeExcelReport() {
    const fechaHoy = new Date();
    const fechaStr = fechaHoy.toISOString().slice(0, 10);           // 2026-05-09
    const fechaLabel = fechaHoy.toLocaleDateString('es-PE', {
      day: '2-digit', month: '2-digit', year: 'numeric',
    });

    // Cabeceras
    const headers = [
      'EMPLEADO',
      'CARGO',
      'ENTRADA',
      'SALIDA',
      'TARDANZAS',
      'FALTAS',
      'ASISTENCIAS',
      'ESTADO',
    ];

    // Fila de título y metadata
    const meta = [
      [`REPORTE DE ASISTENCIA — ${fechaLabel}`],
      [`Total empleados: ${this.employeeControl.length}`],
      [], // fila vacía
      headers,
    ];

    const filas = this.employeeControl.map((e) => [
      e.nombre   ?? '',
      e.cargo    ?? '',
      e.entrada  ?? '--',
      e.salida   ?? '--',
      e.tardanzas  ?? 0,
      e.faltas     ?? 0,
      e.asistencias ?? 0,
      e.estado   ?? '',
    ]);

    // Serializar con punto y coma (Excel es-PE / es-ES)
    const escape = (val: any) =>
      `"${String(val ?? '').replace(/"/g, '""')}"`;

    const csv = [...meta, ...filas]
      .map((row) => row.map(escape).join(';'))
      .join('\r\n');

    // BOM UTF-8 para que Excel abra bien los caracteres
    const blob = new Blob(['\uFEFF' + csv], {
      type: 'text/csv;charset=utf-8;',
    });

    const url = URL.createObjectURL(blob);
    const a   = document.createElement('a');
    a.href     = url;
    a.download = `reporte-asistencia-${fechaStr}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  }
}