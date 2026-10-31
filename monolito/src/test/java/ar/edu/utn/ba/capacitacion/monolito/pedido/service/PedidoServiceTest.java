package ar.edu.utn.ba.capacitacion.monolito.pedido.service;

import ar.edu.utn.ba.capacitacion.monolito.cliente.entity.ClienteEntity;
import ar.edu.utn.ba.capacitacion.monolito.cliente.service.ClienteService;
import ar.edu.utn.ba.capacitacion.monolito.pedido.dto.CrearPedidoRequest;
import ar.edu.utn.ba.capacitacion.monolito.pedido.dto.PedidoResponse;
import ar.edu.utn.ba.capacitacion.monolito.pedido.entity.PedidoEntity;
import ar.edu.utn.ba.capacitacion.monolito.pedido.exception.ClienteInvalidoException;
import ar.edu.utn.ba.capacitacion.monolito.pedido.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private ClienteService clienteService;

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private PedidoService pedidoService;

    private CrearPedidoRequest request;

    @BeforeEach
    void setUp() {
        request = new CrearPedidoRequest(1L, "Pedido de prueba");
    }

    @Test
    @DisplayName("Debe registrar el pedido cuando el cliente existe y está activo")
    void debeRegistrarPedidoCuandoClienteExisteYEstaActivo() {
        ClienteEntity clienteActivo = new ClienteEntity(1L, "Juan Perez", true);
        PedidoEntity pedidoGuardado = new PedidoEntity(10L, 1L, "Pedido de prueba");

        when(clienteService.buscarPorId(1L)).thenReturn(Optional.of(clienteActivo));
        when(pedidoRepository.save(any(PedidoEntity.class))).thenReturn(pedidoGuardado);

        PedidoResponse response = pedidoService.crearPedido(request);

        assertNotNull(response);
        assertEquals(10L, response.id());
        verify(pedidoRepository, times(1)).save(any(PedidoEntity.class));
    }

    @Test
    @DisplayName("Debe rechazar el pedido cuando el cliente no existe")
    void debeRechazarPedidoCuandoClienteNoExiste() {
        when(clienteService.buscarPorId(1L)).thenReturn(Optional.empty());

        ClienteInvalidoException exception = assertThrows(
                ClienteInvalidoException.class,
                () -> pedidoService.crearPedido(request)
        );

        assertTrue(exception.getMessage().contains("no existe"));
        verify(pedidoRepository, never()).save(any(PedidoEntity.class));
    }

    @Test
    @DisplayName("Debe rechazar el pedido cuando el cliente está inactivo")
    void debeRechazarPedidoCuandoClienteEstaInactivo() {
        ClienteEntity clienteInactivo = new ClienteEntity(1L, "Maria Gomez", false);
        when(clienteService.buscarPorId(1L)).thenReturn(Optional.of(clienteInactivo));

        ClienteInvalidoException exception = assertThrows(
                ClienteInvalidoException.class,
                () -> pedidoService.crearPedido(request)
        );

        assertTrue(exception.getMessage().contains("inactivo"));
        verify(pedidoRepository, never()).save(any(PedidoEntity.class));
    }
}

