package ru.practicum.ewm.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.User;
import ru.practicum.ewm.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public List<CommentResponseDto> getPublishedCommentsForEventPublic(Long eventId, Integer from, Integer size) {
        validateEventExists(eventId);

        Page<Comment> comments = commentRepository.findByEventIdAndStatus(
                eventId,
                Comment.CommentStatus.PUBLISHED,
                PageRequest.of(from / size, size));

        return comments.getContent().stream()
                .map(CommentMapper::toResponseDto)
                .toList();
    }

    @Override
    public CommentResponseDto getPublishedCommentPublic(Long eventId, Long commentId) {
        validateEventExists(eventId);

        Comment comment = commentRepository.findByIdAndEventIdAndStatus(
                        commentId,
                        eventId,
                        Comment.CommentStatus.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Comment not found or not published"));

        return CommentMapper.toResponseDto(comment);
    }

    @Override
    @Transactional
    public CommentResponseDto createCommentPrivate(Long userId, Long eventId, NewCommentDto commentDto) {
        User user = getUserIfExists(userId);
        Event event = getEventIfExists(eventId);

        validateEventPublished(event);

        Comment comment = CommentMapper.toComment(commentDto);
        comment.setAuthor(user);
        comment.setEvent(event);

        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toResponseDto(savedComment);
    }


    @Override
    @Transactional
    public CommentResponseDto updateCommentPrivate(Long userId, Long commentId, UpdateCommentDto commentDto) {
        Comment comment = getCommentIfExists(commentId);
        validateUserIsAuthor(userId, comment);
        validateCommentNotRejected(comment);

        if (comment.getStatus() == Comment.CommentStatus.PUBLISHED) {
            comment.setStatus(Comment.CommentStatus.EDITED);
        }

        comment.setText(commentDto.getText());
        comment.setUpdated(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);
        return CommentMapper.toResponseDto(updatedComment);
    }

    @Override
    @Transactional
    public void deleteCommentPrivate(Long userId, Long commentId) {
        Comment comment = getCommentIfExists(commentId);
        validateUserIsAuthor(userId, comment);
        commentRepository.delete(comment);
    }

    @Override
    public List<CommentResponseDto> searchCommentsAdmin(
            List<Long> users, List<String> statuses, List<Long> events, Integer from, Integer size) {

        List<Comment.CommentStatus> statusEnums = statuses != null ?
                statuses.stream()
                        .map(String::toUpperCase)
                        .map(Comment.CommentStatus::valueOf)
                        .toList() : null;

        Page<Comment> comments = commentRepository.searchComments(
                users,
                statusEnums,
                events,
                PageRequest.of(from / size, size));

        return comments.getContent().stream()
                .map(CommentMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentResponseDto publishCommentAdmin(Long commentId) {
        Comment comment = getCommentIfExists(commentId);
        validateCommentStatus(comment);

        comment.setStatus(Comment.CommentStatus.PUBLISHED);
        return CommentMapper.toResponseDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentResponseDto rejectCommentAdmin(Long commentId) {
        Comment comment = getCommentIfExists(commentId);
        validateCommentStatus(comment);

        comment.setStatus(Comment.CommentStatus.CANCELED);
        return CommentMapper.toResponseDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteCommentAdmin(Long commentId) {
        commentRepository.deleteById(commentId);
    }


    private User getUserIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private Event getEventIfExists(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));
    }

    private Comment getCommentIfExists(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found: " + commentId));
    }

    private void validateEventExists(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Event not found: " + eventId);
        }
    }

    private void validateUserIsAuthor(Long userId, Comment comment) {
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("User " + userId + " is not the author of comment " + comment.getId());
        }
    }

    private void validateEventPublished(Event event) {
        if (event.getState() != Event.EventState.PUBLISHED) {
            throw new ConflictException("Cannot comment on unpublished event");
        }
    }

    private void validateCommentNotRejected(Comment comment) {
        if (comment.getStatus() == Comment.CommentStatus.CANCELED) {
            throw new ConflictException("Cannot modify rejected comment");
        }
    }

    private void validateCommentStatus(Comment comment) {
        if (comment.getStatus() != Comment.CommentStatus.PENDING) {
            throw new ConflictException("Only pending comments can be published or rejected");
        }
    }

}