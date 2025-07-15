package ru.practicum.ewm.events.service;

import ru.practicum.ewm.comments.dto.CommentShortDto;
import ru.practicum.ewm.events.dto.*;
import ru.practicum.ewm.events.dto.parameters.*;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

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
}
