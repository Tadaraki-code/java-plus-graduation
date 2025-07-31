package ru.yandex.practicum.core.interaction.request.constants;

public interface RequestConstants {
    public static final String USERS = "/users";
    public static final String REQUEST_BASE_PATH = "/{user-id}/requests";
    public static final String USER_ID = "user-id";
    public static final String REQUEST_BASE_PATCH_PATH = "/{user-id}/requests/{request-id}/cancel";
    public static final String REQUEST_ID = "request-id";

    public static final String INTERACTION_API_PREFIX = "/interaction/request";
    public static final String GET_USER_REQUEST_API_PREFIX = "/user/{user-id}/event/{event-id}";
    public static final String EVENT_REQUESTS_API_PREFIX = "/event/{event-id}";
    public static final String EVENT_ID = "event-id";
    public static final String CONFIRMED_COUNT = "/confirmed-count";
}
