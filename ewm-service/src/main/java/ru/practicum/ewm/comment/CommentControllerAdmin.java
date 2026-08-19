package ru.practicum.ewm.comment;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CommentControllerAdmin {
    private final CommentService commentService;

    @GetMapping
    public List<CommentResponseDto> searchComments(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) List<Long> events,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Search comments with filters: {}, {}, {}", users, statuses, events);
        return commentService.searchCommentsAdmin(users, statuses, events, from, size);
    }

    @PatchMapping("/{commentId}/publish")
    public CommentResponseDto publishComment(
            @PathVariable Long commentId) {
        log.info("Publish comment {}", commentId);
        return commentService.publishCommentAdmin(commentId);
    }

    @PatchMapping("/{commentId}/reject")
    public CommentResponseDto rejectComment(
            @PathVariable Long commentId) {
        log.info("Reject comment {}", commentId);
        return commentService.rejectCommentAdmin(commentId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByAdmin(
            @PathVariable Long commentId) {
        log.info("Delete comment {} by admin", commentId);
        commentService.deleteCommentAdmin(commentId);
    }
}