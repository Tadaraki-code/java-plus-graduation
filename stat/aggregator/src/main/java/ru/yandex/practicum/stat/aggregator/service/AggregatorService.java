package ru.yandex.practicum.stat.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.stat.aggregator.params.ProcessUserActionResult;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;


@Slf4j
@Service
public class AggregatorService {
    //Map<Event, Map<User, Weight>>
    private final Map<Long, Map<Long, Double>> userActionWeight;
    //Map<Event, Map<Event, S_min>>
    private final Map<Long, Map<Long, Double>> minWeightsSums;
    //ключ мероприятие, значение сумма весов действий пользовтатея с ними
    private final Map<Long, Double> commonWeightsSums;

    public AggregatorService() {
        this.userActionWeight = new HashMap<>();
        this.commonWeightsSums = new HashMap<>();
        this.minWeightsSums = new HashMap<>();
    }


    public List<EventSimilarityAvro> calculateEventsSimilarity(UserActionAvro action) {
        List<EventSimilarityAvro> similarities = new ArrayList<>();
        log.info("Получены следующи данные о действии пользователя {}", action);
        ProcessUserActionResult oldWeight = processUserAction(action);
        if (!oldWeight.getResult()) {
            log.info("Пересчёт не требуеться, вес полученого действия ниже чем хронимый.");
            return similarities;
        }
        updateCommonWeightsSums(action.getEventId(), action.getUserId(), oldWeight.getOldWeight());
        updateMinWeightsSums(action.getEventId(), action.getUserId(), oldWeight.getOldWeight());

        Instant timestamp = LocalDateTime.now().toInstant(ZoneOffset.UTC);

        Long userId = action.getUserId();
        for (Long otherEventId : userActionWeight.keySet()) {
            if (!otherEventId.equals(action.getEventId())) {
                Map<Long, Double> otherEventVector = userActionWeight.get(otherEventId);
                if (otherEventVector != null && otherEventVector.containsKey(userId)) {
                    Double sim = calculatePairSimilarity(action.getEventId(), otherEventId);
                    EventSimilarityAvro eventSimilarityAvro = EventSimilarityAvro.newBuilder()
                            .setEventA(Math.min(action.getEventId(), otherEventId))
                            .setEventB(Math.max(action.getEventId(), otherEventId))
                            .setScore(sim)
                            .setTimestamp(timestamp)
                            .build();
                    log.info("Рассчитано сходство между двумя мероприятиями {}", eventSimilarityAvro);
                    similarities.add(eventSimilarityAvro);
                } else {
                    log.info("Пропущен расчёт для пары {} и {}, так как пользователь {} не взаимодействовал с событием {}",
                            action.getEventId(), otherEventId, userId, otherEventId);
                }
            }
        }
        return similarities;
    }

    private ProcessUserActionResult processUserAction(UserActionAvro action) {
        Long eventId = action.getEventId();
        Long userId = action.getUserId();
        double newWeight = getActionWeight(action.getActionType());

        Map<Long, Double> userWeight = userActionWeight.computeIfAbsent(eventId, k -> new HashMap<>());
        Double oldWeight = userWeight.get(userId);

        if (oldWeight != null && oldWeight >= newWeight) {
            return new ProcessUserActionResult(false, 0.0);
        }

        userWeight.put(userId, newWeight);
        return new ProcessUserActionResult(true, oldWeight);
    }

    private void updateCommonWeightsSums(Long eventId, Long userId, Double oldWeight) {
        Map<Long, Double> eventVector = userActionWeight.get(eventId);
        if (eventVector == null || eventVector.isEmpty()) {
            commonWeightsSums.put(eventId, 0.0);
            return;
        }

        if (commonWeightsSums.containsKey(eventId)) {
            if (oldWeight != null) {
                double delta = eventVector.get(userId) - oldWeight;
                double commonWeightSum = commonWeightsSums.get(eventId) + delta;
                commonWeightsSums.put(eventId, commonWeightSum);
            } else {
                double commonWeightSum = commonWeightsSums.get(eventId) + eventVector.get(userId);
                commonWeightsSums.put(eventId, commonWeightSum);
            }
        } else {
            double commonWightSum = eventVector.values().stream().mapToDouble(w -> w).sum();
            commonWeightsSums.put(eventId, commonWightSum);
        }
    }

    private void updateMinWeightsSums(Long eventId, Long userId, Double oldWeight) {
        Map<Long, Double> eventVector = userActionWeight.get(eventId);
        if (eventVector == null || !eventVector.containsKey(userId)) return;
        Double newWeight = eventVector.get(userId);

        for (Map.Entry<Long, Map<Long, Double>> entry : userActionWeight.entrySet()) {
            Long otherEventId = entry.getKey();
            if (otherEventId.equals(eventId)) {
                continue;
            }

            Map<Long, Double> otherVector = entry.getValue();
            if (!otherVector.containsKey(userId)) {
                continue;
            }

            Double otherWeight = otherVector.get(userId);

            if (isAlreadyCalculated(eventId, otherEventId)) {
                Double oldMin = oldWeight != null ? Math.min(oldWeight, otherWeight) : 0.0;
                Double newMin = Math.min(newWeight, otherWeight);

                if (Double.compare(oldMin, newMin) != 0) {
                    Double delta = newMin - oldMin;
                    Double currentSum = get(eventId, otherEventId);
                    put(eventId, otherEventId, currentSum + delta);
                }
            } else {
                Double sum = 0.0;
                for (Long u : eventVector.keySet()) {
                    if (otherVector.containsKey(u)) {
                        sum += Math.min(eventVector.get(u), otherVector.get(u));
                    }
                }
                put(eventId, otherEventId, sum);
            }
        }
    }

    private double getActionWeight(ActionTypeAvro avro) {
        return switch (avro) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }

    private boolean isAlreadyCalculated(Long eventA, Long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        return minWeightsSums.containsKey(first) && minWeightsSums.get(first).containsKey(second);
    }

    private void put(long eventA, long eventB, Double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        minWeightsSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .put(second, sum);
    }

    private double get(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return minWeightsSums.getOrDefault(first, Collections.emptyMap())
                .getOrDefault(second, 0.0);
    }

    private Double calculatePairSimilarity(Long eventA, Long eventB) {
        double minSum = get(eventA, eventB);

        Double commonVectorSumA = Math.sqrt(commonWeightsSums.getOrDefault(eventA, 0.0));
        Double commonVectorSumB = Math.sqrt(commonWeightsSums.getOrDefault(eventB, 0.0));

        log.info("Считаем сходство для пары: {} и {} -> minSum={}, sums=({}, {})",
                eventA, eventB, minSum, commonVectorSumA, commonVectorSumB);

        if (commonVectorSumA == 0.0 || commonVectorSumB == 0.0) {
            return 0.0;
        }

        return minSum / (commonVectorSumA * commonVectorSumB);
    }
}
