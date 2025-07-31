package ru.yandex.practicum.core.event.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.core.event.mapper.EventMapper;
import ru.yandex.practicum.core.event.model.Event;
import ru.yandex.practicum.core.event.model.QEvent;
import ru.yandex.practicum.core.event.parameters.MappingEventParameters;
import ru.yandex.practicum.core.event.storage.EventsRepository;
import ru.yandex.practicum.core.event.views.EventsViewsGetter;
import ru.yandex.practicum.core.interaction.category.dto.CategoryDto;
import ru.yandex.practicum.core.interaction.clients.CategoryClient;
import ru.yandex.practicum.core.interaction.clients.CommentClient;
import ru.yandex.practicum.core.interaction.clients.RequestClient;
import ru.yandex.practicum.core.interaction.clients.UserClient;
import ru.yandex.practicum.core.interaction.comments.dto.CommentShortDto;
import ru.yandex.practicum.core.interaction.error.exception.*;
import ru.yandex.practicum.core.interaction.event.dto.*;
import ru.yandex.practicum.core.interaction.event.dto.parameters.*;
import ru.yandex.practicum.core.interaction.event.enums.AdminEventAction;
import ru.yandex.practicum.core.interaction.event.enums.EventPublishState;
import ru.yandex.practicum.core.interaction.event.enums.SortingEvents;
import ru.yandex.practicum.core.interaction.event.enums.UserUpdateRequestAction;
import ru.yandex.practicum.core.interaction.request.dto.ParticipationRequestDto;
import ru.yandex.practicum.core.interaction.request.enums.RequestStatus;
import ru.yandex.practicum.core.interaction.user.dto.UserDto;
import ru.yandex.practicum.core.interaction.user.dto.UserShortDto;
import ru.yandex.practicum.core.interaction.util.Util;

