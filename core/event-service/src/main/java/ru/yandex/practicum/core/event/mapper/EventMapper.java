package ru.yandex.practicum.core.event.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.core.event.model.Event;
import ru.yandex.practicum.core.event.parameters.MappingEventParameters;
import ru.yandex.practicum.core.interaction.event.dto.*;

@Component
public class EventMapper {
    public static Event fromNewEventDto(NewEventDto newEventDto, Long categoryId) {
        return Event.builder()
                .title(newEventDto.getTitle())
                .description(newEventDto.getDescription())
                .annotation(newEventDto.getAnnotation())
                .categoryId(categoryId)
                .locationLat(newEventDto.getLocation().getLat())
                .locationLon(newEventDto.getLocation().getLon())
                .requestModeration(newEventDto.getRequestModeration())
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .eventDate(newEventDto.getEventDate())
                .build();
    }

    public static EventFullDto toEventFullDto(MappingEventParameters eventFullDtoParams) {
        Event event = eventFullDtoParams.getEvent();

        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(eventFullDtoParams.getCategoryDto())
                .confirmedRequests(eventFullDtoParams.getConfirmedRequests())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(eventFullDtoParams.getInitiator())
                .location(new LocationDto(event.getLocationLat(), event.getLocationLon()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .title(event.getTitle())
                .state(event.getEventPublishState())
                .views(eventFullDtoParams.getViews())
                .build();
    }

    public static EventFullDtoWithComments toEventEventFullDtoWithComments(MappingEventParameters eventFullDtoParams) {
        Event event = eventFullDtoParams.getEvent();

        return EventFullDtoWithComments.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(eventFullDtoParams.getCategoryDto())
                .confirmedRequests(eventFullDtoParams.getConfirmedRequests())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(eventFullDtoParams.getInitiator())
                .location(new LocationDto(event.getLocationLat(), event.getLocationLon()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .title(event.getTitle())
                .state(event.getEventPublishState())
                .views(eventFullDtoParams.getViews())
                .comments(eventFullDtoParams.getComments())
                .build();
    }

    public static EventShortDto toEventShortDto(MappingEventParameters eventDtoParams) {
        Event event = eventDtoParams.getEvent();

        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(eventDtoParams.getCategoryDto())
                .confirmedRequests(eventDtoParams.getConfirmedRequests())
                .eventDate(event.getEventDate())
                .initiator(eventDtoParams.getInitiator())
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(eventDtoParams.getViews())
                .build();
    }

    public static UpdateEventCommonRequest userUpdateRequestToCommonRequest(UpdateEventUserRequest request) {
        return UpdateEventCommonRequest.builder()
                .annotation(request.getAnnotation())
                .description(request.getDescription())
                .location(request.getLocation())
                .requestModeration(request.getRequestModeration())
                .participantLimit(request.getParticipantLimit())
                .category(request.getCategory())
                .eventDate(request.getEventDate())
                .paid(request.getPaid())
                .title(request.getTitle())
                .eventDate(request.getEventDate())
                .build();
    }

    public static UpdateEventCommonRequest adminUpdateRequestToCommonRequest(UpdateEventAdminRequest request) {
        return UpdateEventCommonRequest.builder()
                .annotation(request.getAnnotation())
                .description(request.getDescription())
                .location(request.getLocation())
                .requestModeration(request.getRequestModeration())
                .participantLimit(request.getParticipantLimit())
                .category(request.getCategory())
                .eventDate(request.getEventDate())
                .paid(request.getPaid())
                .title(request.getTitle())
                .eventDate(request.getEventDate())
                .build();
    }
}