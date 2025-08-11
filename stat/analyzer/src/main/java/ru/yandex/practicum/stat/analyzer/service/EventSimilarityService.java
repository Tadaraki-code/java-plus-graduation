package ru.yandex.practicum.stat.analyzer.service;

import io.grpc.stub.StreamObserver;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.grpc.stats.event.RecommendedEventProto;
import ru.yandex.practicum.stat.analyzer.model.EventSimilarity;
import ru.yandex.practicum.stat.analyzer.repository.EventSimilarityRepository;
import ru.yandex.practicum.stat.analyzer.repository.UserActionRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityService {
    private final EventSimilarityRepository eventSimilarityRepository;
    private final UserActionRepository userActionRepository;

    @Transactional
    public void handleEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        log.info("Обработка сообщения из кафка с eventAId:{} и eventBId:{}", eventSimilarityAvro.getEventA(),
                eventSimilarityAvro.getEventB());
        Optional<EventSimilarity> eventSimilarity = eventSimilarityRepository.findById(
                new EventSimilarity.EventSimilarityId
                        (eventSimilarityAvro.getEventA(),
                                eventSimilarityAvro.getEventB()));


        EventSimilarity similarity;
        if (eventSimilarity.isPresent()) {
            similarity = eventSimilarity.get();
            log.info("Обновление записи с eventAId:{} и eventBId:{}", eventSimilarityAvro.getEventA(),
                    eventSimilarityAvro.getEventB());
            similarity.setScore(eventSimilarityAvro.getScore());
            similarity.setTimestamp(getLocalDateTimeFromAvro(eventSimilarityAvro));
        } else {
            similarity = EventSimilarity.builder()
                    .eventAId(eventSimilarityAvro.getEventA())
                    .eventBId(eventSimilarityAvro.getEventB())
                    .score(eventSimilarityAvro.getScore())
                    .timestamp(getLocalDateTimeFromAvro(eventSimilarityAvro))
                    .build();
            log.info("Создание новой записи с eventAId:{} и eventBId:{}", eventSimilarityAvro.getEventA(),
                    eventSimilarityAvro.getEventB());
        }
        eventSimilarityRepository.save(similarity);
    }


    @Transactional(readOnly = true)
    public void getSimilarEvents(Long userId, Long eventId, long maxResults,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("Обработка запроса для userId:{}, evetnId:{}, maxResult:{}", userId, eventId, maxResults);

        List<EventSimilarity> sim = eventSimilarityRepository.findByEventAIdOrEventBId(eventId);
        if (sim.isEmpty()) {
            log.info("Не найдены похожие мероприятия для eventId:{}", eventId);
            responseObserver.onCompleted();
            return;
        }

        List<EventSimilarity> unseenEvents = sim.stream().filter(s -> {
            Long relatedEvent = s.getEventAId().equals(eventId) ? s.getEventBId() : s.getEventAId();
            boolean check = userActionRepository.existsByUserIdAndEventId(userId, relatedEvent);
            return !check;
        }).toList();

        if (unseenEvents.isEmpty()) {
            log.info("Со всеми  похожими мероприятия пользователь с id:{} уже взаимодействова", userId);
            responseObserver.onCompleted();
            return;
        }

        unseenEvents.stream()
                .sorted(Comparator.comparingDouble(EventSimilarity::getScore).reversed()) // По score desc
                .limit(maxResults)
                .forEach(s -> {
                    Long relatedEventId = s.getEventAId().equals(eventId) ? s.getEventBId() : s.getEventAId();
                    RecommendedEventProto proto = RecommendedEventProto.newBuilder()
                            .setEventId(relatedEventId)
                            .setScore(s.getScore())
                            .build();
                    responseObserver.onNext(proto);
                });
        responseObserver.onCompleted();
    }

    private LocalDateTime getLocalDateTimeFromAvro(EventSimilarityAvro eventSimilarityAvro) {
        return eventSimilarityAvro.getTimestamp()
                .atZone(ZoneId.of("UTC"))
                .toLocalDateTime();
    }
}