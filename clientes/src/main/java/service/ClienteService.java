package service;

import dto.ClienteResponse;
import dto.ClienteDetalleResponse;
import dto.CrearClienteRequest;
import entity.ClienteEntity;
import repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public Optional<ClienteEntity> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<ClienteDetalleResponse> consultarPorId(Long id) {
        return buscarPorId(id)
                .map(c -> new ClienteDetalleResponse(c.getId(), c.getNombre(), c.isActivo()));
    }

    @Transactional
    public ClienteResponse crearCliente(CrearClienteRequest request) {
        ClienteEntity cliente = new ClienteEntity(request.nombre(), request.activo());
        ClienteEntity guardado = clienteRepository.save(cliente);
        return new ClienteResponse(guardado.getId());
    }
}

