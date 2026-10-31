package ar.edu.utn.ba.capacitacion.monolito.pedido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearPedidoRequest(
        @NotNull(message = "El identificador de cliente es obligatorio")
        Long clienteId,

        @NotBlank(message = "La descripción del pedido es obligatoria")
        String descripcion
) {
}

