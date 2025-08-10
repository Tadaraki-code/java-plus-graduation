package ru.yandex.practicum.stat.analyzer.controller;

import io.grpc.stub.StreamObserver;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.grpc.stats.event.*;
import ru.yandex.practicum.stat.analyzer.service.EventRecommendationsService;
import ru.yandex.practicum.stat.analyzer.service.EventSimilarityService;
import stats.service.analyzer.AnalyzerControllerGrpc;


@Slf4j
@GrpcService
@RequiredArgsConstructor
public class AnalyzerController extends AnalyzerControllerGrpc.AnalyzerControllerImplBase {
    private final EventSimilarityService eventSimilarityService;
    private final EventRecommendationsService eventRecommendationsService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            if (request != null) {
                eventRecommendationsService.getRecommendationForUser(request.getUserId(),
                        request.getMaxResults(), responseObserver);
                log.info("Обработка запроса на получие рекомендаций для пользователя: " +
                        "данные {} получены и отправлены в обработчик", request);
            } else {
                log.info("Невозможно обработать запрос на получие рекомендованых мероприятий, данные не получены.");
                throw new ValidationException("Невозможно обработать запрос");
            }
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            if (request != null) {
                eventSimilarityService.getSimilarEvents(request.getUserId(),
                        request.getEventId(), request.getMaxResults(), responseObserver);
                log.info("Обработка запроса на получие похожих мероприятий: " +
                        "данные {} получены и отправлены в обработчик", request);
            } else {
                log.info("Невозможно обработать запрос на получие похожих мероприятий, данные не получены.");
                throw new ValidationException("Невозможно обработать запрос");
            }
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}
