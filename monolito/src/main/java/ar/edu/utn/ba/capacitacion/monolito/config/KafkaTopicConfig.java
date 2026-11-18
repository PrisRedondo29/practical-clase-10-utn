package ar.edu.utn.ba.capacitacion.monolito.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic pedidosCreadoTopic(
            @Value("${app.kafka.topics.pedidos-creado}") String pedidosCreadoTopic) {
        return TopicBuilder.name(pedidosCreadoTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
