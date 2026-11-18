package ar.edu.utn.ba.capacitacion.monolito.pedido.kafka;

import ar.edu.utn.ba.capacitacion.monolito.pedido.evento.PedidoCreado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PedidoEventPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(PedidoEventPublisher.class);

    private final KafkaTemplate<String, PedidoCreado> kafkaTemplate;
    private final String pedidosCreadoTopic;

    public PedidoEventPublisher(
            KafkaTemplate<String, PedidoCreado> kafkaTemplate,
            @Value("${app.kafka.topics.pedidos-creado}") String pedidosCreadoTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.pedidosCreadoTopic = pedidosCreadoTopic;
    }

    public void publicar(PedidoCreado evento) {
        kafkaTemplate.send(
                pedidosCreadoTopic,
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