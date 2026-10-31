package ar.edu.utn.ba.capacitacion.monolito.pedido.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ClienteInvalidoException extends RuntimeException {

    public ClienteInvalidoException(String mensaje) {
        super(mensaje);
    }
}

