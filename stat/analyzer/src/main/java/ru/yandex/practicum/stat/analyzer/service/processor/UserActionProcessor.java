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
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.stat.analyzer.service.UserActionService;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionProcessor implements Runnable {
    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private final KafkaConsumer<Long, UserActionAvro> kafkaUserActionsConsumer;
    private final String actionsTopic;
    private final UserActionService userActionService;

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaUserActionsConsumer::wakeup));
        try {
            kafkaUserActionsConsumer.subscribe(List.of(actionsTopic));
            while (true) {
                ConsumerRecords<Long, UserActionAvro> records = kafkaUserActionsConsumer.poll(Duration.ofSeconds(4));

                int count = 0;
                for (ConsumerRecord<Long, UserActionAvro> record : records) {
                    handleRecord(record);
                    manageOffsets(record, count, kafkaUserActionsConsumer);
                    count++;
                }
                kafkaUserActionsConsumer.commitAsync();
            }
        } catch (WakeupException ignored) {
            log.info("Споймано WakeupException в HubEventProcessor {}", ignored.getMessage());
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от хаба", e);
        } finally {

            try {
                kafkaUserActionsConsumer.commitSync(currentOffsets);

            } finally {
                log.info("Закрываем хаб консьюмер");
                kafkaUserActionsConsumer.close();
            }
        }
    }

    private static void manageOffsets(ConsumerRecord<Long, UserActionAvro> record,
                                      int count, KafkaConsumer<Long, UserActionAvro> consumer) {
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

    private void handleRecord(ConsumerRecord<Long, UserActionAvro> record) {
        userActionService.handleUserAction(record.value());
    }
}
