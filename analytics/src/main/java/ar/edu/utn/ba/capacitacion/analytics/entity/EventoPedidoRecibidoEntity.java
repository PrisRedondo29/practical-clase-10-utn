package ar.edu.utn.ba.capacitacion.analytics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "eventos_pedido_recibidos")
public class EventoPedidoRecibidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 100)
    private String eventId;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    public EventoPedidoRecibidoEntity() {
    }

    public EventoPedidoRecibidoEntity(String eventId, Long pedidoId, Long clienteId, BigDecimal total, Instant occurredAt, Instant receivedAt) {
        this.eventId = eventId;
        this.pedidoId = pedidoId;
        this.clienteId = clienteId;
        this.total = total;
        this.occurredAt = occurredAt;
        this.receivedAt = receivedAt;
    }

    public EventoPedidoRecibidoEntity(Long id, String eventId, Long pedidoId, Long clienteId, BigDecimal total, Instant occurredAt, Instant receivedAt) {
        this.id = id;
        this.eventId = eventId;
        this.pedidoId = pedidoId;
        this.clienteId = clienteId;
        this.total = total;
        this.occurredAt = occurredAt;
        this.receivedAt = receivedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }
}
