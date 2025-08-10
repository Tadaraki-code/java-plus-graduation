package ru.yandex.practicum.stat.analyzer.configuration;

import kafka.deserializer.EventSimilarityDeserializer;
import kafka.deserializer.UserActionDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;

@Slf4j
@Configuration
@ConfigurationProperties("analyzer.kafka")
public class KafkaConfig {

    @Bean
    public KafkaConsumer<Long, UserActionAvro> kafkaUserActionsConsumer(
            @Value("${kafka.bootstrap-servers}") String bootstrapServers) {
        Properties props = new Properties();
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "AnalyzerUserActionsConsumer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "analyzer-group");
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserActionDeserializer.class.getName());

        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 3072000);
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 307200);

        KafkaConsumer<Long, UserActionAvro> consumer = new KafkaConsumer<>(props);

        log.info("Создан kafkaUserActionsConsumer: {}", bootstrapServers);

        return consumer;
    }

    @Bean
    public KafkaConsumer<Long, EventSimilarityAvro> kafkaEventSimilarityConsumer(
            @Value("${kafka.bootstrap-servers}") String bootstrapServers) {
        Properties props = new Properties();
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "AnalyzerEventSimilarityConsumer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "similarity-analyzer-group");
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EventSimilarityDeserializer.class.getName());

        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 3072000);
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 307200);

        KafkaConsumer<Long, EventSimilarityAvro> consumer = new KafkaConsumer<>(props);

        log.info("Создан kafkaEventSimilarityConsumer: {}", bootstrapServers);

        return consumer;
    }

    @Bean
    public String actionsTopic(@Value("${kafka.topic.actions}") String actionsTopic) {
        log.info("Настроен топик для действий пользователя: {}", actionsTopic);
        return actionsTopic;
    }

    @Bean
    public String similarityTopic(@Value("${kafka.topic.similarity}") String similarityTopic) {
        log.info("Настроен топик для сходства событий: {}", similarityTopic);
        return similarityTopic;
    }
}
