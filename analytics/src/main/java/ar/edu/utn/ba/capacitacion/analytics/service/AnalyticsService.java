package ar.edu.utn.ba.capacitacion.analytics.service;

import ar.edu.utn.ba.capacitacion.analytics.dto.AnalyticsResumenResponse;
import ar.edu.utn.ba.capacitacion.analytics.repository.EventoPedidoRecibidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class AnalyticsService {

    private static final BigDecimal CERO_MONETARIO = new BigDecimal("0.00");

    private final EventoPedidoRecibidoRepository repository;

    public AnalyticsService(EventoPedidoRecibidoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public AnalyticsResumenResponse obtenerResumen() {
        long cantidad = repository.count();
        if (cantidad == 0) {
            return new AnalyticsResumenResponse(0L, CERO_MONETARIO, CERO_MONETARIO);
        }

        BigDecimal suma = repository.sumarTotal();
        BigDecimal total = (suma != null ? suma : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal promedio = total.divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.HALF_UP);

        return new AnalyticsResumenResponse(cantidad, total, promedio);
    }
}