import java.lang.IllegalArgumentException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class EventsServiceImpl implements EventsService {
    private final EventsRepository eventsRepository;
    private final UserClient userClient;
    private final CategoryClient categoryClient;
    private final RequestClient requestClient;
    private final CommentClient commentClient;

    private final EventsViewsGetter eventsViewsGetter;

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsCreatedByUser(EventsForUserParameters eventsForUserParameters) {
        Long userId = eventsForUserParameters.getUserId();
        Integer from = eventsForUserParameters.getFrom();
        Integer size = eventsForUserParameters.getSize();

        checkUserExisting(userId);

        Pageable page = createPageableObject(from, size);
        List<Event> userEvents = eventsRepository.findAllByInitiatorIdIs(userId, page).stream()
                .toList();

        return createEventShortDtoList(userEvents);
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        checkEventDateBeforeHours(newEventDto.getEventDate());
        UserDto user = getUserWithCheck(userId);
        CategoryDto category = getCategoryWithCheck(newEventDto.getCategory());
        Event event = EventMapper.fromNewEventDto(newEventDto, category.getId());
        event.setCreatedOn(Util.getNowTruncatedToSeconds());
        event.setInitiatorId(user.getId());
        return createEventFullDto(eventsRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventById(Long userId, Long eventId) {
        Event event = getEventWithCheck(eventId);
        checkUserRights(userId, event);
        return createEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(UpdateEventParameters updateEventParameters) {
        Long userId = updateEventParameters.getUserId();
        Long eventId = updateEventParameters.getEventId();
        UpdateEventUserRequest updateEventUserRequest = updateEventParameters.getUpdateEventUserRequest();

        Event event = getEventWithCheck(eventId);
        checkUserRights(userId, event);

        if (!canUserUpdateEvent(event)) {
            throw new DataIntegrityViolationException("Only pending or canceled events can be changed.");
        }

        UpdateEventCommonRequest commonRequest = EventMapper.userUpdateRequestToCommonRequest(updateEventUserRequest);
        updateCommonEventProperties(event, commonRequest);

        if (updateEventUserRequest.getStateAction() != null) {
            switch (updateEventUserRequest.getStateAction()) {
                case CANCEL_REVIEW -> event.setEventPublishState(EventPublishState.CANCELED);
                case SEND_TO_REVIEW -> event.setEventPublishState(EventPublishState.PENDING);
            }
        }

        return createEventFullDto(eventsRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsForEvent(Long userId, Long eventId) {
        Event event = getEventWithCheck(eventId);
        checkUserRights(userId, event);
        return requestClient.getEventRequests(eventId);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestsForEvent(UpdateRequestsStatusParameters updateParams) {
        Long userId = updateParams.getUserId();
        Long eventId = updateParams.getEventId();
        EventRequestStatusUpdateRequest statusUpdateRequest = updateParams.getEventRequestStatusUpdateRequest();

        Event event = getEventWithCheck(eventId);
        checkUserRights(userId, event);

        EventRequestStatusUpdateResult result = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(new ArrayList<>())
                .rejectedRequests(new ArrayList<>())
                .build();

        UserUpdateRequestAction action = statusUpdateRequest.getStatus();
        try {
            List<ParticipationRequestDto> requests = requestClient.getRequestsByIds(statusUpdateRequest.getRequestIds());

            Long confirmedRequests = getConfirmedRequestsMap(List.of(eventId)).getOrDefault(eventId, 0L);
            Integer participantLimit = event.getParticipantLimit();

            long canConfirmRequestsNumber = participantLimit == 0
                    ? requests.size()
                    : participantLimit - confirmedRequests;

            if (canConfirmRequestsNumber <= 0) {
                throw new DataIntegrityViolationException(String.format(
                        "Event id=%d is full filled for requests.", eventId
                ));
            }

            requests.forEach(request -> {
                if (!request.getStatus().equals(RequestStatus.PENDING.toString())) {
                    throw new DataIntegrityViolationException(String.format(
                            "Request id=%d must have status PENDING.", request.getId()
                    ));
                }
            });

            for (ParticipationRequestDto request : requests) {
                if (action == UserUpdateRequestAction.REJECTED || canConfirmRequestsNumber <= 0) {
                    request.setStatus(RequestStatus.REJECTED.toString());
                    result.getRejectedRequests().add(request);
                    continue;
                }

                request.setStatus(RequestStatus.CONFIRMED.toString());
                result.getConfirmedRequests().add(request);
                canConfirmRequestsNumber--;
            }
            requestClient.updateRequests(requests);
        } catch (ClientApiException e) {
            if (e.getMessage().equals("CONFLICT")) {
                throw new DataIntegrityViolationException(e.getReason());
            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> searchEvents(SearchEventsParameters searchParams) {
        QEvent event = QEvent.event;
        List<BooleanExpression> conditions = new ArrayList<>();
        Pageable page = createPageableObject(searchParams.getFrom(), searchParams.getSize());

        if (searchParams.getUsers() != null) {
            conditions.add(event.initiatorId.in(searchParams.getUsers()));
        }

        if (searchParams.getStates() != null) {
            List<EventPublishState> states = searchParams.getStates().stream()
                    .map(EventPublishState::valueOf)
                    .toList();
            conditions.add(event.eventPublishState.in(states));
        }

        if (searchParams.getCategories() != null) {
            conditions.add(event.categoryId.in(searchParams.getCategories()));
        }

        if (searchParams.getRangeStart() != null) {
            conditions.add(event.eventDate.after(searchParams.getRangeStart()));
        }

        if (searchParams.getRangeEnd() != null) {
            conditions.add(event.eventDate.before(searchParams.getRangeEnd()));
        }

        BooleanExpression condition = conditions.stream()
                .reduce(Expressions.asBoolean(true).isTrue(), BooleanExpression::and);
        List<Event> resultEvents = eventsRepository.findAll(condition, page).stream()
                .toList();
        return createEventFullDtoList(resultEvents);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = getEventWithCheck(eventId);
        UpdateEventCommonRequest commonRequest = EventMapper.adminUpdateRequestToCommonRequest(updateRequest);
        updateCommonEventProperties(event, commonRequest);

        if (updateRequest.getStateAction() != null) {
            AdminEventAction stateAction = updateRequest.getStateAction();
            EventPublishState eventPublishState = event.getEventPublishState();

            if (stateAction == AdminEventAction.REJECT_EVENT) {
                if (eventPublishState == EventPublishState.PUBLISHED) {
                    throw new DataIntegrityViolationException("Can't REJECT event which is PUBLISHED already.");
                }

                event.setEventPublishState(EventPublishState.CANCELED);
            } else if (stateAction == AdminEventAction.PUBLISH_EVENT) {
                if (eventPublishState != EventPublishState.PENDING) {
                    throw new DataIntegrityViolationException("Can't PUBLISH event which is not PENDING yet.");
                }

                LocalDateTime now = Util.getNowTruncatedToSeconds();

                if (now.plusHours(1).isAfter(event.getEventDate())) {
                    throw new DataIntegrityViolationException("There are less than 1 hour between publish time and " +
                            "event time.");
                }

                event.setEventPublishState(EventPublishState.PUBLISHED);
                event.setPublishedOn(now);
            }
        }

        return createEventFullDto(eventsRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> searchPublicEvents(SearchPublicEventsParameters searchParams) {
        QEvent event = QEvent.event;
        List<BooleanExpression> conditions = new ArrayList<>();
        conditions.add(event.eventPublishState.eq(EventPublishState.PUBLISHED));

        if (searchParams.getText() != null) {
            String text = searchParams.getText();
            conditions.add(event.annotation.containsIgnoreCase(text).or(event.description.containsIgnoreCase(text)));
        }

        if (searchParams.getCategories() != null) {
            List<CategoryDto> categories = categoryClient.getCategoryByIds(searchParams.getCategories());

            if (categories.isEmpty()) {
                throw new ValidationException("Categories from search query are not found.");
            }

            conditions.add(event.categoryId.in(searchParams.getCategories()));
        }

        if (searchParams.getOnlyAvailable() != null) {
            List<Long> ids = getAvailableEventIdsByParticipantLimit();
            conditions.add(event.id.in(ids));
        }

        if (searchParams.getPaid() != null) {
            conditions.add(event.paid.eq(searchParams.getPaid()));
        }

        if (searchParams.getRangeStart() != null || searchParams.getRangeEnd() != null) {
            if (searchParams.getRangeStart() != null) {
                conditions.add(event.eventDate.after(searchParams.getRangeStart()));
            }

            if (searchParams.getRangeEnd() != null) {
                conditions.add(event.eventDate.before(searchParams.getRangeEnd()));
            }
        } else {
            LocalDateTime now = Util.getNowTruncatedToSeconds();
            conditions.add(event.eventDate.after(now));
        }

        BooleanExpression condition = conditions.stream()
                .reduce(Expressions.asBoolean(true).isTrue(), BooleanExpression::and);
        Iterable<Event> resultEvents = eventsRepository.findAll(condition);
        List<Long> resultEventIds = StreamSupport.stream(resultEvents.spliterator(), false)
                .map(Event::getId)
                .toList();
        Map<Long, Long> eventsViewsMap = eventsViewsGetter.getEventsViewsMap(resultEventIds);
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(resultEventIds);
        Comparator<Event> sorting = Comparator.comparing(Event::getEventDate);

        if (searchParams.getSort() == SortingEvents.VIEWS) {
            sorting = (ev1, ev2) -> Long.compare(eventsViewsMap.get(ev2.getId()), eventsViewsMap.get(ev1.getId()));
        } else if (searchParams.getSort() == SortingEvents.COMMENTS) {
            Map<Long, Long> commentsMap = getCommentsNumberMap(resultEventIds);
            sorting = (ev1, ev2) -> Long.compare(commentsMap.get(ev2.getId()), commentsMap.get(ev1.getId()));
        }

        return StreamSupport.stream(resultEvents.spliterator(), false)
                .sorted(sorting)
                .skip(searchParams.getFrom())
                .limit(searchParams.getSize())
                .map(ev -> createEventFullDto(ev, eventsViewsMap.get(ev.getId()),
                        confirmedRequestsMap.getOrDefault(ev.getId(), 0L)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDtoWithComments getPublicEventById(Long eventId) {
        QEvent event = QEvent.event;
        Event resultEvent = eventsRepository
                .findOne(event.id.eq(eventId).and(event.eventPublishState.eq(EventPublishState.PUBLISHED)))
                .orElseThrow(() -> new NotFoundException(
                        String.format("Event id=%d not found or is not published.", eventId))
                );
        return createEventFullDtoWithComments(resultEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentShortDto> getAllEventComments(GetAllCommentsParameters parameters) {
        Event event = getEventWithCheck(parameters.getEventId());
        return commentClient.getCommentsForEvent(parameters.getEventId(), parameters.getFrom(), parameters.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public Long getEventsCountByCategoryId(Long catId) {
        return eventsRepository.countByCategoryId(catId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsShortDtoByIds(List<Long> eventIds) {
        List<Event> events = eventsRepository.findAllById(eventIds);
        return createEventShortDtoList(events);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean chekEventExistingByIds(List<Long> eventIds) {
        List<Event> events = eventsRepository.findAllById(eventIds);
        return events.size() == eventIds.size();
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventById(Long eventId) {
        Event event = getEventWithCheck(eventId);
        return createEventFullDto(event);
    }

    private Event getEventWithCheck(long eventId) {
        return eventsRepository.findById(eventId)
                .orElseThrow(() -> new ConflictException(String.format("Event id=%d not found.", eventId)));
    }

    private UserDto getUserWithCheck(long userId) {
        try {
            return userClient.getUserById(userId);
        } catch (ClientApiException e) {
            throw new ConflictException(String.format("User id=%d not found.", userId));
        }
    }

    private CategoryDto getCategoryWithCheck(long categoryId) {
        try {
            return categoryClient.getCategoryById(categoryId);
        } catch (ClientApiException e) {
            throw new ConflictException(String.format("Category id=%d not found.", categoryId));
        }
    }

    private void checkUserExisting(long userId) {
        if (!userClient.checkUserExisting(userId)) {
            throw new ConflictException(String.format("User id=%d not found.", userId));
        }
    }

    private void checkEventDateBeforeHours(LocalDateTime eventDateTime) {
        LocalDateTime now = Util.getNowTruncatedToSeconds();

        if (eventDateTime.isBefore(now.plusHours(2))) {
            throw new ValidationException("DateTime of event must be ahead more than 2 hours.");
        }
    }

    private void checkUserRights(long userId, Event event) {
        if (!event.getInitiatorId().equals(userId)) {
            throw new ConflictException(
                    String.format("Access deny for user id=%d with event id=%d.", userId, event.getId())
            );
        }
    }

    private boolean canUserUpdateEvent(Event event) {
        EventPublishState state = event.getEventPublishState();
        return state.equals(EventPublishState.CANCELED) || state.equals(EventPublishState.PENDING);
    }

    private Pageable createPageableObject(int from, int size) {
        if (from < 0 || size <= 0) {
            throw new IllegalArgumentException("Parameters 'from' and 'size' can not be less then zero");
        }

        return PageRequest.of(from / size, size);
    }

    private void updateCommonEventProperties(Event event, UpdateEventCommonRequest commonProperties) {
        if (commonProperties.getEventDate() != null) {
            checkEventDateBeforeHours(commonProperties.getEventDate());
            event.setEventDate(commonProperties.getEventDate());
        }

        if (commonProperties.getCategory() != null) {
            event.setCategoryId(getCategoryWithCheck(commonProperties.getCategory()).getId());
        }

        if (commonProperties.getTitle() != null) {
            event.setTitle(commonProperties.getTitle());
        }

        if (commonProperties.getDescription() != null) {
            event.setDescription(commonProperties.getDescription());
        }

        if (commonProperties.getAnnotation() != null) {
            event.setAnnotation(commonProperties.getAnnotation());
        }

        if (commonProperties.getLocation() != null) {
            LocationDto location = commonProperties.getLocation();
            event.setLocationLat(location.getLat());
            event.setLocationLon(location.getLon());
        }

        if (commonProperties.getRequestModeration() != null) {
            event.setRequestModeration(commonProperties.getRequestModeration());
        }

        if (commonProperties.getPaid() != null) {
            event.setPaid(commonProperties.getPaid());
        }

        if (commonProperties.getParticipantLimit() != null) {
            event.setParticipantLimit(commonProperties.getParticipantLimit());
        }
    }

    private Map<Long, Long> getConfirmedRequestsMap(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return requestClient.getConfirmedRequestsCount(eventIds);
        } catch (ClientApiException e) {
            throw new ServiceUnavailableException("Request service is temporarily unavailable, please try again later");
        }
    }

    private List<Long> getAvailableEventIdsByParticipantLimit() {
        List<Event> events = eventsRepository.findAll();
        if (events.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> eventIds = events.stream().map(Event::getId).toList();
        System.out.println("В методе getAvailableEventIdsByParticipantLimit");
        System.out.println(eventIds);
        Map<Long, Long> confirmedRequestsMap = requestClient.getConfirmedRequestsCount(eventIds);
        System.out.println(confirmedRequestsMap);

        return events.stream()
                .filter(event -> event.getParticipantLimit() == 0 ||
                        confirmedRequestsMap.getOrDefault(event.getId(), 0L) < event.getParticipantLimit())
                .map(Event::getId)
                .toList();
    }

    private Map<Long, Long> getCommentsNumberMap(List<Long> eventIds) {
        Map<Long, Long> commentsNumberMap = commentClient.getCommentsNumberForEvents(eventIds).stream()
                .collect(Collectors.toMap(List::getFirst, List::getLast));

        return eventIds.stream()
                .collect(Collectors.toMap(Function.identity(), id -> commentsNumberMap.getOrDefault(id, 0L)));
    }

    private UserShortDto getUserShorDto(Long userId) {
        try {
            return userClient.getUserShortDtoById(userId);
        } catch (ClientApiException e) {
            throw new NotFoundException(String.format("User not found with id %d", userId));
        }
    }

    private EventFullDto createEventFullDto(Event event) {
        long id = event.getId();
        Map<Long, Long> eventsViewsMap = eventsViewsGetter.getEventsViewsMap(List.of(id));
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(List.of(id));

        MappingEventParameters eventFullDtoParams = EventMapper.createMappingEventParameter(event,
                getCategoryWithCheck(event.getCategoryId()),
                getUserShorDto(event.getInitiatorId()),
                eventsViewsMap.getOrDefault(id, 0L),
                confirmedRequestsMap.getOrDefault(id, 0L));
        return EventMapper.toEventFullDto(eventFullDtoParams);
    }

    private EventFullDtoWithComments createEventFullDtoWithComments(Event event) {
        long id = event.getId();
        Map<Long, Long> eventsViewsMap = eventsViewsGetter.getEventsViewsMap(List.of(id));
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(List.of(id));
        List<CommentShortDto> comments;

        try {
          comments = commentClient.findFirstCommentsForEvent(id, 5L);
        } catch (ClientApiException e) {
            comments = new ArrayList<>();
        }

        MappingEventParameters eventFullDtoParams = EventMapper.createMappingEventParameterWithComments(event,
                getCategoryWithCheck(event.getCategoryId()),
                getUserShorDto(event.getInitiatorId()),
                eventsViewsMap.getOrDefault(id, 0L),
                confirmedRequestsMap.getOrDefault(id, 0L),
                comments);
        return EventMapper.toEventEventFullDtoWithComments(eventFullDtoParams);
    }

    private EventFullDto createEventFullDto(Event event, long views, long confirmedRequests) {

        MappingEventParameters eventFullDtoParams = EventMapper.createMappingEventParameter(event,
                getCategoryWithCheck(event.getCategoryId()),
                getUserShorDto(event.getInitiatorId()),
                views,
                confirmedRequests);
        return EventMapper.toEventFullDto(eventFullDtoParams);
    }

    private List<EventFullDto> createEventFullDtoList(List<Event> events) {
        List<Long> ids = events.stream()
                .map(Event::getId)
                .toList();
        Map<Long, Long> eventsViewsMap = eventsViewsGetter.getEventsViewsMap(ids);
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(ids);

        return events.stream()
                .map(event -> {
                    MappingEventParameters eventFullDtoParams = EventMapper.createMappingEventParameter(event,
                            getCategoryWithCheck(event.getCategoryId()),
                            getUserShorDto(event.getInitiatorId()),
                            eventsViewsMap.getOrDefault(event.getId(), 0L),
                            confirmedRequestsMap.getOrDefault(event.getId(), 0L));
                    return EventMapper.toEventFullDto(eventFullDtoParams);
                })
                .toList();
    }

    private List<EventShortDto> createEventShortDtoList(List<Event> events) {
        List<Long> ids = events.stream()
                .map(Event::getId)
                .toList();
        Map<Long, Long> eventsViewsMap = eventsViewsGetter.getEventsViewsMap(ids);
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(ids);

        return events.stream()
                .map(event -> {
                    MappingEventParameters mappingEventParameters = EventMapper.createMappingEventParameter(event,
                            getCategoryWithCheck(event.getCategoryId()),
                            getUserShorDto(event.getInitiatorId()),
                            eventsViewsMap.getOrDefault(event.getId(), 0L),
                            confirmedRequestsMap.getOrDefault(event.getId(), 0L));
                    return EventMapper.toEventShortDto(mappingEventParameters);
                })
                .toList();
    }
}
