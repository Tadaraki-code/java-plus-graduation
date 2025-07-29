package ru.yandex.practicum.core.request.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.core.interaction.request.dto.ParticipationRequestDto;
import ru.yandex.practicum.core.interaction.request.enums.RequestStatus;
import ru.yandex.practicum.core.request.model.Request;

import java.time.LocalDateTime;

@Component
public class RequestMapper {

    public static ParticipationRequestDto toRequestDto(Request request) {

        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEventId())
                .requester(request.getRequesterId())
                .status(request.getStatus().name())
                .build();
    }

    public static Request toRequestEntity(ParticipationRequestDto dto, Long eventId, Long requesterId) {

        return Request.builder()
                .id(dto.getId())
                .created(LocalDateTime.now())
                .eventId(eventId)
                .requesterId(requesterId)
                .status(RequestStatus.valueOf(dto.getStatus()))
                .build();
    }

    public static Request toRequestEntityFromDto(ParticipationRequestDto dto) {

        return Request.builder()
                .id(dto.getId())
                .created(LocalDateTime.now())
                .eventId(dto.getEvent())
                .requesterId(dto.getRequester())
                .status(RequestStatus.valueOf(dto.getStatus()))
                .build();
    }
}