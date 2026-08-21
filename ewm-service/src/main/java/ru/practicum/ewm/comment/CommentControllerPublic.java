package ru.practicum.ewm.comment;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events/{eventId}/comments")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CommentControllerPublic {
    private final CommentService commentService;

    @GetMapping
    public List<CommentResponseDto> getEventComments(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Get comments on event {}", eventId);
        return commentService.getPublishedCommentsForEventPublic(eventId, from, size);
    }

    @GetMapping("/{commentId}")
    public CommentResponseDto getComment(
            @PathVariable Long eventId,
            @PathVariable Long commentId) {
        log.info("Get comment with id {} on event {}", commentId, eventId);
        return commentService.getPublishedCommentPublic(eventId, commentId);
    }

}
