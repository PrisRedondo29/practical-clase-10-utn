package ar.edu.utn.ba.capacitacion.monolito.outbox;

import ar.edu.utn.ba.capacitacion.monolito.pedido.evento.PedidoCreado;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(
        name = "app.outbox.publisher.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class OutboxPublisher {

    private final OutboxRepository repository;
    private final KafkaTemplate<String, PedidoCreado> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(
            OutboxRepository repository,
            KafkaTemplate<String, PedidoCreado> kafkaTemplate,
            ObjectMapper objectMapper) {

        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(
            fixedDelayString = "${app.outbox.intervalo-ms:5000}"
    )
    public void publicarPendientes() {

        for (OutboxPendiente pendiente : repository.obtenerPendientes()) {
            try {
                PedidoCreado evento = objectMapper.readValue(
                        pendiente.payload(),
                        PedidoCreado.class
                );

                kafkaTemplate.send(
                        pendiente.topic(),
                        pendiente.clave(),
                        evento
                ).get(10, TimeUnit.SECONDS);

                repository.marcarPublicado(
                        pendiente.eventId()
                );

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(
                        "Publicación interrumpida. eventId="
                                + pendiente.eventId(), e);

            } catch (Exception e) {
                throw new IllegalStateException(
                        "No se pudo publicar eventId="
                                + pendiente.eventId(), e);
            }
        }
    }
}