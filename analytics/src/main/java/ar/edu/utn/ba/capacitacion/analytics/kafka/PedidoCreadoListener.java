package ar.edu.utn.ba.capacitacion.analytics.kafka;

import ar.edu.utn.ba.capacitacion.analytics.evento.PedidoCreado;
import ar.edu.utn.ba.capacitacion.analytics.service.AnalyticsService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PedidoCreadoListener {

    private static final Logger log =
            LoggerFactory.getLogger(PedidoCreadoListener.class);

    private final AnalyticsService analyticsService;

    public PedidoCreadoListener(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @KafkaListener(topics = "pedidos.creado.v1")
    public void recibir(ConsumerRecord<String, PedidoCreado> registro) {
        PedidoCreado evento = registro.value();

        analyticsService.registrarPedidoCreado(evento);

        log.info(
                "PedidoCreado procesado. pedidoId={}, particion={}, offset={}",
                evento.pedidoId(),
                registro.partition(),
                registro.offset()
        );
    }
}