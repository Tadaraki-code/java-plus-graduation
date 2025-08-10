package ru.yandex.practicum.stat.analyzer.service.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.stat.analyzer.service.EventSimilarityService;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityProcessor {
    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private final KafkaConsumer<Long, EventSimilarityAvro> kafkaEventSimilarityConsumer;
    private final String similarityTopic;
    private final EventSimilarityService eventSimilarityService;

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaEventSimilarityConsumer::wakeup));
        try {
            kafkaEventSimilarityConsumer.subscribe(List.of(similarityTopic));
            while (true) {
                ConsumerRecords<Long, EventSimilarityAvro> records = kafkaEventSimilarityConsumer
                        .poll(Duration.ofSeconds(4));

                int count = 0;
                for (ConsumerRecord<Long, EventSimilarityAvro> record : records) {
                    handleRecord(record);
                    manageOffsets(record, count, kafkaEventSimilarityConsumer);
                    count++;
                }
                kafkaEventSimilarityConsumer.commitAsync();
            }
        } catch (WakeupException ignored) {
            log.info("Споймано WakeupException в SnapshotProcessor {}", ignored.getMessage());
        } catch (Exception e) {
            log.error("Ошибка во время обработки снепшота", e);
        } finally {

            try {
                kafkaEventSimilarityConsumer.commitSync(currentOffsets);

            } finally {
                log.info("Закрываем  снэпшот консьюмер");
                kafkaEventSimilarityConsumer.close();
            }
        }
    }

    private static void manageOffsets(ConsumerRecord<Long, EventSimilarityAvro> record,
                                      int count, KafkaConsumer<Long, EventSimilarityAvro> consumer) {
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % 10 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }

    private void handleRecord(ConsumerRecord<Long, EventSimilarityAvro> record) {
        eventSimilarityService.handleEventSimilarity(record.value());
    }

}
