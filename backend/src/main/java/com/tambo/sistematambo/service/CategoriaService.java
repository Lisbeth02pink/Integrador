package com.tambo.sistematambo.service;

import com.tambo.sistematambo.dto.CategoriaRequest;
import com.tambo.sistematambo.model.Categoria;
import com.tambo.sistematambo.repository.CategoriaRepository;
import com.tambo.sistematambo.response.CategoriaResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CategoriaService {
    private static final List<String> CODIGOS_RESTRINGIDOS = List.of("CAT-ROP", "CAT-TEC", "CAT-ELE");
    private static final List<String> NOMBRES_RESTRINGIDOS = List.of("ROPA", "TECNOLOGIA", "TECNOLOGÍA", "ELECTRONICA", "ELECTRONICA", "ELECTRONICOS", "ELECTRÓNICOS");

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll().stream().map(this::toResponse).toList();
    }

    public CategoriaResponse crear(CategoriaRequest request) {
        validarCodigo(null, request.codigo());
        validarDominioTambo(request.nombre(), request.codigo());
        Categoria categoria = new Categoria();
        mapRequest(categoria, request);
        return toResponse(categoriaRepository.save(categoria));
    }

    public CategoriaResponse actualizar(Long categoriaId, CategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria no encontrada"));
        validarCodigo(categoriaId, request.codigo());
        validarDominioTambo(request.nombre(), request.codigo());
        mapRequest(categoria, request);
        return toResponse(categoriaRepository.save(categoria));
    }

    public void eliminar(Long categoriaId) {
        if (!categoriaRepository.existsById(categoriaId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria no encontrada");
        }
        categoriaRepository.deleteById(categoriaId);
    }

    private void validarCodigo(Long categoriaId, String codigo) {
        String normalized = codigo.trim().toUpperCase();
        boolean duplicado = categoriaId == null
                ? categoriaRepository.existsByCodigo(normalized)
                : categoriaRepository.existsByCodigoAndIdNot(normalized, categoriaId);
        if (duplicado) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe una categoria con ese codigo");
        }
    }

    private void mapRequest(Categoria categoria, CategoriaRequest request) {
        categoria.setNombre(request.nombre().trim());
        categoria.setCodigo(request.codigo().trim().toUpperCase());
        categoria.setDescripcion(request.descripcion() != null ? request.descripcion().trim() : "");
        categoria.setImagen(request.imagen() != null ? request.imagen().trim() : "");
        categoria.setEstado(request.estado());
    }

    private void validarDominioTambo(String nombre, String codigo) {
        String normalizedName = nombre.trim().toUpperCase();
        String normalizedCode = codigo.trim().toUpperCase();

        if (CODIGOS_RESTRINGIDOS.contains(normalizedCode) || NOMBRES_RESTRINGIDOS.contains(normalizedName)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tambo no comercializa ropa ni articulos electronicos; usa categorias alineadas al retail de conveniencia");
        }
    }

    private CategoriaResponse toResponse(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getCodigo(),
                categoria.getDescripcion(),
                categoria.getImagen(),
                categoria.getEstado());
    }
}
