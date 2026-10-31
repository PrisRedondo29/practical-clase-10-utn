package ar.edu.utn.ba.capacitacion.monolito.cliente.service;

import ar.edu.utn.ba.capacitacion.monolito.cliente.dto.ClienteResponse;
import ar.edu.utn.ba.capacitacion.monolito.cliente.dto.CrearClienteRequest;
import ar.edu.utn.ba.capacitacion.monolito.cliente.entity.ClienteEntity;
import ar.edu.utn.ba.capacitacion.monolito.cliente.repository.ClienteRepository;
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
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    @DisplayName("Debe persistir el cliente y retornar el identificador generado")
    void debeCrearClienteCorrectamente() {
        CrearClienteRequest request = new CrearClienteRequest("Carlos Lopez", true);
        ClienteEntity entidadGuardada = new ClienteEntity(5L, "Carlos Lopez", true);

        when(clienteRepository.save(any(ClienteEntity.class))).thenReturn(entidadGuardada);

        ClienteResponse response = clienteService.crearCliente(request);

        assertNotNull(response);
        assertEquals(5L, response.id());
        verify(clienteRepository, times(1)).save(any(ClienteEntity.class));
    }

    @Test
    @DisplayName("Debe buscar cliente por id en el repositorio")
    void debeBuscarClientePorId() {
        ClienteEntity cliente = new ClienteEntity(1L, "Juan Perez", true);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        Optional<ClienteEntity> resultado = clienteService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Juan Perez", resultado.get().getNombre());
        verify(clienteRepository, times(1)).findById(1L);
    }
}

