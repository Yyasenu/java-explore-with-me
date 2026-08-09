package ru.practicum.ewm.request;

import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;


    @Override
    @Transactional
    public RequestResponseDto addRequestPrivate(Long userId, Long eventId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User was not found: " + userId));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event was not found: " + eventId));

        validateRequestCreation(requester, event);

        Request request = buildRequest(requester, event);
        Request savedRequest = requestRepository.save(request);

        return RequestMapper.toResponseDto(savedRequest);
    }

    @Override
    public List<RequestResponseDto> getUserRequestsPrivate(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User was not found: " + userId);
        }

        List<Request> requests = requestRepository.findAllByRequesterId(userId);

        return requests.stream()
                .map(RequestMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public RequestResponseDto cancelRequestPrivate(Long userId, Long requestId) {
        Request request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Request with id " + requestId + " for user " + userId + " was not found"));

        if (request.getStatus() == Request.Status.CANCELED) {
            throw new ConflictException("Request already canceled");
        }

        if (request.getStatus() == Request.Status.CONFIRMED) {
            throw new ConflictException("Cannot cancel confirmed request");
        }

        request.setStatus(Request.Status.CANCELED);
        Request updatedRequest = requestRepository.save(request);

        return RequestMapper.toResponseDto(updatedRequest);
    }

    private void validateRequestCreation(User requester, Event event) {
        if (event.getInitiator().getId().equals(requester.getId())) {
            throw new ConflictException("Initiator can't create request for own event");
        }

        if (event.getState() != Event.EventState.PUBLISHED) {
            throw new ConflictException("Can't participate in unpublished event");
        }

        if (requestRepository.existsByEventIdAndRequesterId(event.getId(), requester.getId())) {
            throw new ConflictException("Request already exists");
        }

        if (event.getParticipantLimit() != 0 &&
                event.getParticipantLimit() <= requestRepository.countByEventIdAndStatus(event.getId(), Request.Status.CONFIRMED)) {
            throw new ConflictException("Participant limit reached");
        }
    }

    private Request buildRequest(User requester, Event event) {
        Request.Status status = event.getRequestModeration() && event.getParticipantLimit() != 0
                ? Request.Status.PENDING
                : Request.Status.CONFIRMED;

        return Request.builder()
                .event(event)
                .requester(requester)
                .created(LocalDateTime.now())
                .status(status)
                .build();
    }
}
