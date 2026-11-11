package ar.edu.utn.ba.capacitacion.monolito.pedido.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "pedidos")
public class PedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "descripcion", nullable = false, length = 255)
    private String descripcion;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    public PedidoEntity() {
    }

    public PedidoEntity(Long clienteId, String descripcion, BigDecimal total) {
        this.clienteId = clienteId;
        this.descripcion = descripcion;
        this.total = total;
    }

    public PedidoEntity(Long id, Long clienteId, String descripcion, BigDecimal total) {
        this.id = id;
        this.clienteId = clienteId;
        this.descripcion = descripcion;
        this.total = total;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}


