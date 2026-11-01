package ar.edu.utn.ba.capacitacion.monolito.pedido.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ClienteNoDisponibleException extends RuntimeException {

    public ClienteNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public ClienteNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}