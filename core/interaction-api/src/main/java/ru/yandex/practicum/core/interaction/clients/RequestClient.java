package ru.yandex.practicum.core.interaction.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.core.interaction.config.FeignConfig;
import ru.yandex.practicum.core.interaction.decoders.CommonFeignErrorDecoder;
import ru.yandex.practicum.core.interaction.request.dto.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

import static ru.yandex.practicum.core.interaction.request.constants.RequestConstants.*;

@FeignClient(name = "request-service", configuration = {FeignConfig.class, CommonFeignErrorDecoder.class})
public interface RequestClient {

    @GetMapping(USERS + REQUEST_BASE_PATH)
    List<ParticipationRequestDto> getUserRequests(@PathVariable(USER_ID) Long userId);

    @GetMapping(INTERACTION_API_PREFIX + GET_USER_REQUEST_API_PREFIX)
    ParticipationRequestDto getUserRequest(@PathVariable(USER_ID) Long userId,
                                           @PathVariable(EVENT_ID) Long eventId);

    @GetMapping(INTERACTION_API_PREFIX + EVENT_REQUESTS_API_PREFIX)
    List<ParticipationRequestDto> getEventRequests(@PathVariable(EVENT_ID) Long eventId);

    @GetMapping(INTERACTION_API_PREFIX)
    List<ParticipationRequestDto> getRequestsByIds(@RequestParam("requestIds") List<Long> requestIds);

    @PostMapping(value = INTERACTION_API_PREFIX , consumes = MediaType.APPLICATION_JSON_VALUE)
    void updateRequests(@RequestBody List<ParticipationRequestDto> request);

    @GetMapping(INTERACTION_API_PREFIX + CONFIRMED_COUNT)
    Map<Long, Long> getConfirmedRequestsCount(@RequestParam("eventIds") List<Long> eventIds);
}
