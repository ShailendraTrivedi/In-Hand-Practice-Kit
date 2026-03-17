package com.kafka.config;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.config.TopicBuilder;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 * Central Kafka configuration for the application.
 *
 * For now, we use simple String key and String value messages.
 * Later, we can switch to JSON-based message values (Order DTOs).
 */
@Configuration
public class KafkaConfig {

    // Read bootstrap servers from application.yml
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public NewTopic ordersTopic() {
        return TopicBuilder.name("orders")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Base producer configuration map.
     * This is where we define how the producer connects and serializes data.
     */
    private Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();

        // Address of the Kafka broker(s)
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Key and value serializers (how Java objects are converted to bytes)
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return props;
    }

    /**
     * ProducerFactory is used by Spring to create Kafka producers.
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    /**
     * KafkaTemplate is the main abstraction for sending messages to Kafka.
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Explicit ObjectMapper bean so services can inject it.
     * Spring Boot would normally auto-configure this via web/json starters,
     * but we define it here to make the dependency explicit per microservice.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}