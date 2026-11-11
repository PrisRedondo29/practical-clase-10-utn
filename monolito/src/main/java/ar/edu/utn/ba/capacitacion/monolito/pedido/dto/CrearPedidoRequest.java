package ar.edu.utn.ba.capacitacion.monolito.pedido.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CrearPedidoRequest(
        @NotNull(message = "El identificador de cliente es obligatorio")
        Long clienteId,

        @NotBlank(message = "La descripción del pedido es obligatoria")
        String descripcion,

        @NotNull(message = "El total del pedido es obligatorio")
        @DecimalMin(value = "0.01", message = "El total del pedido debe ser mayor a cero")
        @Digits(
                integer = 10,
                fraction = 2,
                message = "El total debe tener como máximo 10 dígitos enteros y 2 decimales"
        )
        BigDecimal total
) {
}

