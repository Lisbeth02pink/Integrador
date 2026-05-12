package com.tambo.sistematambo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "rutas_entrega")
public class RutaEntrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 160)
    private String zona;

    @Column(nullable = false, length = 120)
    private String repartidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private PedidoInterno pedidoInterno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transferencia_id")
    private Transferencia transferencia;

    @Column(length = 80)
    private String vehiculo;

    @Column(length = 30)
    private String tipoVehiculo;

    @Column(length = 20)
    private String placa;

    @Column(name = "capacidad_vehiculo")
    private Integer capacidadVehiculo;

    @Column(name = "cantidad_carga")
    private Integer cantidadCarga;

    @Column(length = 160)
    private String origen;

    @Column(length = 160)
    private String destino;

    @Column(name = "fecha_entrega")
    private LocalDate fechaEntrega;

    @Column(nullable = false)
    private Integer pedidos;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "hora_salida", nullable = false, length = 5)
    private String horaSalida;

    @Column(name = "hora_estimada_llegada", length = 5)
    private String horaEstimadaLlegada;

    @Column(name = "hora_entrega_real", length = 5)
    private String horaEntregaReal;

    @Column(name = "ubicacion_actual", length = 180)
    private String ubicacionActual;

    @Column(length = 500)
    private String observaciones;

    @Column(length = 500)
    private String incidencias;

    @Column(name = "estado_gps", length = 30)
    private String estadoGps;

    @Column(name = "evidencia_entrega", length = 500)
    private String evidenciaEntrega;

    @Column(name = "firma_digital", length = 500)
    private String firmaDigital;

    @Column(name = "foto_entrega", length = 500)
    private String fotoEntrega;

    @Column(name = "vehiculo_activo", nullable = false)
    private Boolean vehiculoActivo = true;

    @Column(name = "conductor_bloqueado", nullable = false)
    private Boolean conductorBloqueado = false;

    @Column(name = "confirmacion_entrega", nullable = false)
    private Boolean confirmacionEntrega = false;

    @Column(nullable = false)
    private Integer progreso;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    public String getRepartidor() {
        return repartidor;
    }

    public void setRepartidor(String repartidor) {
        this.repartidor = repartidor;
    }

    public PedidoInterno getPedidoInterno() {
        return pedidoInterno;
    }

    public void setPedidoInterno(PedidoInterno pedidoInterno) {
        this.pedidoInterno = pedidoInterno;
    }

    public Transferencia getTransferencia() {
        return transferencia;
    }

    public void setTransferencia(Transferencia transferencia) {
        this.transferencia = transferencia;
    }

    public String getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(String vehiculo) {
        this.vehiculo = vehiculo;
    }

    public String getTipoVehiculo() {
        return tipoVehiculo;
    }

    public void setTipoVehiculo(String tipoVehiculo) {
        this.tipoVehiculo = tipoVehiculo;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public Integer getCapacidadVehiculo() {
        return capacidadVehiculo;
    }

    public void setCapacidadVehiculo(Integer capacidadVehiculo) {
        this.capacidadVehiculo = capacidadVehiculo;
    }

    public Integer getCantidadCarga() {
        return cantidadCarga;
    }

    public void setCantidadCarga(Integer cantidadCarga) {
        this.cantidadCarga = cantidadCarga;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public LocalDate getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDate fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public Integer getPedidos() {
        return pedidos;
    }

    public void setPedidos(Integer pedidos) {
        this.pedidos = pedidos;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(String horaSalida) {
        this.horaSalida = horaSalida;
    }

    public String getHoraEstimadaLlegada() {
        return horaEstimadaLlegada;
    }

    public void setHoraEstimadaLlegada(String horaEstimadaLlegada) {
        this.horaEstimadaLlegada = horaEstimadaLlegada;
    }

    public String getHoraEntregaReal() {
        return horaEntregaReal;
    }

    public void setHoraEntregaReal(String horaEntregaReal) {
        this.horaEntregaReal = horaEntregaReal;
    }

    public String getUbicacionActual() {
        return ubicacionActual;
    }

    public void setUbicacionActual(String ubicacionActual) {
        this.ubicacionActual = ubicacionActual;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getIncidencias() {
        return incidencias;
    }

    public void setIncidencias(String incidencias) {
        this.incidencias = incidencias;
    }

    public String getEstadoGps() {
        return estadoGps;
    }

    public void setEstadoGps(String estadoGps) {
        this.estadoGps = estadoGps;
    }

    public String getEvidenciaEntrega() {
        return evidenciaEntrega;
    }

    public void setEvidenciaEntrega(String evidenciaEntrega) {
        this.evidenciaEntrega = evidenciaEntrega;
    }

    public String getFirmaDigital() {
        return firmaDigital;
    }

    public void setFirmaDigital(String firmaDigital) {
        this.firmaDigital = firmaDigital;
    }

    public String getFotoEntrega() {
        return fotoEntrega;
    }

    public void setFotoEntrega(String fotoEntrega) {
        this.fotoEntrega = fotoEntrega;
    }

    public Boolean getVehiculoActivo() {
        return vehiculoActivo;
    }

    public void setVehiculoActivo(Boolean vehiculoActivo) {
        this.vehiculoActivo = vehiculoActivo;
    }

    public Boolean getConductorBloqueado() {
        return conductorBloqueado;
    }

    public void setConductorBloqueado(Boolean conductorBloqueado) {
        this.conductorBloqueado = conductorBloqueado;
    }

    public Boolean getConfirmacionEntrega() {
        return confirmacionEntrega;
    }

    public void setConfirmacionEntrega(Boolean confirmacionEntrega) {
        this.confirmacionEntrega = confirmacionEntrega;
    }

    public Integer getProgreso() {
        return progreso;
    }

    public void setProgreso(Integer progreso) {
        this.progreso = progreso;
    }
}
