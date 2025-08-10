package ru.practicum.ewm.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.grpc.stats.event.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.event.RecommendedEventProto;
import ru.practicum.grpc.stats.event.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.event.UserPredictionsRequestProto;
import stats.service.analyzer.AnalyzerControllerGrpc;
import stats.service.dashboard.RecommendationsControllerGrpc;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class AnalyzerClient {

    private final AnalyzerControllerGrpc.AnalyzerControllerBlockingStub analyzerClient;
    private final RecommendationsControllerGrpc.RecommendationsControllerBlockingStub recommendationsClient;

    public AnalyzerClient(@net.devh.boot.grpc.client.inject.GrpcClient("analyzer")
                          AnalyzerControllerGrpc.AnalyzerControllerBlockingStub analyzerClient,
                          @net.devh.boot.grpc.client.inject.GrpcClient("analyzer")
                          RecommendationsControllerGrpc.RecommendationsControllerBlockingStub recommendationsClient) {
        this.analyzerClient = analyzerClient;
        this.recommendationsClient =  recommendationsClient;
    }

    public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();

        log.info("Запрос отправлен в Analyzer отправлен: eventId:{}, userId:{}, maxResult:{}", eventId,
                userId, maxResults);
        try {
            Iterator<RecommendedEventProto> iterator = analyzerClient.getSimilarEvents(request);
            return asStream(iterator);
        } catch (io.grpc.StatusRuntimeException e) {
            log.error("Ошибка при вызове getSimilarEvents", e);
            return Stream.empty();
        }
    }

    public Stream<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        InteractionsCountRequestProto request = InteractionsCountRequestProto
                .newBuilder()
                .addAllEventIds(eventIds)
                .build();

        log.info("Отправлен запрос для получения суммы весов действия для событий {}", eventIds);

        try {
            Iterator<RecommendedEventProto> iterator = recommendationsClient.getInteractionsCount(request);
            return asStream(iterator);
        } catch (io.grpc.StatusRuntimeException e) {
            log.error("Ошибка при вызове getInteractionsCount", e);
            return Stream.empty();
        }
    }

    public Stream<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResult) {
        UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResult)
                .build();

        log.info("Отправлен запрос на получение рекомендаций для пользователя :{}", userId);
        try {
            Iterator<RecommendedEventProto> iterator = analyzerClient.getRecommendationsForUser(request);
            return asStream(iterator);
        } catch (io.grpc.StatusRuntimeException e) {
            log.error("Ошибка при вызове getRecommendationsForUser", e);
            return Stream.empty();
        }
    }

    private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }
}
