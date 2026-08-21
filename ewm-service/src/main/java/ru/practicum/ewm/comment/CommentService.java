package ru.practicum.ewm.comment;

import java.util.List;

public interface CommentService {
    List<CommentResponseDto> getPublishedCommentsForEventPublic(Long eventId, Integer from, Integer size);

    CommentResponseDto getPublishedCommentPublic(Long eventId, Long commentId);

    CommentResponseDto createCommentPrivate(Long userId, Long eventId, NewCommentDto commentRequestDto);

    CommentResponseDto updateCommentPrivate(Long userId, Long commentId, UpdateCommentDto commentRequestDto);

    void deleteCommentPrivate(Long userId, Long commentId);

    List<CommentResponseDto> searchCommentsAdmin(
            List<Long> users, List<String> statuses, List<Long> events, Integer from, Integer size);

    CommentResponseDto publishCommentAdmin(Long commentId);

    CommentResponseDto rejectCommentAdmin(Long commentId);

    void deleteCommentAdmin(Long commentId);
}