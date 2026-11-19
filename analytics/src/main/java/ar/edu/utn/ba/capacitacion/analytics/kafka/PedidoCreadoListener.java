package ar.edu.utn.ba.capacitacion.analytics.kafka;

import ar.edu.utn.ba.capacitacion.analytics.evento.PedidoCreado;
import ar.edu.utn.ba.capacitacion.analytics.service.AnalyticsService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class PedidoCreadoListener {

    private static final Logger log =
            LoggerFactory.getLogger(PedidoCreadoListener.class);

    private final AnalyticsService analyticsService;
    private final boolean fallaPersistenteActiva;

    private final ConcurrentHashMap<String, AtomicInteger> intentos =
            new ConcurrentHashMap<>();

    public PedidoCreadoListener(
            AnalyticsService analyticsService,
            @Value("${app.demo.falla-persistente:true}")
            boolean fallaPersistenteActiva) {

        this.analyticsService = analyticsService;
        this.fallaPersistenteActiva = fallaPersistenteActiva;
    }

    @KafkaListener(topics = "${app.kafka.topic.pedido-creado}")
    public void recibir(ConsumerRecord<String, PedidoCreado> registro) {

        PedidoCreado evento = registro.value();

        if (evento.total().compareTo(
                new BigDecimal("901.00")) == 0) {

            int intento = intentos
                    .computeIfAbsent(
                            evento.eventId(),
                            id -> new AtomicInteger()
                    )
                    .incrementAndGet();

            log.info(
                    "DEMO transitoria. eventId={}, intento={}",
                    evento.eventId(), intento
            );

            if (intento <= 2) {
                throw new IllegalStateException(
                        "Falla transitoria simulada");
            }
        }

        if (evento.total().compareTo(
                new BigDecimal("902.00")) == 0
                && fallaPersistenteActiva) {

            log.warn(
                    "DEMO persistente. eventId={}",
                    evento.eventId()
            );

            throw new IllegalStateException(
                    "Falla persistente simulada");
        }

        analyticsService.registrarPedidoCreado(evento);

        log.info(
                "PedidoCreado procesado. pedidoId={}, particion={}, offset={}",
                evento.pedidoId(),
                registro.partition(),
                registro.offset()
        );
    }
}