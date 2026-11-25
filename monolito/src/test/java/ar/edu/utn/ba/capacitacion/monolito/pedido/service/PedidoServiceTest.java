package ar.edu.utn.ba.capacitacion.monolito.pedido.service;

import ar.edu.utn.ba.capacitacion.monolito.outbox.OutboxRepository;
import ar.edu.utn.ba.capacitacion.monolito.pedido.client.ClienteLookup;
import ar.edu.utn.ba.capacitacion.monolito.pedido.client.ClienteResumen;
import ar.edu.utn.ba.capacitacion.monolito.pedido.dto.CrearPedidoRequest;
import ar.edu.utn.ba.capacitacion.monolito.pedido.exception.ClienteInvalidoException;
import ar.edu.utn.ba.capacitacion.monolito.pedido.repository.PedidoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private ClienteLookup clienteLookup;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private PedidoService service;

    @BeforeEach
    void preparar() {
        service = new PedidoService(
                clienteLookup,
                pedidoRepository,
                outboxRepository,
                new ObjectMapper().findAndRegisterModules(),
                "pedidos.creado.v2",
                applicationEventPublisher
        );
    }

    @Test
    void clienteInactivoNoGeneraPedidoNiOutbox() {
        var solicitud = new CrearPedidoRequest(
                2L,
                "Pedido no autorizado",
                new BigDecimal("1500.50")
        );

        when(clienteLookup.buscarPorId(2L))
                .thenReturn(
                        Optional.of(new ClienteResumen(2L, false))
                );

        assertThrows(
                ClienteInvalidoException.class,
                () -> service.crearPedido(solicitud)
        );

        verifyNoInteractions(
                pedidoRepository,
                outboxRepository,
                applicationEventPublisher
        );
    }
}