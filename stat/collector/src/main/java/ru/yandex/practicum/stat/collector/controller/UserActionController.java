package ru.yandex.practicum.stat.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.avro.specific.SpecificRecordBase;
import ru.practicum.grpc.stats.collector.UserActionControllerGrpc;
import ru.practicum.grpc.stats.event.UserActionProto;
import ru.yandex.practicum.stat.collector.service.handlers.action.BaseUserActionHandler;


@Slf4j
@GrpcService
public class UserActionController extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final BaseUserActionHandler<SpecificRecordBase> baseBaseUserActionHandler;

    public UserActionController(BaseUserActionHandler<SpecificRecordBase> baseBaseUserActionHandler) {
        this.baseBaseUserActionHandler = baseBaseUserActionHandler;
        ;
    }


    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
            if (request != null) {
                baseBaseUserActionHandler.handle(request);
                log.info("Данные действия: {} получены и отправлены в обработчик", request);
            } else {
                log.info("Невозможно обработать запрос, данные не получены.");
                throw new ValidationException("Невозможно обработать запрос");
            }
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}
