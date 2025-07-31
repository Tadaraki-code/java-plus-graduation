package ru.yandex.practicum.core.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.core.interaction.clients.EventClient;
import ru.yandex.practicum.core.interaction.clients.UserClient;
import ru.yandex.practicum.core.interaction.error.exception.ClientApiException;
import ru.yandex.practicum.core.interaction.error.exception.NotFoundException;
import ru.yandex.practicum.core.interaction.event.dto.EventFullDto;
import ru.yandex.practicum.core.interaction.request.dto.ParticipationRequestDto;
import ru.yandex.practicum.core.interaction.request.enums.RequestStatus;
import ru.yandex.practicum.core.interaction.user.dto.UserDto;
import ru.yandex.practicum.core.request.model.Request;
import ru.yandex.practicum.core.request.params.RequestValidator;
import ru.yandex.practicum.core.request.mapper.RequestMapper;
import ru.yandex.practicum.core.request.repository.RequestRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserClient userClient;
    private final EventClient eventClient;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        return requestRepository.findByRequesterId(userId).stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto createUserRequest(Long userId, Long eventId) {

        EventFullDto event = getEventById(eventId);
        UserDto requester = getUserById(userId);

        RequestStatus status = event.getParticipantLimit() == 0 || !event.getRequestModeration()
                ? RequestStatus.CONFIRMED
                : RequestStatus.PENDING;

        RequestValidator validator = new RequestValidator(event, userId, eventId, requestRepository);
        validator.validate();

        Request newRequest = RequestMapper.toNewRequestEntity(eventId, userId, status);
        return RequestMapper.toRequestDto(requestRepository.save(newRequest));
    }

    @Override
    public ParticipationRequestDto cancelUserRequest(Long userId, Long requestId) {
        Request request = getRequestById(requestId);

        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public ParticipationRequestDto getUserRequest(Long requestorId, Long eventId) {
        Request request = requestRepository.findByRequesterIdAndEventId(requestorId, eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Request not found for user with id %d", requestorId)));
        return RequestMapper.toRequestDto(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventRequests(Long eventId) {
        List<Request> requests = requestRepository.findByEventId(eventId);
        return requests.stream()
                .map(RequestMapper::toRequestDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsByIds(List<Long> requestIds) {
        if (requestIds == null || requestIds.isEmpty()) {
            return List.of();
        }
        List<Request> requests = requestRepository.findAllById(requestIds);
        return requests.stream()
                .map(RequestMapper::toRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public void updateRequests(List<ParticipationRequestDto> requestDto) {
        if (requestDto == null || requestDto.isEmpty()) {
            return;
        }
        List<Request> requests = requestDto.stream().map(RequestMapper::toRequestEntityFromDto).toList();
        requestRepository.saveAll(requests);
    }

    @Transactional(readOnly = true)
    @Override
    public Map<Long, Long> getConfirmedRequestsCount(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new HashMap<>();
        }

        List<Request> confirmedRequests = requestRepository.findByEventIdInAndStatus(
                eventIds, RequestStatus.CONFIRMED
        );

        return confirmedRequests.stream()
                .collect(Collectors.groupingBy(
                        Request::getEventId,
                        Collectors.counting()
                ));
    }

    private Request getRequestById(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Request not found with id %d", requestId)));
    }

    private EventFullDto getEventById(Long eventId) {
        try {
            return eventClient.getEventById(eventId);
        } catch (ClientApiException e) {
            throw new NotFoundException(String.format("Event not found with id %d", eventId));
        }
    }

    private UserDto getUserById(Long userId) {
        try {
            return userClient.getUserById(userId);
        } catch (ClientApiException e) {
            throw new NotFoundException(String.format("User not found with id %d", userId));
        }
    }


}