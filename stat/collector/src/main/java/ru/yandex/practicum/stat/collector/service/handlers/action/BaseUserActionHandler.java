package ru.yandex.practicum.stat.collector.service.handlers.action;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.stats.event.UserActionProto;
import ru.yandex.practicum.stat.collector.service.KafkaProducerService;

import java.time.Instant;

@Slf4j
@Component
public class BaseUserActionHandler<T extends SpecificRecordBase> implements UserActionHandler<T> {

    private final KafkaProducerService kafkaProducerService;
    private final String topic;

    protected BaseUserActionHandler(KafkaProducerService kafkaProducerService, String userActionTopic) {
        this.kafkaProducerService = kafkaProducerService;
        this.topic = userActionTopic;
    }

    @Override
    public void handle(UserActionProto userAction) {
        log.info("Полученно действие пользователя от контролера {}", userAction);
        Instant userActionTimestamp = Instant.ofEpochSecond(
                userAction.getTimestamp().getSeconds(),
                userAction.getTimestamp().getNanos()
        );
        UserActionAvro result = UserActionAvro.newBuilder()
                .setEventId(userAction.getEventId())
                .setUserId(userAction.getUserId())
                .setActionType(getActionType(userAction))
                .setTimestamp(userActionTimestamp)
                .build();
        log.info("Подготовленно Avro-сообщение для передачи в Kafka {}", result);
        kafkaProducerService.sendToKafka(topic, userAction.getEventId(), result);
    }

    private ActionTypeAvro getActionType(UserActionProto userAction) {
        return switch (userAction.getActionType()) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            default -> throw new IllegalStateException("Wrong action type " + userAction.getActionType());
        };
    }
}

