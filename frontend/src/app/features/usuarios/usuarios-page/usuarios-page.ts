import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import * as faceapi from 'face-api.js';
import { timeout } from 'rxjs';
import Swal from 'sweetalert2';
import { AsistenciaPerfil, AsistenciaService } from '../../../core/services/asistencia';
import { Profile, ProfilesService } from '../../../core/services/profiles';
import { CreateUserPayload, User, UsersService } from '../../../core/services/users';

@Component({
  selector: 'app-usuarios-page',
  imports: [CommonModule, FormsModule],
  templateUrl: './usuarios-page.html',
  styleUrl: './usuarios-page.css',
})
export class UsuariosPage implements OnInit, OnDestroy {
  users: User[] = [];
  profiles: Profile[] = [];

  modalOpen = false;
  faceModalOpen = false;

  saving = false;
  loadingUsers = false;
  errorMessage = '';
  actionStatus = '';

  editingUserId: number | null = null;
  selectedUser: User | null = null;

  @ViewChild('videoElement') videoRef!: ElementRef<HTMLVideoElement>;
  @ViewChild('canvasElement') canvasRef!: ElementRef<HTMLCanvasElement>;

  readonly modelPath = 'assets/models/face-api';
  modelsLoaded = false;
  cameraReady = false;

  registeredFaces: AsistenciaPerfil[] = [];
  private stream: MediaStream | null = null;
  private animationFrameId: number | null = null;

  form: CreateUserPayload = {
    nombre: '',
    usuario: '',
    clave: '',
    correo: '',
    idPerfil: 0,
  };

  constructor(
    private usersService: UsersService,
    private profilesService: ProfilesService,
    private asistenciaService: AsistenciaService,
    private cdr: ChangeDetectorRef
  ) {}

  get selectedUserHasFace() {
    if (!this.selectedUser) {
      return false;
    }

    return this.registeredFaces.some((face) => face.userId === this.selectedUser?.id);
  }

  ngOnInit() {
    this.loadUsers();

    this.profilesService.listProfiles().subscribe({
      next: (profiles) => {
        this.profiles = profiles;
        this.cdr.detectChanges();
      },
    });

    this.loadFaceProfiles();
    this.loadModels();
  }

