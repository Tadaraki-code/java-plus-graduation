package ru.yandex.practicum.core.event.service;

import ru.yandex.practicum.core.interaction.comments.dto.CommentShortDto;
import ru.yandex.practicum.core.interaction.event.dto.*;
import ru.yandex.practicum.core.interaction.event.dto.parameters.*;
import ru.yandex.practicum.core.interaction.request.dto.ParticipationRequestDto;

import java.util.List;

public interface EventsService {
    List<EventShortDto> getEventsCreatedByUser(EventsForUserParameters eventsForUserParameters);

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getEventById(Long userId, Long eventId);

    EventFullDto updateEvent(UpdateEventParameters updateEventRequestParams);

    List<ParticipationRequestDto> getRequestsForEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestsForEvent(UpdateRequestsStatusParameters updateParams);

    List<EventFullDto> searchEvents(SearchEventsParameters searchEventsParameters);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventFullDto> searchPublicEvents(SearchPublicEventsParameters searchPublicEventsParameters);

    EventFullDtoWithComments getPublicEventById(Long eventId);

    List<CommentShortDto> getAllEventComments(GetAllCommentsParameters parameters);

    Long getEventsCountByCategoryId(Long catId);

    List<EventShortDto> getEventsShortDtoByIds(List<Long> eventIds);

    Boolean chekEventExistingByIds(List<Long> eventIds);

    EventFullDto getEventById(Long eventId);
}
