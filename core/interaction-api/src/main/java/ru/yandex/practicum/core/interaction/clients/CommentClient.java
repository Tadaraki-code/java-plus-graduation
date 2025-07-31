package ru.yandex.practicum.core.interaction.clients;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.core.interaction.comments.dto.CommentShortDto;
import ru.yandex.practicum.core.interaction.config.FeignConfig;
import ru.yandex.practicum.core.interaction.decoders.CommonFeignErrorDecoder;

import java.util.List;

import static ru.yandex.practicum.core.interaction.comments.constants.CommentConstants.*;

@FeignClient(name = "comments-service", configuration = {FeignConfig.class, CommonFeignErrorDecoder.class})
public interface CommentClient {

    @GetMapping(INTERACTION_API_PREFIX)
    List<CommentShortDto> getCommentsForEvent(@RequestParam("event-id") Long eventId,
                                              @RequestParam("from") Integer from,
                                              @RequestParam("size") Integer size);

    @GetMapping(INTERACTION_API_PREFIX + COUNT_API_PREFIX)
    List<List<Long>> getCommentsNumberForEvents(@RequestParam("eventIds") List<Long> eventIds);

    @GetMapping(INTERACTION_API_PREFIX + FIRST_API_PREFIX)
    List<CommentShortDto> findFirstCommentsForEvent(@PathVariable(EVENT_ID) Long eventId,
                                                    @RequestParam("size") Long size);

}
