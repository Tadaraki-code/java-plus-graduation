package ru.yandex.practicum.core.interaction.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.core.interaction.config.FeignConfig;
import ru.yandex.practicum.core.interaction.decoders.CommonFeignErrorDecoder;
import ru.yandex.practicum.core.interaction.event.dto.EventFullDto;
import ru.yandex.practicum.core.interaction.event.dto.EventShortDto;

import java.util.List;

import static ru.yandex.practicum.core.interaction.event.constants.EventsConstants.*;

@FeignClient(name = "event-service", configuration = {FeignConfig.class, CommonFeignErrorDecoder.class})
public interface EventClient {

    @GetMapping(INTERACTION_API_PREFIX + COUNT + CAT_ID_PATH)
    Long getEventsCountByCategoryId(
            @PathVariable(CAT_ID) Long catId);

    @GetMapping(INTERACTION_API_PREFIX + EVENT_ID_PATH)
    EventFullDto getEventById(@PathVariable(EVENT_ID) Long eventId);

    @GetMapping(INTERACTION_API_PREFIX + SHORT_DTO)
    List<EventShortDto> getEventsShortDtoByIds(@RequestParam("eventIds") List<Long> eventIds);

    @GetMapping(INTERACTION_API_PREFIX)
    Boolean chekEventExistingByIds(@RequestParam("eventIds") List<Long> eventIds);

}