  ngOnDestroy() {
    this.stopCamera();
    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId);
    }
  }

  loadUsers() {
    this.loadingUsers = true;
    this.usersService.list().pipe(timeout(10000)).subscribe({
      next: (users) => {
        this.users = users;
        this.loadingUsers = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loadingUsers = false;
        this.errorMessage = 'No se pudieron cargar los usuarios.';
        this.cdr.detectChanges();
      },
    });
  }

  openModal() {
    this.errorMessage = '';
    this.form = {
      nombre: '',
      usuario: '',
      clave: '',
      correo: '',
      idPerfil: 0,
    };
    this.editingUserId = null;
    this.modalOpen = true;
  }

  editUser(user: User) {
    this.editingUserId = user.id;
    this.form = {
      nombre: user.nombre,
      usuario: user.usuario,
      clave: '',
      correo: user.correo,
      idPerfil: user.perfilId ?? 0,
    };
    this.openModal();
  }

  closeModal() {
    this.modalOpen = false;
    this.editingUserId = null;
  }

  saveUser() {
    if (!this.form.nombre.trim() || !this.form.usuario.trim() || !this.form.correo.trim()) {
      this.errorMessage = 'Completa nombre, usuario y correo.';
      return;
    }

    if (!this.editingUserId && !this.form.clave.trim()) {
      this.errorMessage = 'La clave es obligatoria al registrar un usuario.';
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    const isEditing = this.editingUserId !== null;
    const payload = { ...this.form };
    const request$ = this.editingUserId
      ? this.usersService.update(this.editingUserId, payload)
      : this.usersService.create(payload);

    request$.subscribe({
      next: () => {
        this.loadUsers();
        this.closeModal();
        this.saving = false;
        void Swal.fire({
          icon: 'success',
          title: isEditing ? 'Usuario actualizado' : 'Usuario registrado',
          text: 'La informacion del usuario se guardo correctamente.',
          confirmButtonColor: '#7c3f97',
        });
      },
      error: (error) => {
        this.saving = false;
        this.errorMessage = error?.error?.message || 'No se pudo guardar el usuario.';
      },
    });
  }

  toggleUserStatus(user: User) {
    const nuevoEstado = user.estado === 1 ? 0 : 1;

    this.usersService
      .update(user.id, {
        nombre: user.nombre,
        usuario: user.usuario,
        clave: '',
        correo: user.correo,
        idPerfil: user.perfilId ?? 0,
        estado: nuevoEstado,
      })
      .subscribe(() => {
        this.loadUsers();
        void Swal.fire({
          icon: 'success',
          title: 'Estado actualizado',
          text: 'El estado del usuario cambio correctamente.',
          confirmButtonColor: '#7c3f97',
        });
      });
  }

  async deleteUser(user: User) {
    const result = await Swal.fire({
      icon: 'warning',
      title: `Eliminar a ${user.nombre}?`,
      text: 'Esta accion no se puede deshacer.',
      showCancelButton: true,
      confirmButtonText: 'Si, eliminar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#b42318',
    });

    if (!result.isConfirmed) return;

    this.usersService.delete(user.id).subscribe({
      next: () => {
        this.loadUsers();
        void Swal.fire({
          icon: 'success',
          title: 'Usuario eliminado',
          text: 'El usuario fue eliminado correctamente.',
          confirmButtonColor: '#7c3f97',
        });
      },
      error: () => (this.errorMessage = 'Error al eliminar usuario'),
    });
  }

  loadFaceProfiles() {
    this.asistenciaService.listProfiles().subscribe({
      next: (faces) => {
        this.registeredFaces = faces;
        this.cdr.detectChanges();
      },
    });
  }

  async loadModels() {
    try {
      await Promise.all([
        faceapi.nets.tinyFaceDetector.loadFromUri(this.modelPath),
        faceapi.nets.faceLandmark68Net.loadFromUri(this.modelPath),
        faceapi.nets.faceRecognitionNet.loadFromUri(this.modelPath),
      ]);
      this.modelsLoaded = true;
      this.cdr.detectChanges();
    } catch {
      this.actionStatus = 'Error cargando modelos';
      this.cdr.detectChanges();
    }
  }

  async startCamera() {
    try {
      this.stream = await navigator.mediaDevices.getUserMedia({
        video: true,
        audio: false,
      });

      const video = this.videoRef?.nativeElement;
      if (!video) {
        this.actionStatus = 'No se encontro el video';
        return;
      }

      video.srcObject = this.stream;
      await video.play();
      this.cameraReady = true;
      this.startDetectionLoop();
      this.cdr.detectChanges();
    } catch {
      this.actionStatus = 'Error al abrir camara';
      this.cdr.detectChanges();
      void Swal.fire({
        icon: 'error',
        title: 'No se pudo abrir la camara',
        text: 'Revisa permisos del navegador o disponibilidad del dispositivo.',
        confirmButtonColor: '#7c3f97',
      });
    }
  }

  stopCamera() {
    this.stream?.getTracks().forEach((t) => t.stop());
    this.stream = null;
    this.cameraReady = false;
    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId);
      this.animationFrameId = null;
    }
    this.cdr.detectChanges();
  }

  openFaceModal(user: User) {
    this.selectedUser = user;
    this.faceModalOpen = true;
    this.actionStatus = this.selectedUserHasFace
      ? 'Rostro ya registrado para este usuario'
      : 'Prepara el rostro dentro del recuadro y luego captura.';
    this.cdr.detectChanges();
    setTimeout(() => this.startCamera(), 300);
  }

  closeFaceModal() {
    this.faceModalOpen = false;
    this.stopCamera();
    this.cdr.detectChanges();
  }

  private startDetectionLoop() {
    const detect = async () => {
      if (!this.modelsLoaded || !this.cameraReady) {
        this.animationFrameId = requestAnimationFrame(detect);
        return;
      }

      const video = this.videoRef?.nativeElement;
      if (!video) return;

      const result = await faceapi
        .detectSingleFace(video, new faceapi.TinyFaceDetectorOptions())
        .withFaceLandmarks();

      this.drawDetection(result);
      this.animationFrameId = requestAnimationFrame(detect);
    };

    detect();
  }

  async captureDescriptor() {
    if (!this.modelsLoaded) {
      this.actionStatus = 'Modelos no cargados';
      this.cdr.detectChanges();
      return null;
    }

    const video = this.videoRef?.nativeElement;
    if (!video) {
      this.actionStatus = 'Camara no lista';
      this.cdr.detectChanges();
      return null;
    }

    const result = await faceapi
      .detectSingleFace(video, new faceapi.TinyFaceDetectorOptions())
      .withFaceLandmarks()
      .withFaceDescriptor();

    if (!result) {
      this.actionStatus = 'No se detecto rostro';
      this.cdr.detectChanges();
      return null;
    }

    return result;
  }

  private drawDetection(result: any) {
    const video = this.videoRef?.nativeElement;
    const canvas = this.canvasRef?.nativeElement;

    if (!video || !canvas) return;

    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;

    const displaySize = {
      width: canvas.width,
      height: canvas.height,
    };

    faceapi.matchDimensions(canvas, displaySize);
    const resized = result ? faceapi.resizeResults(result, displaySize) : null;
    const ctx = canvas.getContext('2d');
    ctx?.clearRect(0, 0, canvas.width, canvas.height);

    if (resized) {
      faceapi.draw.drawDetections(canvas, resized);
      faceapi.draw.drawFaceLandmarks(canvas, resized);
    }
  }

  async captureAndSaveFace() {
    if (this.selectedUserHasFace) {
      this.actionStatus = 'Rostro ya registrado para este usuario';
      void Swal.fire({
        icon: 'info',
        title: 'Rostro ya registrado',
        text: 'Si deseas reemplazarlo, primero elimina el registro actual.',
        confirmButtonColor: '#7c3f97',
      });
      return;
    }

    const result = await this.captureDescriptor();
    if (!result || !this.selectedUser) {
      if (!result) {
        void Swal.fire({
          icon: 'warning',
          title: 'No se detecto ningun rostro',
          text: 'Acercate a la camara y vuelve a intentarlo.',
          confirmButtonColor: '#7c3f97',
        });
      }
      return;
    }

    this.asistenciaService
      .saveProfile({
        userId: this.selectedUser.id,
        descriptor: Array.from(result.descriptor),
      })
      .subscribe({
        next: () => {
          this.actionStatus = 'Biometria guardada correctamente';
          this.loadFaceProfiles();
          void Swal.fire({
            icon: 'success',
            title: 'Rostro registrado',
            text: `La biometria de ${this.selectedUser?.nombre ?? 'usuario'} se guardo correctamente.`,
            confirmButtonColor: '#7c3f97',
          });
          this.closeFaceModal();
        },
        error: (error) => {
          this.actionStatus = error?.error?.message || 'Error al guardar biometria';
          void Swal.fire({
            icon: 'error',
            title: 'No se pudo registrar el rostro',
            text: this.actionStatus,
            confirmButtonColor: '#7c3f97',
          });
        },
      });
  }
}
