package ru.yandex.practicum.core.event.views;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.clients.AnalyzerClient;
import ru.practicum.grpc.stats.event.RecommendedEventProto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventsRatingGetter {
    private final AnalyzerClient analyzerClient;

    public Map<Long, Double> getRatingMap(List<Long> eventIds) {
        Stream<RecommendedEventProto> eventRating = analyzerClient.getInteractionsCount(eventIds);

        return eventRating
                .collect(Collectors.toMap(RecommendedEventProto::getEventId, RecommendedEventProto::getScore));
    }

}

