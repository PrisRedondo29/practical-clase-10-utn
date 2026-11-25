package ar.edu.utn.ba.capacitacion.monolito.pedido.service;

import ar.edu.utn.ba.capacitacion.monolito.outbox.OutboxRepository;
import ar.edu.utn.ba.capacitacion.monolito.pedido.client.ClienteLookup;
import ar.edu.utn.ba.capacitacion.monolito.pedido.client.ClienteResumen;
import ar.edu.utn.ba.capacitacion.monolito.pedido.dto.CrearPedidoRequest;
import ar.edu.utn.ba.capacitacion.monolito.pedido.dto.PedidoResponse;
import ar.edu.utn.ba.capacitacion.monolito.pedido.entity.PedidoEntity;
import ar.edu.utn.ba.capacitacion.monolito.pedido.evento.PedidoCreado;
import ar.edu.utn.ba.capacitacion.monolito.pedido.exception.ClienteInvalidoException;
import ar.edu.utn.ba.capacitacion.monolito.pedido.repository.PedidoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class PedidoService {

    private final ClienteLookup clienteLookup;
    private final PedidoRepository pedidoRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final String pedidosCreadoTopic;
    private final ApplicationEventPublisher applicationEventPublisher;

    public PedidoService(
            ClienteLookup clienteLookup,
            PedidoRepository pedidoRepository,
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topics.pedidos-creado}") String pedidosCreadoTopic,
            ApplicationEventPublisher applicationEventPublisher) {
        this.clienteLookup = clienteLookup;
        this.pedidoRepository = pedidoRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.pedidosCreadoTopic = pedidosCreadoTopic;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public PedidoResponse crearPedido(CrearPedidoRequest request) {
        ClienteResumen cliente = clienteLookup.buscarPorId(request.clienteId())
                .orElseThrow(() -> new ClienteInvalidoException("El cliente con ID " + request.clienteId() + " no existe."));

        if (!cliente.activo()) {
            throw new ClienteInvalidoException("El cliente con ID " + request.clienteId() + " se encuentra inactivo.");
        }

        PedidoEntity nuevoPedido = new PedidoEntity(cliente.id(), request.descripcion(), request.total());
        PedidoEntity pedidoGuardado = pedidoRepository.save(nuevoPedido);

        PedidoCreado evento = new PedidoCreado(
                UUID.randomUUID().toString(),
                pedidoGuardado.getId(),
                pedidoGuardado.getClienteId(),
                pedidoGuardado.getTotal(),
                Instant.now()
        );

        try {
            outboxRepository.guardar(
                    evento.eventId(),
                    pedidosCreadoTopic,
                    evento.pedidoId().toString(),
                    objectMapper.writeValueAsString(evento)
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar PedidoCreado", e);
        }

        applicationEventPublisher.publishEvent(
                new PedidoConfirmadoInterno(pedidoGuardado.getId())
        );

        return new PedidoResponse(pedidoGuardado.getId());
    }
}