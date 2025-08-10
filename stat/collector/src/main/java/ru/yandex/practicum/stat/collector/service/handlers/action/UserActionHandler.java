package ru.yandex.practicum.stat.collector.service.handlers.action;

import ru.practicum.grpc.stats.event.UserActionProto;

public interface UserActionHandler<T> {
    void handle(UserActionProto sensorEvent);
}
