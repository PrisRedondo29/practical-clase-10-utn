package ar.edu.utn.ba.capacitacion.monolito.pedido.repository;

import ar.edu.utn.ba.capacitacion.monolito.pedido.entity.PedidoEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Testcontainers
class PedidoRepositoryIntegracionTest {

    @Container
    @ServiceConnection
    static OracleContainer oracle =
            new OracleContainer("gvenzl/oracle-free:23-slim-faststart");

    @Autowired
    private PedidoRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void guardaYRecuperaUnPedidoEnOracle() {
        var pedido = new PedidoEntity(
                1L,
                "Pedido de integración",
                new BigDecimal("1500.50")
        );

        var guardado = repository.saveAndFlush(pedido);
        Long id = guardado.getId();

        entityManager.clear();

        var recuperado = repository.findById(id).orElseThrow();

        assertThat(recuperado.getClienteId()).isEqualTo(1L);
        assertThat(recuperado.getDescripcion())
                .isEqualTo("Pedido de integración");
        assertThat(recuperado.getTotal())
                .isEqualByComparingTo("1500.50");
    }
}