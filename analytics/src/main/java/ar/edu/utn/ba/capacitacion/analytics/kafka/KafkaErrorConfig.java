package ar.edu.utn.ba.capacitacion.analytics.config;

import ar.edu.utn.ba.capacitacion.analytics.evento.PedidoCreado;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorConfig {

    @Bean
    public NewTopic pedidosCreadoDlt(
            @Value("${app.kafka.topic.pedido-creado}") String pedidosCreadoTopic) {
        return TopicBuilder
                .name(pedidosCreadoTopic + ".DLT")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public DefaultErrorHandler errorHandler(
            KafkaTemplate<String, PedidoCreado> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (registro, error) -> new TopicPartition(
                                registro.topic() + ".DLT",
                                registro.partition()
                        )
                );

        return new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(600L, 2L)
        );
    }
}