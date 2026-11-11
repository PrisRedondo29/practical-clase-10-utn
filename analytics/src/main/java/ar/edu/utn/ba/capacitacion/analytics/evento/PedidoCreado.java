package ar.edu.utn.ba.capacitacion.analytics.evento;

import java.math.BigDecimal;
import java.time.Instant;

public record PedidoCreado(
        String eventId,
        Long pedidoId,
        Long clienteId,
        BigDecimal total,
        Instant occurredAt
) {
}