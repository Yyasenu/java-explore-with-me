package ru.practicum.ewm.comment;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.user.UserShortResponseDto;

import java.time.LocalDateTime;

@UtilityClass
public class CommentMapper {
    public Comment toComment(NewCommentDto dto) {
        return Comment.builder()
                .text(dto.getText())
                .created(LocalDateTime.now())
                .status(Comment.CommentStatus.PENDING)
                .build();
    }

    public CommentResponseDto toResponseDto(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .author(new UserShortResponseDto(
                        comment.getAuthor().getId(),
                        comment.getAuthor().getName()))
                .created(comment.getCreated())
                .updated(comment.getUpdated())
                .status(comment.getStatus().name())
                .eventId(comment.getEvent().getId())
                .build();
    }

}
