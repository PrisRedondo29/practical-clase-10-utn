
package ar.edu.utn.ba.capacitacion.monolito.outbox;

import ar.edu.utn.ba.capacitacion.monolito.pedido.evento.PedidoCreado;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

    @Mock
    private OutboxRepository repository;

    @Mock
    private KafkaTemplate<String, PedidoCreado> kafkaTemplate;

    private ObjectMapper objectMapper;
    private OutboxPublisher publisher;

    @BeforeEach
    void preparar() {
        objectMapper = new ObjectMapper().findAndRegisterModules();

        publisher = new OutboxPublisher(
                repository,
                kafkaTemplate,
                objectMapper
        );
    }

    @Test
    void publicaUnEventoPendienteYLoMarcaComoPublicado()
            throws Exception {

        PedidoCreado evento = new PedidoCreado(
                "evento-test-14",
                42L,
                3L,
                new BigDecimal("1500.50"),
                Instant.parse("2026-11-18T12:00:00Z")
        );

        OutboxPendiente pendiente = new OutboxPendiente(
                evento.eventId(),
                "pedidos.creado.v2",
                "42",
                objectMapper.writeValueAsString(evento)
        );

        when(repository.obtenerPendientes())
                .thenReturn(List.of(pendiente));

        when(kafkaTemplate.send(
                "pedidos.creado.v2",
                "42",
                evento
        )).thenReturn(
                CompletableFuture.completedFuture(
                        mock(SendResult.class)
                )
        );

        publisher.publicarPendientes();

        verify(kafkaTemplate).send(
                "pedidos.creado.v2",
                "42",
                evento
        );

        verify(repository).marcarPublicado("evento-test-14");
    }
}