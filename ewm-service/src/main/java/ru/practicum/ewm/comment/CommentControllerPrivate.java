package ru.practicum.ewm.comment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CommentControllerPrivate {
    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDto createComment(
            @PathVariable Long userId,
            @RequestParam Long eventId,
            @Valid @RequestBody NewCommentDto commentDto) {
        log.info("Create comment by user {} on event {}", userId, eventId);
        return commentService.createCommentPrivate(userId, eventId, commentDto);
    }

    @PatchMapping("/{commentId}")
    public CommentResponseDto updateComment(
            @PathVariable Long userId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentDto commentDto) {
        log.info("Update comment {} by user {}", commentId, userId);
        return commentService.updateCommentPrivate(userId, commentId, commentDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable Long userId,
            @PathVariable Long commentId) {
        log.info("Delete comment {} by user {}", commentId, userId);
        commentService.deleteCommentPrivate(userId, commentId);
    }

}