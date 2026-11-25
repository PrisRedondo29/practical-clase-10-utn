package ar.edu.utn.ba.capacitacion.monolito.pedido.controller;

import ar.edu.utn.ba.capacitacion.monolito.pedido.dto.CrearPedidoRequest;
import ar.edu.utn.ba.capacitacion.monolito.pedido.dto.PedidoResponse;
import ar.edu.utn.ba.capacitacion.monolito.pedido.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final String instanceId;

    public PedidoController(
            PedidoService pedidoService,
            @Value("${app.instance.id:local}") String instanceId) {
        this.pedidoService = pedidoService;
        this.instanceId = instanceId;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> crearPedido(@Valid @RequestBody CrearPedidoRequest request) {
        PedidoResponse response = pedidoService.crearPedido(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("X-Instance-Id", instanceId)
                .body(response);
    }
}

