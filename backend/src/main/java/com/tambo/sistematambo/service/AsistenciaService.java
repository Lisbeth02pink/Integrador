package com.tambo.sistematambo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tambo.sistematambo.dto.AsistenciaPerfilRequest;
import com.tambo.sistematambo.dto.AsistenciaRegistroRequest;
import com.tambo.sistematambo.model.AsistenciaPerfil;
import com.tambo.sistematambo.model.AsistenciaRegistro;
import com.tambo.sistematambo.model.User;
import com.tambo.sistematambo.repository.AsistenciaPerfilRepository;
import com.tambo.sistematambo.repository.AsistenciaRegistroRepository;
import com.tambo.sistematambo.repository.UserRepository;
import com.tambo.sistematambo.response.AsistenciaPerfilResponse;
import com.tambo.sistematambo.response.AsistenciaRegistroResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AsistenciaService {

    private static final ZoneId LIMA_ZONE = ZoneId.of("America/Lima");

    private final AsistenciaPerfilRepository perfilRepository;
    private final AsistenciaRegistroRepository registroRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AsistenciaService(
            AsistenciaPerfilRepository perfilRepository,
            AsistenciaRegistroRepository registroRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper) {
        this.perfilRepository = perfilRepository;
        this.registroRepository = registroRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public List<AsistenciaPerfilResponse> listarPerfiles() {
        return perfilRepository.findAll().stream().map(this::toPerfilResponse).toList();
    }

    public AsistenciaPerfilResponse guardarPerfil(AsistenciaPerfilRequest request) {
        if (request.userId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe seleccionar un usuario");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        AsistenciaPerfil perfil = perfilRepository.findByUsuarioId(user.getId())
                .or(() -> perfilRepository.findByCodigoIgnoreCase(user.getId().toString()))
                .orElseGet(AsistenciaPerfil::new);
        perfil.setUsuario(user);
        perfil.setCodigo(user.getId().toString());
        perfil.setNombre(user.getNombre().trim().toUpperCase());
        perfil.setDescriptorJson(writeDescriptor(request.descriptor()));

        if (perfil.getCreadoEn() == null) {
            perfil.setCreadoEn(LocalDateTime.now(LIMA_ZONE));
        }

        return toPerfilResponse(perfilRepository.save(perfil));
    }

    public void eliminarPerfil(Long perfilId) {
        if (!perfilRepository.existsById(perfilId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil facial no encontrado");
        }
        perfilRepository.deleteById(perfilId);
    }

    public List<AsistenciaRegistroResponse> listarRegistros() {
        return registroRepository.findAll().stream()
                .sorted((a, b) -> b.getFecha().compareTo(a.getFecha()))
                .limit(50)
                .map(this::toRegistroResponse)
                .toList();
    }

    public AsistenciaRegistroResponse registrarAsistencia(AsistenciaRegistroRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (user.getEstado() == null || user.getEstado() != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario esta inactivo");
        }

        LocalDate hoy = LocalDate.now(LIMA_ZONE);
        LocalDateTime inicio = hoy.atStartOfDay();
        LocalDateTime fin = hoy.atTime(LocalTime.MAX);
        List<AsistenciaRegistro> registrosHoy = registroRepository.findByUsuarioIdAndFechaBetweenOrderByFechaAsc(
                user.getId(),
                inicio,
                fin);

        validarMarcacion(request.tipo(), registrosHoy);

        AsistenciaRegistro registro = new AsistenciaRegistro();
        registro.setUsuario(user);
        registro.setCodigo(user.getId().toString());
        registro.setNombre(user.getNombre().trim().toUpperCase());
        registro.setCoincidencia(request.coincidencia());
        registro.setFecha(LocalDateTime.now(LIMA_ZONE));
        registro.setTipo(request.tipo().name());

        return toRegistroResponse(registroRepository.save(registro));
    }

    private void validarMarcacion(
            AsistenciaRegistroRequest.TipoMarcacion tipo,
            List<AsistenciaRegistro> registrosHoy) {
        boolean tieneEntrada = registrosHoy.stream().anyMatch(item -> "ENTRADA".equals(item.getTipo()));
        boolean tieneSalida = registrosHoy.stream().anyMatch(item -> "SALIDA".equals(item.getTipo()));

        if (tipo == AsistenciaRegistroRequest.TipoMarcacion.ENTRADA && tieneEntrada) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La entrada ya fue registrada hoy");
        }

        if (tipo == AsistenciaRegistroRequest.TipoMarcacion.SALIDA && !tieneEntrada) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Primero debes registrar la entrada");
        }

        if (tipo == AsistenciaRegistroRequest.TipoMarcacion.SALIDA && tieneSalida) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La salida ya fue registrada hoy");
        }
    }

    private AsistenciaPerfilResponse toPerfilResponse(AsistenciaPerfil perfil) {
        return new AsistenciaPerfilResponse(
                perfil.getId(),
                perfil.getUsuario() != null ? perfil.getUsuario().getId() : null,
                perfil.getCodigo(),
                perfil.getUsuario() != null ? perfil.getUsuario().getUsuario() : null,
                perfil.getNombre(),
                readDescriptor(perfil.getDescriptorJson()),
                perfil.getCreadoEn());
    }

    private AsistenciaRegistroResponse toRegistroResponse(AsistenciaRegistro registro) {
        return new AsistenciaRegistroResponse(
                registro.getId(),
                registro.getUsuario() != null ? registro.getUsuario().getId() : null,
                registro.getCodigo(),
                registro.getUsuario() != null ? registro.getUsuario().getUsuario() : null,
                registro.getNombre(),
                registro.getCoincidencia(),
                registro.getFecha(),
                registro.getTipo());
    }

    private String writeDescriptor(List<Float> descriptor) {
        try {
            return objectMapper.writeValueAsString(descriptor);
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo serializar el descriptor facial");
        }
    }

    private List<Float> readDescriptor(String descriptorJson) {
        try {
            return objectMapper.readValue(descriptorJson, new TypeReference<>() {});
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Descriptor facial invalido");
        }
    }
}
