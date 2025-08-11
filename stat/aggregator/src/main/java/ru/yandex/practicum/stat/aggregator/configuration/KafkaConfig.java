package ru.yandex.practicum.stat.aggregator.configuration;

import kafka.deserializer.UserActionDeserializer;
import kafka.serializer.GeneralAvroSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;

@Slf4j
@Configuration
@ConfigurationProperties("aggregator.kafka")
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
    public KafkaConsumer<Long, UserActionAvro> kafkaConsumer(
            @Value("${kafka.bootstrap-servers}") String bootstrapServers) {
        Properties props = new Properties();
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "AggregatorConsumer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "aggregator-group");
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserActionDeserializer.class.getName());

        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 3072000);
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 307200);

        KafkaConsumer<Long, UserActionAvro> consumer = new KafkaConsumer<>(props);

        log.info("Создан KafkaConsumer: {}", bootstrapServers);

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

