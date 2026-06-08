package com.tambo.sistematambo.service;

import com.tambo.sistematambo.dto.ProductoRequest;
import com.tambo.sistematambo.model.Categoria;
import com.tambo.sistematambo.model.Producto;
import com.tambo.sistematambo.repository.CategoriaRepository;
import com.tambo.sistematambo.repository.ProductoRepository;
import com.tambo.sistematambo.response.ProductoResponse;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final AlmacenService almacenService;

    public ProductoService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            AlmacenService almacenService) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.almacenService = almacenService;
    }

    public List<ProductoResponse> listar() {
        return productoRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ProductoResponse crear(ProductoRequest request) {
        validarSku(null, request.sku(), request.almacenId());
        Producto producto = new Producto();
        mapRequest(producto, request);
        return toResponse(productoRepository.save(producto));
    }

    public ProductoResponse actualizar(Long productoId, ProductoRequest request) {
        Producto producto = buscarEntidad(productoId);
        validarSku(productoId, request.sku(), request.almacenId());
        mapRequest(producto, request);
        return toResponse(productoRepository.save(producto));
    }

    public void eliminar(Long productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }
        productoRepository.deleteById(productoId);
    }

    public Producto buscarEntidad(Long productoId) {
        return productoRepository.findById(productoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    private void validarSku(Long productoId, String sku, Long almacenId) {
        String normalized = normalizeSku(sku);
        boolean duplicado = productoId == null
                ? productoRepository.existsBySkuAndAlmacenId(normalized, almacenId)
                : productoRepository.existsBySkuAndAlmacenIdAndIdNot(normalized, almacenId, productoId);
        if (duplicado) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un producto con ese SKU en el almacen seleccionado");
        }
    }

    private void mapRequest(Producto producto, ProductoRequest request) {
        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria no encontrada"));
        validarCategoriaActiva(categoria);
        validarStock(request.stock(), request.stockMinimo());

        producto.setNombre(StringUtils.trim(request.nombre()));
        producto.setSku(normalizeSku(request.sku()));
        producto.setPrecioCompra(request.precioCompra());
        producto.setPrecioVenta(request.precioVenta());
        producto.setStock(request.stock());
        producto.setStockMinimo(request.stockMinimo());
        producto.setCategoria(categoria);
        producto.setAlmacen(almacenService.buscarEntidad(request.almacenId()));
        producto.setImagen(StringUtils.trimToEmpty(request.imagen()));
        producto.setEstado(request.estado());
    }

    private String normalizeSku(String sku) {
        String normalized = StringUtils.upperCase(StringUtils.trimToEmpty(sku));

        if (StringUtils.isBlank(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El SKU es obligatorio");
        }

        return normalized;
    }

    private void validarCategoriaActiva(Categoria categoria) {
        if (categoria.getEstado() == null || categoria.getEstado() != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La categoria seleccionada no esta activa");
        }
    }

    private void validarStock(Integer stock, Integer stockMinimo) {
        if (stock == null || stock < 0 || stockMinimo == null || stockMinimo < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El stock y el stock minimo deben ser valores no negativos");
        }
    }

    ProductoResponse toResponse(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getSku(),
                producto.getPrecioCompra(),
                producto.getPrecioVenta(),
                producto.getStock(),
                producto.getStockMinimo(),
                producto.getCategoria().getId(),
                producto.getCategoria().getNombre(),
                producto.getAlmacen().getId(),
                producto.getAlmacen().getNombre(),
                producto.getImagen(),
                producto.getEstado());
    }
}
