package ru.practicum.ewm.clients;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.grpc.stats.event.ActionTypeProto;
import ru.practicum.grpc.stats.event.UserActionProto;
import stats.service.collector.UserActionControllerGrpc;

import java.time.Instant;

@Slf4j
@Service
public class CollectorClient {

    private final UserActionControllerGrpc.UserActionControllerBlockingStub userActionController;

    public CollectorClient(@net.devh.boot.grpc.client.inject.GrpcClient("collector")
                           UserActionControllerGrpc.UserActionControllerBlockingStub userActionController) {
        this.userActionController = userActionController;
    }

    public void sendToCollector(Long userId, Long eventId, ActionTypeProto action,
                                Instant userActionTimestamp) {
        try {
            Timestamp timestamp = Timestamp.newBuilder()
                    .setSeconds(userActionTimestamp.getEpochSecond())
                    .setNanos(userActionTimestamp.getNano())
                    .build();

            UserActionProto request = UserActionProto.newBuilder()
                    .setUserId(userId)
                    .setEventId(eventId)
                    .setActionType(action)
                    .setTimestamp(timestamp)
                    .build();

            Empty response = userActionController.collectUserAction(request);
            log.info("Действие отправлено: user_id={}, event_id={}, type={}, timestamp={}",
                    userId, eventId, action, userActionTimestamp);
        } catch (Exception e) {
            log.error("gRPC ошибка при отправке действия для пользователя {}", userId);
        }
    }
}
