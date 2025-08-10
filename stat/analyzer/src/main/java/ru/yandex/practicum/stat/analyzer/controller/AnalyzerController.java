package ru.yandex.practicum.stat.analyzer.controller;

import io.grpc.stub.StreamObserver;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.grpc.stats.analyzer.AnalyzerControllerGrpc;
import ru.practicum.grpc.stats.event.*;
import ru.yandex.practicum.stat.analyzer.service.EventRecommendationsService;
import ru.yandex.practicum.stat.analyzer.service.EventSimilarityService;
import ru.yandex.practicum.stat.analyzer.service.UserActionService;



@Slf4j
@GrpcService
@RequiredArgsConstructor
public class AnalyzerController extends AnalyzerControllerGrpc.AnalyzerControllerImplBase {
    EventSimilarityService eventSimilarityService;
    UserActionService userActionService;
    EventRecommendationsService eventRecommendationsService;

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

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            if (request != null && !request.getEventIdsList().isEmpty()) {
                userActionService.getInteractionsCount(request.getEventIdsList(),
                        responseObserver);
                log.info("Запрос на получение суммы весов для событий: {} получен и отправлен в обработчик", request);
            } else {
                log.info("Невозможно обработать запрос на получение суммы весов для событий, данные не получены.");
                throw new ValidationException("Невозможно обработать запрос");
            }
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}
