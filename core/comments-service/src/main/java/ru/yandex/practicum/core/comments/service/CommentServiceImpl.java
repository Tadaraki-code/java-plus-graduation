package ru.yandex.practicum.core.comments.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.core.comments.mapper.CommentMapper;
import ru.yandex.practicum.core.comments.model.Comment;
import ru.yandex.practicum.core.comments.model.QComment;
import ru.yandex.practicum.core.comments.storage.CommentRepository;
import ru.yandex.practicum.core.interaction.clients.EventClient;
import ru.yandex.practicum.core.interaction.clients.RequestClient;
import ru.yandex.practicum.core.interaction.clients.UserClient;
import ru.yandex.practicum.core.interaction.comments.dto.*;
import ru.yandex.practicum.core.interaction.comments.dto.parameters.GetCommentsForAdminParameters;
import ru.yandex.practicum.core.interaction.comments.dto.parameters.GetCommentsParameters;
import ru.yandex.practicum.core.interaction.comments.dto.parameters.UpdateCommentParameters;
import ru.yandex.practicum.core.interaction.comments.enums.AdminAction;
import ru.yandex.practicum.core.interaction.comments.enums.CommentStatus;
import ru.yandex.practicum.core.interaction.error.exception.ClientApiException;
import ru.yandex.practicum.core.interaction.error.exception.ConflictException;
import ru.yandex.practicum.core.interaction.error.exception.NotFoundException;
import ru.yandex.practicum.core.interaction.error.exception.ValidationException;
import ru.yandex.practicum.core.interaction.event.dto.EventFullDto;
import ru.yandex.practicum.core.interaction.event.dto.parameters.GetAllCommentsParameters;
import ru.yandex.practicum.core.interaction.request.dto.ParticipationRequestDto;
import ru.yandex.practicum.core.interaction.request.enums.RequestStatus;
import ru.yandex.practicum.core.interaction.user.dto.UserDto;
import ru.yandex.practicum.core.interaction.util.Util;


