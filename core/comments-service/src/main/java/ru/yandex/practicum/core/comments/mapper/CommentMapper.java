package ru.yandex.practicum.core.comments.mapper;

import ru.yandex.practicum.core.comments.model.Comment;
import ru.yandex.practicum.core.interaction.comments.dto.CommentDto;
import ru.yandex.practicum.core.interaction.comments.dto.CommentShortDto;
import ru.yandex.practicum.core.interaction.comments.dto.NewCommentDto;

public class CommentMapper {


    public static Comment fromNewCommentDto(NewCommentDto newCommentDto) {
        return Comment.builder()
                .text(newCommentDto.getText())
                .build();
    }

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .eventId(comment.getEventId())
                .authorId(comment.getAuthorId())
                .text(comment.getText())
                .status(comment.getStatus())
                .createdOn(comment.getCreatedOn())
                .build();
    }

    public static CommentShortDto toCommentShortDto(Comment comment) {
        return CommentShortDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .author(comment.getAuthorName())
                .build();
    }
}
