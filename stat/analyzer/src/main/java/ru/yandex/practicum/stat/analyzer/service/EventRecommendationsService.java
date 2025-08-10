package ru.yandex.practicum.stat.analyzer.service;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.grpc.stats.event.RecommendedEventProto;
import ru.yandex.practicum.stat.analyzer.model.EventSimilarity;
import ru.yandex.practicum.stat.analyzer.model.UserAction;
import ru.yandex.practicum.stat.analyzer.repository.EventSimilarityRepository;
import ru.yandex.practicum.stat.analyzer.repository.UserActionRepository;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class EventRecommendationsService {
    private final EventSimilarityRepository eventSimilarityRepository;
    private final UserActionRepository userActionRepository;

    @Transactional(readOnly = true)
    public void getRecommendationForUser(Long userId, long maxResults,
                                         StreamObserver<RecommendedEventProto> responseObserver) {
        List<UserAction> recentActions = getLastUserActions(userId, maxResults);
        if (recentActions.isEmpty()) {
            log.info("Не найдены взаимодействия для userId={}", userId);
            responseObserver.onCompleted();
            return;
        }

        Set<Long> interactedEvents = recentActions.stream().map(UserAction::getEventId).collect(Collectors.toSet());
        Set<Long> allInteractedEvents = userActionRepository.findEventIdsByUserId(userId);

        List<EventSimilarity> candidateSimilarities = eventSimilarityRepository
                .findByEventAIdOrEventBIdIn(interactedEvents)
                .stream()
                .filter(sim -> {
                    Long related = getRelatedEventId(sim, interactedEvents);
                    return related != null && !allInteractedEvents.contains(related);
                }).toList();

        if (candidateSimilarities.isEmpty()) {
            log.info("Не найдены новые похожие события для userId={}", userId);
            responseObserver.onCompleted();
            return;
        }

        Map<Long, Double> eventMaxScore = new HashMap<>();
        for (EventSimilarity sim : candidateSimilarities) {
            Long related = getRelatedEventId(sim, interactedEvents);
            if (related != null) {
                eventMaxScore.merge(related, sim.getScore(), Double::max);
            }
        }

        List<Long> topCandidateEventIds = eventMaxScore.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(Map.Entry::getKey)
                .toList();

        Map<Long, Double> eventWeights = recentActions.stream()
                .collect(Collectors.toMap(UserAction::getEventId, UserAction::getWeight));

        Map<Long, List<EventSimilarity>> allSimilarEvents = eventSimilarityRepository
                .findByEventAIdOrEventBIdIn(new HashSet<>(topCandidateEventIds))
                .stream()
                .collect(Collectors.groupingBy(sim -> topCandidateEventIds.contains(sim.getEventAId()) ? sim.getEventAId() : sim.getEventBId()));

        for (Long eventId : topCandidateEventIds) {
            List<EventSimilarity> sims = allSimilarEvents.getOrDefault(eventId, List.of()).stream()
                    .filter(sim -> {
                        Long related = getRelatedEventId(sim, interactedEvents);
                        return related != null;
                    })
                    .sorted(Comparator.comparingDouble(EventSimilarity::getScore).reversed())
                    .limit(maxResults)
                    .toList();

            if (sims.isEmpty()) {
                log.info("Нету соседних событий для события {}", eventId);
                continue;
            }

            double totalScore = 0;
            double totalSimilarity = 0;
            for (EventSimilarity sim : sims) {
                Long related = getRelatedEventId(sim, interactedEvents);
                double weight = eventWeights.getOrDefault(related, 0.0);
                double score = sim.getScore();

                totalScore += weight * score;
                totalSimilarity += score;
            }

            double finalScore = totalSimilarity > 0 ? totalScore / totalSimilarity : 0.0;

            responseObserver.onNext(RecommendedEventProto.newBuilder()
                    .setEventId(eventId)
                    .setScore(finalScore)
                    .build());
        }

        responseObserver.onCompleted();
    }

    private List<UserAction> getLastUserActions(Long userId, long maxResult) {
        return userActionRepository.findAllByUserIdOrderByTimestampDesc(userId,
                PageRequest.of(0, (int) maxResult));
    }

    private Long getRelatedEventId(EventSimilarity sim, Set<Long> refEvents) {
        if (refEvents.contains(sim.getEventAId())) return sim.getEventBId();
        if (refEvents.contains(sim.getEventBId())) return sim.getEventAId();
        return null;
    }

}
