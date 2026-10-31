package ar.edu.utn.ba.capacitacion.monolito.cliente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearClienteRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotNull(message = "El estado activo es obligatorio")
        Boolean activo
) {
}