import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserClient userClient;
    private final EventClient eventClient;
    private final RequestClient requestClient;

    @Override
    @Transactional
    public CommentDto createComment(Long userId, NewCommentDto newCommentDto) {
        UserDto user = getUserWithCheck(userId);
        EventFullDto event = getEventWithCheck(newCommentDto.getEventId());
        ParticipationRequestDto request = getRequestWithCheck(userId, newCommentDto.getEventId());

        if (!RequestStatus.CONFIRMED.toString().equals(request.getStatus())) {
            throw new ValidationException("You cannot leave a comment because " +
                    "your request was rejected.");
        }

        if (commentRepository.existsByAuthorIdAndEventId(userId, newCommentDto.getEventId())) {
            throw new ConflictException("You can leave a comment only once.");
        }

        Comment comment = CommentMapper.fromNewCommentDto(newCommentDto);
        comment.setAuthorId(user.getId());
        comment.setAuthorName(user.getName());
        comment.setEventId(event.getId());
        comment.setCreatedOn(Util.getNowTruncatedToSeconds());

        log.info("Created comment for userId={}, eventId={}", userId, newCommentDto.getEventId());
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getComments(GetCommentsParameters parameters) {
        QComment comment = QComment.comment;
        getUserWithCheck(parameters.getUserId());
        List<BooleanExpression> conditions = new ArrayList<>();
        Pageable page = createPageableObject(parameters.getFrom(), parameters.getSize());

        conditions.add(comment.authorId.eq(parameters.getUserId()));

        if (parameters.getEventIds() != null && !parameters.getEventIds().isEmpty()) {
            conditions.add(comment.eventId.in(parameters.getEventIds()));
        }

        if (parameters.getStatus() != null) {
            conditions.add(comment.status.eq(parameters.getStatus()));
        }

        BooleanExpression condition = conditions.stream()
                .reduce(Expressions.asBoolean(true).isTrue(), BooleanExpression::and);

        return commentRepository.findAll(condition, page)
                .map(CommentMapper::toCommentDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getComment(Long commentId, Long userId) {
        Comment comment = getCommentWithCheck(commentId);
        if (!comment.getAuthorId().equals(userId)) {
            throw new ValidationException("Only author can see comment.");
        }
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public CommentDto updateComment(UpdateCommentParameters parameters) {
        Comment comment = getCommentWithCheck(parameters.getCommentId());
        if (!comment.getAuthorId().equals(parameters.getUserId())) {
            throw new ValidationException("Only author can update comment.");
        }

        if (comment.getStatus() == CommentStatus.PENDING) {
            throw new ValidationException("Cannot edit comment while it is pending moderation.");
        }

        UpdateCommentDto updateDto = parameters.getUpdateCommentDto();
        comment.setText(updateDto.getText());
        comment.setStatus(CommentStatus.PENDING);

        log.info("Updated comment id={} for userId={}", parameters.getCommentId(), parameters.getUserId());
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = getCommentWithCheck(commentId);
        if (!comment.getAuthorId().equals(userId)) {
            throw new ValidationException("Only author can delete his comment.");
        }
        commentRepository.delete(comment);
        log.info("Deleted comment id={} for userId={}", commentId, userId);
    }

    @Override
    public List<CommentDto> getCommentsForAdmin(GetCommentsForAdminParameters parameters) {
        CommentStatus status = parameters.getStatus();
        Pageable pageable = createPageableObject(parameters.getFrom(), parameters.getSize());
        return commentRepository.findPageableCommentsForAdmin(status, pageable).stream()
                .map(CommentMapper::toCommentDto)
                .toList();
    }

    @Override
    public CommentDto updateCommentByAdmin(long commentId, UpdateCommentAdminDto updateCommentAdminDto) {
        Comment comment = getCommentWithCheck(commentId);
        AdminAction action = updateCommentAdminDto.getAction();

        if (comment.getStatus() != CommentStatus.PENDING) {
            throw new ConflictException("Comment must has status PENDING.");
        }

        comment.setStatus(action == AdminAction.APPROVE ? CommentStatus.APPROVE : CommentStatus.REJECT);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentShortDto> getCommentsForEvent(GetAllCommentsParameters parameters) {
        List<Comment> comments = commentRepository.findPageableCommentsForEvent(parameters.getEventId(),
                parameters.getFrom(), parameters.getSize());
        return comments.stream().map(CommentMapper::toCommentShortDto).toList();
    }

    @Override
    public List<List<Long>> getCommentsNumberForEvents(List<Long> eventIds) {
        return commentRepository.getCommentsNumberForEvents(eventIds);
    }

    @Override
    public List<CommentShortDto> findFirstCommentsForEvent(Long eventId, Long size) {
        return commentRepository.findFirstCommentsForEvent(eventId, size).stream()
                .map(CommentMapper::toCommentShortDto)
                .toList();
    }

    private UserDto getUserWithCheck(Long userId) {
        try {
            return userClient.getUserById(userId);
        } catch (ClientApiException e) {
            throw new NotFoundException(String.format("User not found with id %d", userId));
        }
    }

    private ParticipationRequestDto getRequestWithCheck(Long userId, Long eventId) {
        try {
            return requestClient.getUserRequest(userId, eventId);
        } catch (ClientApiException e) {
            if (e.getMessage().equals("NOT_FOUND")) {
               throw new ValidationException("You cannot leave a comment because " +
                       "you did not leave a request to participate");
            }else {
                throw e;
            }
        }
    }

    private EventFullDto getEventWithCheck(Long eventId) {
        try {
            return eventClient.getEventById(eventId);
        } catch (ClientApiException e) {
            throw new NotFoundException(String.format("Event not found with id %d", eventId));
        }
    }

    private Comment getCommentWithCheck(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment id=%d not found.", commentId)));
    }

    private Pageable createPageableObject(Integer from, Integer size) {
        return PageRequest.of(from / size, size);
    }
}
