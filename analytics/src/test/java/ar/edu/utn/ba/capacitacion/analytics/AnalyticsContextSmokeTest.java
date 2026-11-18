
package ar.edu.utn.ba.capacitacion.analytics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

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
@DirtiesContext
class AnalyticsContextSmokeTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private Environment environment;

    @Test
    void contextoDeTestingIniciaCorrectamente() {

        assertThat(environment.getActiveProfiles())
                .contains("test");

        String usuarioOracle = jdbcTemplate.queryForObject(
                "SELECT USER FROM dual",
                String.class
        );

        assertThat(usuarioOracle)
                .isEqualTo("ANALYTICS_TEST");

        assertThat(environment.getProperty(
                "spring.kafka.bootstrap-servers"))
                .isEqualTo(embeddedKafka.getBrokersAsString());
    }
}
