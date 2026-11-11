package ar.edu.utn.ba.capacitacion.monolito.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String PEDIDOS_CREADO_TOPIC = "pedidos.creado.v1";

    @Bean
    public NewTopic pedidosCreadoTopic() {
        return TopicBuilder.name(PEDIDOS_CREADO_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
