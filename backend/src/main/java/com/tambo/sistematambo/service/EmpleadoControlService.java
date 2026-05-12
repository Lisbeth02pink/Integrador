package com.tambo.sistematambo.service;

import com.tambo.sistematambo.model.AsistenciaRegistro;
import com.tambo.sistematambo.model.AsistenciaPerfil;
import com.tambo.sistematambo.model.User;
import com.tambo.sistematambo.repository.AsistenciaPerfilRepository;
import com.tambo.sistematambo.repository.AsistenciaRegistroRepository;
import com.tambo.sistematambo.repository.UserRepository;
import com.tambo.sistematambo.response.EmpleadoControlResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class EmpleadoControlService {

    private static final ZoneId LIMA_ZONE = ZoneId.of("America/Lima");
    private static final LocalTime HORA_TARDANZA = LocalTime.of(8, 15);
    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final UserRepository userRepository;
    private final AsistenciaPerfilRepository asistenciaPerfilRepository;
    private final AsistenciaRegistroRepository asistenciaRegistroRepository;

    public EmpleadoControlService(
            UserRepository userRepository,
            AsistenciaPerfilRepository asistenciaPerfilRepository,
            AsistenciaRegistroRepository asistenciaRegistroRepository) {
        this.userRepository = userRepository;
        this.asistenciaPerfilRepository = asistenciaPerfilRepository;
        this.asistenciaRegistroRepository = asistenciaRegistroRepository;
    }

    public List<EmpleadoControlResponse> listar() {
        LocalDate hoy = LocalDate.now(LIMA_ZONE);
        LocalDate primerDiaMes = hoy.withDayOfMonth(1);
        LocalDateTime inicioMes = primerDiaMes.atStartOfDay();
        LocalDateTime finHoy = hoy.atTime(LocalTime.MAX);

        Map<Long, List<AsistenciaRegistro>> registrosPorUsuario = asistenciaRegistroRepository
                .findAllByFechaBetweenOrderByFechaAsc(inicioMes, finHoy)
                .stream()
                .filter(registro -> registro.getUsuario() != null)
                .collect(Collectors.groupingBy(registro -> registro.getUsuario().getId()));

        return userRepository.findAll().stream()
                .filter(user -> user.getEstado() != null && user.getEstado() == 1)
                .map(user -> toResponse(user, registrosPorUsuario.getOrDefault(user.getId(), List.of()), hoy))
                .toList();
    }

    private EmpleadoControlResponse toResponse(User user, List<AsistenciaRegistro> registrosMes, LocalDate hoy) {
        AsistenciaPerfil perfil = asistenciaPerfilRepository.findByUsuarioId(user.getId()).orElse(null);
        Map<LocalDate, List<AsistenciaRegistro>> registrosPorDia = registrosMes.stream()
                .collect(Collectors.groupingBy(registro -> registro.getFecha().toLocalDate()));

        List<AsistenciaRegistro> registrosHoy = registrosPorDia.getOrDefault(hoy, List.of());
        AsistenciaRegistro entradaHoy = registrosHoy.stream()
                .filter(registro -> "ENTRADA".equals(registro.getTipo()))
                .findFirst()
                .orElse(null);
        AsistenciaRegistro salidaHoy = registrosHoy.stream()
                .filter(registro -> "SALIDA".equals(registro.getTipo()))
                .reduce((first, second) -> second)
                .orElse(null);

        int asistencias = 0;
        int tardanzas = 0;
        int faltas = 0;

        LocalDate fechaInicioControl = hoy.withDayOfMonth(1);
        if (perfil != null && perfil.getCreadoEn() != null && perfil.getCreadoEn().toLocalDate().isAfter(fechaInicioControl)) {
            fechaInicioControl = perfil.getCreadoEn().toLocalDate();
        }

        for (LocalDate fecha = fechaInicioControl; !fecha.isAfter(hoy); fecha = fecha.plusDays(1)) {
            if (fecha.getDayOfWeek() == DayOfWeek.SATURDAY || fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }

            AsistenciaRegistro entradaDia = registrosPorDia.getOrDefault(fecha, List.of()).stream()
                    .filter(registro -> "ENTRADA".equals(registro.getTipo()))
                    .findFirst()
                    .orElse(null);

            if (entradaDia == null) {
                faltas++;
                continue;
            }

            asistencias++;
            if (entradaDia.getFecha().toLocalTime().isAfter(HORA_TARDANZA)) {
                tardanzas++;
            }
        }

        String estado = "Pendiente";
        if (perfil == null) {
            estado = "Pendiente";
        } else if (entradaHoy != null) {
            estado = entradaHoy.getFecha().toLocalTime().isAfter(HORA_TARDANZA) ? "Tarde" : "Presente";
        } else if (esDiaLaboral(hoy)) {
            estado = "Falta";
        }

        return new EmpleadoControlResponse(
                user.getId(),
                user.getNombre(),
                user.getPerfil() != null ? user.getPerfil().getNombre() : "Sin perfil",
                entradaHoy != null ? entradaHoy.getFecha().toLocalTime().format(HORA_FORMATTER) : "--",
                salidaHoy != null ? salidaHoy.getFecha().toLocalTime().format(HORA_FORMATTER) : "--",
                tardanzas,
                faltas,
                asistencias,
                estado);
    }

    private boolean esDiaLaboral(LocalDate fecha) {
        return fecha.getDayOfWeek() != DayOfWeek.SATURDAY && fecha.getDayOfWeek() != DayOfWeek.SUNDAY;
    }
}
