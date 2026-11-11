package ar.edu.utn.ba.capacitacion.analytics.dto;

import java.math.BigDecimal;

public record AnalyticsResumenResponse(
        long cantidadPedidos,
        BigDecimal importeTotal,
        BigDecimal importePromedio
) {
}
