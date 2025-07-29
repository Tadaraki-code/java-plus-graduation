package ru.yandex.practicum.core.request.service;

import ru.yandex.practicum.core.interaction.request.dto.ParticipationRequestDto;

import java.util.List;
import java.util.Map;


public interface RequestService {

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto createUserRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelUserRequest(Long userId, Long requestId);

    ParticipationRequestDto getUserRequest(Long requestorId, Long eventId);

    List<ParticipationRequestDto> getEventRequests(Long eventId);

    List<ParticipationRequestDto> getRequestsByIds(List<Long> requestIds);

    void updateRequests(List<ParticipationRequestDto> request);

    Map<Long, Long> getConfirmedRequestsCount(List<Long> eventIds);

}
