package ar.edu.utn.ba.capacitacion.analytics;

import ar.edu.utn.ba.capacitacion.analytics.evento.PedidoCreado;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 3,
        topics = {"pedidos.creado.v2", "pedidos.creado.v2.DLT"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@Import(AnalyticsKafkaFailuresTest.ConfiguracionTest.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AnalyticsKafkaFailuresTest {

    private static final String TOPIC = "pedidos.creado.v2";
    private static final String DLT = TOPIC + ".DLT";

    @Autowired
    private KafkaTemplate<String, PedidoCreado> kafkaTemplate;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EmbeddedKafkaBroker broker;

    @Autowired
    private FallasControladas fallas;

    // 1. Configuracion exclusiva de testing

    @TestConfiguration(proxyBeanMethods = false)
    static class ConfiguracionTest {

        @Bean
        FallasControladas fallasControladas() {
            return new FallasControladas();
        }
    }

    // 2. Simulacion controlada de errores

    @Aspect
    @Order(Ordered.HIGHEST_PRECEDENCE)
    static class FallasControladas {

        private final ConcurrentHashMap<String, AtomicInteger> intentos =
                new ConcurrentHashMap<>();

        private final ConcurrentHashMap<String, Boolean> persistentes =
                new ConcurrentHashMap<>();

        void configurar(String eventId, boolean persistente) {
            intentos.put(eventId, new AtomicInteger());
            persistentes.put(eventId, persistente);
        }

        int intentos(String eventId) {
            return intentos.get(eventId).get();
        }

        @Around("execution(* ar.edu.utn.ba.capacitacion.analytics.service.AnalyticsService.registrarPedidoCreado(..))")
        public Object interceptar(ProceedingJoinPoint llamada) throws Throwable {
            PedidoCreado evento = (PedidoCreado) llamada.getArgs()[0];

            AtomicInteger contador = intentos.get(evento.eventId());

            // Eventos no configurados se procesan normalmente
            if (contador == null) {
                return llamada.proceed();
            }

            int intento = contador.incrementAndGet();
            boolean persistente = persistentes.get(evento.eventId());

            // Falla siempre, o solo en los primeros dos intentos
            if (persistente || intento <= 2) {
                throw new IllegalStateException(
                        "Falla controlada del test"
                );
            }

            // El tercer intento transitorio ejecuta el Service real
            return llamada.proceed();
        }
    }

    // 3. Metodos auxiliares de las pruebas

    private PedidoCreado nuevoEvento(String total) {
        return new PedidoCreado(
                UUID.randomUUID().toString(),
                42L,
                3L,
                new BigDecimal(total),
                Instant.now()
        );
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

    // 4. Prueba de falla transitoria

    @Test
    void fallaTransitoriaSeRecupera() {

        PedidoCreado evento = nuevoEvento("410.00");

        fallas.configurar(evento.eventId(), false);

        kafkaTemplate.send(
                TOPIC,
                evento.pedidoId().toString(),
                evento
        ).join();

        await().atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> {

                    assertThat(fallas.intentos(evento.eventId()))
                            .isEqualTo(3);

                    assertThat(cantidadEnOracle(evento.eventId()))
                            .isEqualTo(1);
                });
    }

    // 5. Prueba de falla persistente y DLT

    @Test
    void fallaPersistenteTerminaEnDlt() {

        PedidoCreado evento = nuevoEvento("420.00");

        fallas.configurar(evento.eventId(), true);

        var factory = new DefaultKafkaConsumerFactory<>(
                KafkaTestUtils.consumerProps(
                        "observador-dlt-" + UUID.randomUUID(),
                        "false",
                        broker
                ),
                new StringDeserializer(),
                new StringDeserializer()
        );

        try (Consumer<String, String> consumer =
                     factory.createConsumer()) {

            broker.consumeFromAnEmbeddedTopic(consumer, DLT);

            kafkaTemplate.send(
                    TOPIC,
                    evento.pedidoId().toString(),
                    evento
            ).join();

            ConsumerRecord<String, String> recibido =
                    KafkaTestUtils.getSingleRecord(
                            consumer,
                            DLT,
                            Duration.ofSeconds(15)
                    );

            assertThat(recibido.key())
                    .isEqualTo(evento.pedidoId().toString());

            assertThat(recibido.value())
                    .contains(evento.eventId());

            assertThat(fallas.intentos(evento.eventId()))
                    .isEqualTo(3);

            assertThat(cantidadEnOracle(evento.eventId()))
                    .isZero();
        }
    }
}
