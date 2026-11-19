
package ar.edu.utn.ba.capacitacion.monolito.outbox;

import ar.edu.utn.ba.capacitacion.monolito.pedido.evento.PedidoCreado;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@EmbeddedKafka(partitions = 3, topics = "pedidos.creado.v2")
class OutboxPublisherIntegracionKafkaTest {

    private static final String TOPIC = "pedidos.creado.v2";

    @Test
    void publicaElEventoEnKafka(EmbeddedKafkaBroker broker)
            throws Exception {

        ObjectMapper mapper =
                new ObjectMapper().findAndRegisterModules();

        OutboxRepository repository = mock(OutboxRepository.class);

        // Productor real conectado al broker de prueba
        var producerFactory = new DefaultKafkaProducerFactory<>(
                KafkaTestUtils.producerProps(broker),
                new StringSerializer(),
                new JsonSerializer<PedidoCreado>()
        );

        var kafkaTemplate = new KafkaTemplate<>(producerFactory);

        // Consumidor de prueba
        var consumerFactory = new DefaultKafkaConsumerFactory<>(
                KafkaTestUtils.consumerProps(
                        "test-publicador-" + UUID.randomUUID(),
                        "false",
                        broker
                ),
                new StringDeserializer(),
                new StringDeserializer()
        );

        try (var consumer = consumerFactory.createConsumer()) {

            broker.consumeFromAnEmbeddedTopic(consumer, TOPIC);

            PedidoCreado evento = new PedidoCreado(
                    "evento-integracion-14",
                    42L,
                    3L,
                    new BigDecimal("1500.50"),
                    Instant.parse("2026-11-18T12:00:00Z")
            );

            OutboxPendiente pendiente = new OutboxPendiente(
                    evento.eventId(),
                    TOPIC,
                    "42",
                    mapper.writeValueAsString(evento)
            );

            when(repository.obtenerPendientes())
                    .thenReturn(List.of(pendiente));

            OutboxPublisher publisher = new OutboxPublisher(
                    repository,
                    kafkaTemplate,
                    mapper
            );

            // Publicación real
            publisher.publicarPendientes();

            // Lectura del registro recibido
            ConsumerRecord<String, String> registro =
                    KafkaTestUtils.getSingleRecord(
                            consumer,
                            TOPIC
                    );

            PedidoCreado recibido = mapper.readValue(
                    registro.value(),
                    PedidoCreado.class
            );

            // Comprobaciones sobre el mensaje publicado
            assertThat(registro.topic()).isEqualTo(TOPIC);
            assertThat(registro.key()).isEqualTo("42");

            assertThat(recibido.eventId())
                    .isEqualTo("evento-integracion-14");

            assertThat(recibido.pedidoId()).isEqualTo(42L);
            assertThat(recibido.clienteId()).isEqualTo(3L);

            assertThat(recibido.total())
                    .isEqualByComparingTo("1500.50");

            verify(repository)
                    .marcarPublicado("evento-integracion-14");

        } finally {
            producerFactory.destroy();
        }
    }
}