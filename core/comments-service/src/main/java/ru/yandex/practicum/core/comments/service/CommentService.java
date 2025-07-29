package ru.yandex.practicum.core.comments.service;


import ru.yandex.practicum.core.interaction.comments.dto.CommentDto;
import ru.yandex.practicum.core.interaction.comments.dto.CommentShortDto;
import ru.yandex.practicum.core.interaction.comments.dto.NewCommentDto;
import ru.yandex.practicum.core.interaction.comments.dto.UpdateCommentAdminDto;
import ru.yandex.practicum.core.interaction.comments.dto.parameters.GetCommentsForAdminParameters;
import ru.yandex.practicum.core.interaction.comments.dto.parameters.GetCommentsParameters;
import ru.yandex.practicum.core.interaction.comments.dto.parameters.UpdateCommentParameters;
import ru.yandex.practicum.core.interaction.event.dto.parameters.GetAllCommentsParameters;

import java.util.List;


public interface CommentService {
    CommentDto createComment(Long userId, NewCommentDto newCommentDto);

    List<CommentDto> getComments(GetCommentsParameters parameters);

    CommentDto getComment(Long commentId, Long userId);

    CommentDto updateComment(UpdateCommentParameters parameters);

    void deleteComment(Long commentId, Long userId);

    List<CommentDto> getCommentsForAdmin(GetCommentsForAdminParameters parameters);

    CommentDto updateCommentByAdmin(long commentId, UpdateCommentAdminDto updateCommentAdminDto);

    List<CommentShortDto> getCommentsForEvent(GetAllCommentsParameters parameters);

    List<List<Long>> getCommentsNumberForEvents(List<Long> eventIds);

    List<CommentShortDto> findFirstCommentsForEvent(Long eventId, Long size);
}
