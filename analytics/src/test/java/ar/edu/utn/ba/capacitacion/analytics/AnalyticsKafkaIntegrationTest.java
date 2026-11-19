
package ar.edu.utn.ba.capacitacion.analytics;

import ar.edu.utn.ba.capacitacion.analytics.evento.PedidoCreado;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 3,
        topics = {
                "pedidos.creado.v2",
                "pedidos.creado.v2.DLT"
        },
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AnalyticsKafkaIntegrationTest {

    private static final String TOPIC = "pedidos.creado.v2";
    private static final String DLT = TOPIC + ".DLT";
    private static final String GRUPO = "analytics-test";

    @Autowired
    private KafkaTemplate<String, PedidoCreado> kafkaTemplate;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EmbeddedKafkaBroker broker;

    @Test
    void consumeYGuardaUnEventoReal() {

        PedidoCreado evento = new PedidoCreado(
                UUID.randomUUID().toString(),
                42L,
                3L,
                new BigDecimal("1500.50"),
                Instant.now()
        );

        kafkaTemplate.send(
                TOPIC,
                evento.pedidoId().toString(),
                evento
        ).join();

        await().atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> {

                    Integer cantidad = jdbc.queryForObject(
                            """
                            SELECT COUNT(*)
                            FROM eventos_pedido_recibidos
                            WHERE event_id = ?
                            """,
                            Integer.class,
                            evento.eventId()
                    );

                    assertThat(cantidad).isEqualTo(1);
                });

        var registro = jdbc.queryForMap(
                """
                SELECT pedido_id, cliente_id, total
                FROM eventos_pedido_recibidos
                WHERE event_id = ?
                """,
                evento.eventId()
        );

        assertThat(((Number) registro.get("PEDIDO_ID")).longValue())
                .isEqualTo(42L);

        assertThat(((Number) registro.get("CLIENTE_ID")).longValue())
                .isEqualTo(3L);

        assertThat((BigDecimal) registro.get("TOTAL"))
                .isEqualByComparingTo("1500.50");
    }

    @Test
    void dosEntregasProducenUnSoloRegistro() throws Exception {

        PedidoCreado evento = new PedidoCreado(
                UUID.randomUUID().toString(),
                42L,
                3L,
                new BigDecimal("1250.00"),
                Instant.now()
        );

        var consumerFactory = new DefaultKafkaConsumerFactory<>(
                KafkaTestUtils.consumerProps(
                        "observador-dlt-" + UUID.randomUUID(),
                        "false",
                        broker
                ),
                new StringDeserializer(),
                new StringDeserializer()
        );

        try (
                Consumer<String, String> consumidorDLT =
                        consumerFactory.createConsumer();

                AdminClient admin = AdminClient.create(Map.of(
                        AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                        broker.getBrokersAsString()
                ))
        ) {

            broker.consumeFromAnEmbeddedTopic(consumidorDLT, DLT);

            // Primera entrega
            kafkaTemplate.send(
                    TOPIC,
                    evento.pedidoId().toString(),
                    evento
            ).join();

            await().atMost(Duration.ofSeconds(15))
                    .untilAsserted(() ->
                            assertThat(cantidadEnOracle(evento.eventId()))
                                    .isEqualTo(1)
                    );

            // Segunda entrega del mismo evento
            var segundaPublicacion = kafkaTemplate.send(
                    TOPIC,
                    evento.pedidoId().toString(),
                    evento
            ).join();

            TopicPartition particion = new TopicPartition(
                    TOPIC,
                    segundaPublicacion.getRecordMetadata().partition()
            );

            long offsetSegunda =
                    segundaPublicacion.getRecordMetadata().offset();

            // Esperar que Analytics confirme el segundo offset
            await().atMost(Duration.ofSeconds(15))
                    .untilAsserted(() -> {

                        var offsetsConfirmados = admin
                                .listConsumerGroupOffsets(GRUPO)
                                .partitionsToOffsetAndMetadata()
                                .get(5, TimeUnit.SECONDS);

                        assertThat(offsetsConfirmados)
                                .containsKey(particion);

                        assertThat(
                                offsetsConfirmados.get(particion).offset()
                        ).isGreaterThan(offsetSegunda);
                    });

            // Dos entregas atendidas, un solo efecto de negocio
            assertThat(cantidadEnOracle(evento.eventId()))
                    .isEqualTo(1);

            // El duplicado no debe derivarse a la DLT
            var registrosDLT = KafkaTestUtils.getRecords(
                    consumidorDLT,
                    Duration.ofSeconds(2)
            );

            assertThat(registrosDLT.records(DLT))
                    .noneMatch(registro ->
                            registro.value() != null
                            && registro.value().contains(evento.eventId())
                    );
        }
    }

    private int cantidadEnOracle(String eventId) {
        Integer cantidad = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM eventos_pedido_recibidos
                WHERE event_id = ?
                """,
                Integer.class,
                eventId
        );

        return cantidad == null ? 0 : cantidad;
    }
}