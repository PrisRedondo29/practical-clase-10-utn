package ar.edu.utn.ba.capacitacion.monolito.pedido.service;

import ar.edu.utn.ba.capacitacion.monolito.pedido.dto.ClienteResumen;
import ar.edu.utn.ba.capacitacion.monolito.pedido.dto.CrearPedidoRequest;
import ar.edu.utn.ba.capacitacion.monolito.pedido.dto.PedidoResponse;
import ar.edu.utn.ba.capacitacion.monolito.pedido.entity.PedidoEntity;
import ar.edu.utn.ba.capacitacion.monolito.pedido.exception.ClienteInvalidoException;
import ar.edu.utn.ba.capacitacion.monolito.pedido.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    private final ClienteLookup clienteLookup;
    private final PedidoRepository pedidoRepository;

    public PedidoService(ClienteLookup clienteLookup, PedidoRepository pedidoRepository) {
        this.clienteLookup = clienteLookup;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional
    public PedidoResponse crearPedido(CrearPedidoRequest request) {
        ClienteResumen cliente = clienteLookup.buscarPorId(request.clienteId())
                .orElseThrow(() -> new ClienteInvalidoException("El cliente con ID " + request.clienteId() + " no existe."));

        if (!cliente.activo()) {
            throw new ClienteInvalidoException("El cliente con ID " + request.clienteId() + " se encuentra inactivo.");
        }

        PedidoEntity nuevoPedido = new PedidoEntity(cliente.id(), request.descripcion());
        PedidoEntity pedidoGuardado = pedidoRepository.save(nuevoPedido);

        return new PedidoResponse(pedidoGuardado.getId());
    }
}