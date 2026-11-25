package ar.edu.utn.ba.capacitacion.monolito.pedido.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PedidoObservabilidad {

    private static final Logger log =
            LoggerFactory.getLogger(PedidoObservabilidad.class);

    private final Counter confirmados;
    private final String instanceId;

    public PedidoObservabilidad(
            MeterRegistry registry,
            @Value("${app.instance.id:local}") String instanceId) {

        this.confirmados = registry.counter("pedidos.confirmados");
        this.instanceId = instanceId;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alConfirmar(PedidoConfirmadoInterno evento) {
        confirmados.increment();

        log.info(
                "Pedido confirmado. pedidoId={} instancia={}",
                evento.pedidoId(),
                instanceId
        );
    }
}