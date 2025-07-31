package ru.yandex.practicum.core.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.core.interaction.request.dto.ParticipationRequestDto;
import ru.yandex.practicum.core.request.service.RequestService;

import java.util.List;
import java.util.Map;

import static ru.yandex.practicum.core.interaction.request.constants.RequestConstants.*;


@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public final class RequestController {

    private final RequestService requestService;

    @GetMapping(USERS + REQUEST_BASE_PATH)
    public List<ParticipationRequestDto> getUserRequests(@PathVariable(USER_ID) Long userId) {
        return requestService.getUserRequests(userId);
    }

    @PostMapping(USERS + REQUEST_BASE_PATH)
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createUserRequest(@PathVariable(USER_ID) Long userId,
                                                     @RequestParam Long eventId) {
        log.info("Creating request for user with ID: {} for event ID: {}", userId, eventId);
        return requestService.createUserRequest(userId, eventId);
    }


    @PatchMapping(USERS + REQUEST_BASE_PATCH_PATH)
    public ParticipationRequestDto cancelUserRequest(@PathVariable(USER_ID) Long userId,
                                                     @PathVariable(REQUEST_ID) Long requestId) {
        log.info("Cancelling request with ID: {} for user with ID: {}", requestId, userId);
        return requestService.cancelUserRequest(userId, requestId);

    }

    @GetMapping(INTERACTION_API_PREFIX + GET_USER_REQUEST_API_PREFIX)
    public ParticipationRequestDto getUserRequest(@PathVariable(USER_ID) Long userId,
                                                  @PathVariable(EVENT_ID) Long eventId) {
        return requestService.getUserRequest(userId, eventId);
    }

    @GetMapping(INTERACTION_API_PREFIX + EVENT_REQUESTS_API_PREFIX)
    public List<ParticipationRequestDto> getEventRequests(@PathVariable(EVENT_ID) Long eventId) {
        return requestService.getEventRequests(eventId);
    }

    @GetMapping(INTERACTION_API_PREFIX)
    public List<ParticipationRequestDto> getRequestsByIds(@RequestParam(name = "requestIds", required = false)
                                                              List<Long> requestIds) {
        return requestService.getRequestsByIds(requestIds);
    }

    @PostMapping(INTERACTION_API_PREFIX)
    public void updateRequests(@RequestBody List<ParticipationRequestDto> request) {
        requestService.updateRequests(request);
    }

    @GetMapping(INTERACTION_API_PREFIX + CONFIRMED_COUNT)
    public Map<Long, Long> getConfirmedRequestsCount(@RequestParam("eventIds") List<Long> eventIds) {
        log.info("Request to get confirmed request count whit events IDs: {}", eventIds);
        return requestService.getConfirmedRequestsCount(eventIds);
    }
}
