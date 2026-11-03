package ar.edu.utn.ba.capacitacion.monolito.pedido.client;

import java.util.Optional;

public interface ClienteLookup {

    /**
     * @return el cliente, o vacío si no existe.
     * @throws ar.edu.utn.ba.capacitacion.monolito.pedido.exception.ClienteServicioNoDisponibleException ante fallas técnicas.
     */
    Optional<ClienteResumen> buscarPorId(Long id);
}