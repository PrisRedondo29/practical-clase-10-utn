package ar.edu.utn.ba.capacitacion.monolito.pedido.service;

import ar.edu.utn.ba.capacitacion.monolito.pedido.dto.ClienteResumen;

import java.util.Optional;

public interface ClienteLookup {

    /**
     * @return el cliente, o vacío si no existe.
     * @throws ar.edu.utn.ba.capacitacion.monolito.pedido.exception.ClienteNoDisponibleException
     *         ante una falla técnica al consultar (5xx, timeout, conexión).
     */
    Optional<ClienteResumen> buscarPorId(Long id);
}