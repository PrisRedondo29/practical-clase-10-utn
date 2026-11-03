package ar.edu.utn.ba.capacitacion.monolito.pedido.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ClienteServicioNoDisponibleException extends RuntimeException {

    public ClienteServicioNoDisponibleException(String message) {
        super(message);
    }

    public ClienteServicioNoDisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}