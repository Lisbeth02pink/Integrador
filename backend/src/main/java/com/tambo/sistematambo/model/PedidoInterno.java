package com.tambo.sistematambo.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos_internos")
public class PedidoInterno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tienda_id", nullable = false)
    private Almacen tienda;

    @Column(name = "solicitado_por", nullable = false, length = 120)
    private String solicitadoPor;

    @Column(nullable = false, length = 20)
    private String prioridad;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(length = 400)
    private String observaciones;

    @Column(name = "transferencia_generada", nullable = false)
    private Boolean transferenciaGenerada = false;

    @OneToMany(mappedBy = "pedidoInterno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoInternoItem> items = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Almacen getTienda() {
        return tienda;
    }

    public void setTienda(Almacen tienda) {
        this.tienda = tienda;
    }

    public String getSolicitadoPor() {
        return solicitadoPor;
    }

    public void setSolicitadoPor(String solicitadoPor) {
        this.solicitadoPor = solicitadoPor;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Boolean getTransferenciaGenerada() {
        return transferenciaGenerada;
    }

    public void setTransferenciaGenerada(Boolean transferenciaGenerada) {
        this.transferenciaGenerada = transferenciaGenerada;
    }

    public List<PedidoInternoItem> getItems() {
        return items;
    }

    public void setItems(List<PedidoInternoItem> items) {
        this.items = items;
    }
}
