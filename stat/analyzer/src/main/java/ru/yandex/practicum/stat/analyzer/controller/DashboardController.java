package ru.yandex.practicum.stat.analyzer.controller;

import io.grpc.stub.StreamObserver;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.grpc.stats.event.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.event.RecommendedEventProto;
import ru.yandex.practicum.stat.analyzer.service.UserActionService;
import stats.service.dashboard.RecommendationsControllerGrpc;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class DashboardController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final UserActionService userActionService;

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
