package com.tambo.sistematambo.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class MermaResponse {

    private Long movimientoId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fecha;

    private Long productoId;
    private String productoNombre;
    private String almacenNombre;
    private Integer cantidad;
    private String motivo;
    private String responsable;
    private String observaciones;

    public Long getMovimientoId() {
        return movimientoId;
    }

    public void setMovimientoId(Long movimientoId) {
        this.movimientoId = movimientoId;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public String getAlmacenNombre() {
        return almacenNombre;
    }

    public void setAlmacenNombre(String almacenNombre) {
        this.almacenNombre = almacenNombre;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
