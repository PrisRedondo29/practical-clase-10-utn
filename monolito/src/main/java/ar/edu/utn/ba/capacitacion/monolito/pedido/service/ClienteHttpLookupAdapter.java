package ar.edu.utn.ba.capacitacion.monolito.pedido.service;

import ar.edu.utn.ba.capacitacion.monolito.pedido.dto.ClienteResumen;
import ar.edu.utn.ba.capacitacion.monolito.pedido.exception.ClienteNoDisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Optional;

@Component
public class ClienteHttpLookupAdapter implements ClienteLookup {

    private final RestClient restClient;

    public ClienteHttpLookupAdapter(
            RestClient.Builder builder,
            @Value("${clientes.service.url}") String baseUrl,
            @Value("${clientes.service.connect-timeout-ms}") long connectTimeoutMs,
            @Value("${clientes.service.read-timeout-ms}") long readTimeoutMs) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        this.restClient = builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public Optional<ClienteResumen> buscarPorId(Long id) {
        try {
            ClienteResumen cliente = restClient.get()
                    .uri("/api/clientes/{id}", id)
                    .retrieve()
                    .body(ClienteResumen.class);
            if (cliente == null) {
                throw new ClienteNoDisponibleException(
                        "El servicio de Clientes devolvió una respuesta vacía para el cliente con ID " + id + ".");
            }
            return Optional.of(cliente);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            throw new ClienteNoDisponibleException(
                    "No fue posible consultar el servicio de Clientes para el cliente con ID " + id + ".", e);
        }
    }
}