package ru.yandex.practicum.stat.analyzer.service;

import io.grpc.stub.StreamObserver;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.stats.event.RecommendedEventProto;
import ru.yandex.practicum.stat.analyzer.model.UserAction;
import ru.yandex.practicum.stat.analyzer.repository.EventSimilarityRepository;
import ru.yandex.practicum.stat.analyzer.repository.UserActionRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionService {
    private final EventSimilarityRepository eventSimilarityRepository;
    private final UserActionRepository userActionRepository;

    @Transactional
    public void handleUserAction(UserActionAvro userActionAvro) {
        log.info("Обработка сообщения из кафка с userId:{} и eventId:{}", userActionAvro.getUserId(),
                userActionAvro.getEventId());
        Optional<UserAction> userAction = userActionRepository.findById(
                new UserAction.UserActionId
                        (userActionAvro.getUserId(),
                                userActionAvro.getEventId()));


        UserAction action;
        if (userAction.isPresent()) {
            action = userAction.get();
            log.info("Обновлнеи уже имеющийся записи, userId:{} и eventId:{}", userActionAvro.getUserId(),
                    userActionAvro.getEventId());
            action.setWeight(Math.max(getActionWeight(userActionAvro.getActionType()), action.getWeight()));
            action.setTimestamp(getLocalDateTimeFromAvro(userActionAvro));
        } else {
            action = UserAction.builder()
                    .userId(userActionAvro.getUserId())
                    .eventId(userActionAvro.getEventId())
                    .weight(getActionWeight(userActionAvro.getActionType()))
                    .timestamp(getLocalDateTimeFromAvro(userActionAvro))
                    .build();
            log.info("Создание новой записи, userId:{} и eventId:{}", userActionAvro.getUserId(),
                    userActionAvro.getEventId());
        }
        userActionRepository.save(action);
    }

    @Transactional(readOnly = true)
    public void getInteractionsCount(List<Long> eventIds,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("Обработка запрос на получение суммы всех взаимодействия для событий{}", eventIds);
        Map<Long, List<UserAction>> actionMap = userActionRepository.findAllByEventIdIn(eventIds).stream()
                .collect(Collectors.groupingBy(
                        UserAction::getEventId));

        for (Long eventId : eventIds) {
            List<UserAction> usersActions = actionMap.getOrDefault(eventId, List.of());
            if (usersActions.isEmpty()) {
                log.info("Не найдены действия для события с id{}", eventId);
            }
            double weightSum = usersActions.stream().mapToDouble(UserAction::getWeight).sum();

            RecommendedEventProto proto = RecommendedEventProto.newBuilder()
                    .setEventId(eventId)
                    .setScore(weightSum)
                    .build();
            log.info("Подсчитаная сумма весов действия {} для события с id:{}", proto.getScore(), eventId);
            responseObserver.onNext(proto);
        }
        responseObserver.onCompleted();
    }

    private LocalDateTime getLocalDateTimeFromAvro(UserActionAvro userActionAvro) {
        return userActionAvro.getTimestamp()
                .atZone(ZoneId.of("UTC"))
                .toLocalDateTime();
    }

    private Double getActionWeight(ActionTypeAvro avro) {
        return switch (avro) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }

}
