package ar.edu.utn.ba.capacitacion.monolito.pedido.service;

import ar.edu.utn.ba.capacitacion.monolito.cliente.entity.ClienteEntity;
import ar.edu.utn.ba.capacitacion.monolito.cliente.service.ClienteService;
import ar.edu.utn.ba.capacitacion.monolito.pedido.dto.CrearPedidoRequest;
import ar.edu.utn.ba.capacitacion.monolito.pedido.dto.PedidoResponse;
import ar.edu.utn.ba.capacitacion.monolito.pedido.entity.PedidoEntity;
import ar.edu.utn.ba.capacitacion.monolito.pedido.exception.ClienteInvalidoException;
import ar.edu.utn.ba.capacitacion.monolito.pedido.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    private final ClienteService clienteService;
    private final PedidoRepository pedidoRepository;

    public PedidoService(ClienteService clienteService, PedidoRepository pedidoRepository) {
        this.clienteService = clienteService;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional
    public PedidoResponse crearPedido(CrearPedidoRequest request) {
        ClienteEntity cliente = clienteService.buscarPorId(request.clienteId())
                .orElseThrow(() -> new ClienteInvalidoException("El cliente con ID " + request.clienteId() + " no existe."));

        if (!cliente.isActivo()) {
            throw new ClienteInvalidoException("El cliente con ID " + request.clienteId() + " se encuentra inactivo.");
        }

        PedidoEntity nuevoPedido = new PedidoEntity(cliente.getId(), request.descripcion());
        PedidoEntity pedidoGuardado = pedidoRepository.save(nuevoPedido);

        return new PedidoResponse(pedidoGuardado.getId());
    }
}

