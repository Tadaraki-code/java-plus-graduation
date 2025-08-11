package ru.yandex.practicum.stat.collector.configuration;

import kafka.serializer.GeneralAvroSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Slf4j
@Configuration
@ConfigurationProperties("collector.kafka")
public class KafkaConfig {

    @Bean
    public KafkaProducer<Long, SpecificRecordBase> kafkaProducer(
            @Value("${kafka.bootstrap-servers}") String bootstrapServers) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class.getName());
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");

        KafkaProducer<Long, SpecificRecordBase> producer = new KafkaProducer<>(props);

        log.info("Создан KafkaProducer: {}", bootstrapServers);

        return producer;
    }

    @Bean
    public String userActionTopic(@Value("${kafka.topic.user-actions}") String userActionTopic) {
        log.info("Настроен топик для записи действий пользователя: {}", userActionTopic);
        return userActionTopic;
    }
}
