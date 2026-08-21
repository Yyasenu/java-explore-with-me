package ru.practicum.ewm.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByEventIdAndStatus(
            Long eventId,
            Comment.CommentStatus status,
            Pageable pageable);

    Optional<Comment> findByIdAndEventIdAndStatus(
            Long id,
            Long eventId,
            Comment.CommentStatus status);

    @Query("SELECT c FROM Comment c " +
            "WHERE (:users IS NULL OR c.author.id IN :users) " +
            "AND (:statuses IS NULL OR c.status IN :statuses) " +
            "AND (:events IS NULL OR c.event.id IN :events)")
    Page<Comment> searchComments(
            @Param("users") List<Long> users,
            @Param("statuses") List<Comment.CommentStatus> statuses,
            @Param("events") List<Long> events,
            Pageable pageable);
}
