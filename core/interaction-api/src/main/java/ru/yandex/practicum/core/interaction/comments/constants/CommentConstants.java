package ru.yandex.practicum.core.interaction.comments.constants;

public class CommentConstants {
    public static final String PRIVATE_API_PREFIX = "/users/{user-id}/comments";
    public static final String PRIVATE_API_PREFIX_COMMENT_ID = "/users/{user-id}/comments/{comment-id}";

    public static final String ADMIN_API_PREFIX = "/admin/comments";
    public static final String ADMIN_API_PREFIX_COMMENT_ID = "/admin/comments/{comment-id}";

    public static final String USER_ID = "user-id";
    public static final String COMMENT_ID = "comment-id";

    public static final String INTERACTION_API_PREFIX = "/interaction/comment";
    public static final String COUNT_API_PREFIX = "/count";
    public static final String FIRST_API_PREFIX = "/first/{event-id}";
    public static final String EVENT_ID = "event-id";

}
