package ar.edu.utn.ba.capacitacion.monolito.pedido.client;

import ar.edu.utn.ba.capacitacion.monolito.pedido.exception.ClienteServicioNoDisponibleException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class ClienteRestAdapter implements ClienteLookup {

    private final RestClient restClient;

    public ClienteRestAdapter(
            @Value("${clientes.service.base-url}") String baseUrl,
            @Value("${clientes.service.connect-timeout}") Duration connectTimeout,
            @Value("${clientes.service.read-timeout}") Duration readTimeout) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(connectTimeout)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public Optional<ClienteResumen> buscarPorId(Long id) {
        ClienteHttpResponse respuesta;
        try {
            respuesta = restClient.get()
                    .uri("/api/clientes/{id}", id)
                    .retrieve()
                    .body(ClienteHttpResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            // 5xx, otros 4xx, timeout, conexión rechazada, respuesta ilegible
            throw new ClienteServicioNoDisponibleException(
                    "No se pudo consultar el servicio de clientes para el ID " + id + ".", e);
        }

        if (respuesta == null) {
            throw new ClienteServicioNoDisponibleException(
                    "El servicio de clientes devolvió una respuesta vacía para el ID " + id + ".");
        }
        return Optional.of(new ClienteResumen(respuesta.id(), respuesta.activo()));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ClienteHttpResponse(Long id, boolean activo) {
    }
}