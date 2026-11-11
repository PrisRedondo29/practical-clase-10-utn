package ar.edu.utn.ba.capacitacion.monolito.pedido.kafka;

import ar.edu.utn.ba.capacitacion.monolito.config.KafkaTopicConfig;
import ar.edu.utn.ba.capacitacion.monolito.pedido.evento.PedidoCreado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PedidoEventPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(PedidoEventPublisher.class);

    private final KafkaTemplate<String, PedidoCreado> kafkaTemplate;

    public PedidoEventPublisher(
            KafkaTemplate<String, PedidoCreado> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publicar(PedidoCreado evento) {
        kafkaTemplate.send(
                KafkaTopicConfig.PEDIDOS_CREADO_TOPIC,
                evento.pedidoId().toString(),
                evento
        ).whenComplete((resultado, error) -> {
            if (error != null) {
                log.error(
                        "Error al publicar PedidoCreado. eventId={}, pedidoId={}",
                        evento.eventId(),
                        evento.pedidoId(),
                        error
                );
                return;
            }

            log.info(
                    "PedidoCreado confirmado por Kafka. eventId={}, topic={}, particion={}, offset={}",
                    evento.eventId(),
                    resultado.getRecordMetadata().topic(),
                    resultado.getRecordMetadata().partition(),
                    resultado.getRecordMetadata().offset()
            );
        });
    }
}