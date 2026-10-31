package ar.edu.utn.ba.capacitacion.monolito.cliente.controller;

import ar.edu.utn.ba.capacitacion.monolito.cliente.dto.ClienteResponse;
import ar.edu.utn.ba.capacitacion.monolito.cliente.dto.CrearClienteRequest;
import ar.edu.utn.ba.capacitacion.monolito.cliente.service.ClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteService clienteService;

    @Test
    @DisplayName("POST /api/clientes debe retornar 201 y el id generado con datos válidos")
    void debeCrearClienteConDatosValidos() throws Exception {
        CrearClienteRequest request = new CrearClienteRequest("Carlos Lopez", true);
        when(clienteService.crearCliente(any(CrearClienteRequest.class)))
                .thenReturn(new ClienteResponse(5L));

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @DisplayName("POST /api/clientes debe retornar 400 cuando el nombre es inválido")
    void debeRechazarCuandoNombreEsInvalido() throws Exception {
        CrearClienteRequest request = new CrearClienteRequest("", true);

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/clientes debe retornar 400 cuando activo es nulo")
    void debeRechazarCuandoActivoEsNulo() throws Exception {
        String payloadJson = "{\"nombre\":\"Carlos Lopez\",\"activo\":null}";

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadJson))
                .andExpect(status().isBadRequest());
    }
}

