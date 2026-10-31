package ar.edu.utn.ba.capacitacion.monolito.cliente.service;

import ar.edu.utn.ba.capacitacion.monolito.cliente.entity.ClienteEntity;
import ar.edu.utn.ba.capacitacion.monolito.cliente.repository.ClienteRepository;
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
}

