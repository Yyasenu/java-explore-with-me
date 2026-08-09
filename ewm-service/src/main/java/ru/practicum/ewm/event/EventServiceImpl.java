package ru.practicum.ewm.event;

import client.StatsClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.RequestEndpointHitDto;
import dto.ResponseViewStatsDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.Category;
import ru.practicum.ewm.category.CategoryRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.EventCreateException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.StatsClientException;
import ru.practicum.ewm.request.Request;
import ru.practicum.ewm.request.RequestMapper;
import ru.practicum.ewm.request.RequestRepository;
import ru.practicum.ewm.request.RequestResponseDto;
import ru.practicum.ewm.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final CategoryRepository categoryRepository;
    private final StatsClient statsClient;

    @Value("${app}")
    private String app;


    @Override
    @Transactional
    public EventFullResponseDto updateEventAdmin(Long eventId, UpdateEventAdminDto updateDto) {
        Event event = getAndValidateEventForAdminUpdate(eventId, updateDto);

        if (updateDto.getStateAction() == UpdateEventAdminDto.StateAction.REJECT_EVENT) {
            if (event.getState() != Event.EventState.PENDING) {
                throw new ConflictException("Cannot reject event that is not in PENDING state");
            }
            event.setState(Event.EventState.CANCELED);
        }

        updateEventFieldsByAdmin(event, updateDto);

        Event updatedEvent = eventRepository.save(event);

        Long confirmedRequests = requestRepository.countByEventIdAndStatus(
                eventId, Request.Status.CONFIRMED);
        Long views = getViewsForEvent(event);

        return EventMapper.toFullResponseDto(updatedEvent, confirmedRequests, views);
    }

    @Override
    public List<EventFullResponseDto> getEventsAdmin(
            List<Long> userIds, List<String> states, List<Long> categories,
            LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {

        List<Event.EventState> statesEnum = null;
        if (states != null) {
            statesEnum = states.stream()
                    .map(String::toUpperCase)
                    .map(Event.EventState::valueOf)
                    .toList();
        }

        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
        }

        Page<Event> events = eventRepository.getEventsByAdmin(
                userIds,
                statesEnum,
                categories,
                rangeStart,
                rangeEnd,
                PageRequest.of(from / size, size));


        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> confirmedRequests = getConfirmedRequestsCounts(eventIds);
        Map<Long, Long> views = getViewsForEvents(eventIds);

        return events.stream()
                .map(event -> EventMapper.toFullResponseDto(
                        event,
                        confirmedRequests.getOrDefault(event.getId(), 0L),
                        views.getOrDefault(event.getId(), 0L)
                ))
                .toList();
    }

    @Override
    @Transactional
    public EventFullResponseDto createEventPrivate(Long userId, NewEventDto eventDto) {
        validationEventDate(eventDto.getEventDate());
        Event event = EventMapper.toEvent(userId, eventDto, categoryRepository, userRepository);
        Event savedEvent = eventRepository.save(event);
        return EventMapper.toFullResponseDto(savedEvent, 0L, 0L);
    }

    @Override
    public EventFullResponseDto getFullEventByIdPrivate(Long userId, Long eventId) {
        EventFullResponseDto eventDto = eventRepository.findById(eventId)
                .map(event1 -> {
                    Long confirmedRequests = requestRepository.countByEventIdAndStatus(
                            event1.getId(), Request.Status.CONFIRMED);
                    Long views = getViewsForEvent(event1);
                    return EventMapper.toFullResponseDto(event1, confirmedRequests, views);
                })
                .orElseThrow(() -> new NotFoundException("Event was not found: " + eventId));

        if (!eventDto.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("User " + userId + " does not match the initiator " + eventDto.getInitiator().getId());
        }

        return eventDto;
    }

    @Override
    public List<EventShortResponseDto> getEventsPrivate(Long userId, Integer from, Integer size) {
        Page<Event> events = eventRepository.findByInitiatorId(
                userId, PageRequest.of(from / size, size));

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> confirmedRequests = getConfirmedRequestsCounts(eventIds);
        Map<Long, Long> views = getViewsForEvents(eventIds);

        return events.stream()
                .map(event -> EventMapper.toShortResponseDto(
                        event,
                        confirmedRequests.getOrDefault(event.getId(), 0L),
                        views.getOrDefault(event.getId(), 0L)
                ))
                .toList();
    }

    @Override
    @Transactional
    public EventFullResponseDto updateEventPrivate(Long userId, Long eventId, UpdateEventUserDto updateDto) {
        Event oldEvent = getAndValidateEventBeforeUpdate(userId, eventId, updateDto);

        if (oldEvent.getState() == Event.EventState.CANCELED &&
                updateDto.getStateAction() == UpdateEventUserDto.StateAction.SEND_TO_REVIEW) {
            oldEvent.setState(Event.EventState.PENDING);
        }

        updateEventFields(oldEvent, updateDto);

        Event updatedEvent = eventRepository.save(oldEvent);

        Long confirmedRequests = requestRepository.countByEventIdAndStatus(
                eventId, Request.Status.CONFIRMED);
        Long views = getViewsForEvent(updatedEvent);

        return EventMapper.toFullResponseDto(updatedEvent, confirmedRequests, views);
    }

    @Override
    public List<RequestResponseDto> getRequestsByEventPrivate(Long userId, Long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User was not found " + userId);
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event was not found: " + eventId));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("User " + userId + " does not match the initiator " + event.getInitiator().getId());
        }

        List<Request> requests = requestRepository.findAllByEventId(eventId);

        return requests.stream()
                .map(RequestMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatusesPrivate(
            Long userId, Long eventId,
            EventRequestStatusUpdateRequest updateRequest) {

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User was not found " + userId);
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event was not found: " + eventId));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("User " + userId + " does not match the initiator " + event.getInitiator().getId());
        }

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ConflictException("Event does not require request moderation");
        }

        Long confirmedRequestsCount = requestRepository.countByEventIdAndStatus(
                eventId,
                Request.Status.CONFIRMED
        );


        List<Request> requests = requestRepository.findAllByIdInAndEventId(updateRequest.getRequestIds(), eventId);

        boolean hasNonPending = requests.stream()
                .anyMatch(request -> request.getStatus() != Request.Status.PENDING);

        if (hasNonPending) {
            throw new ConflictException("All requests must have PENDING status");
        }

        if (updateRequest.getStatus() == Request.Status.CONFIRMED) {
            return confirmation(event, requests, confirmedRequestsCount);
        } else {
            return rejection(requests);
        }

    }

    @Override
    public List<EventShortResponseDto> getEventsPublic(
            String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
            LocalDateTime rangeEnd, Boolean onlyAvailable, String sort,
            Integer from, Integer size, HttpServletRequest request) {


        statsClient.save(new RequestEndpointHitDto(
                app,
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now()
        ));

        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
        }

        Page<Event> events = eventRepository.getEventsPublic(
                text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, PageRequest.of(from / size, size));

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> confirmedRequests = getConfirmedRequestsCounts(eventIds);
        Map<Long, Long> views = getViewsForEvents(eventIds);

        return events.stream()
                .map(event -> EventMapper.toShortResponseDto(
                        event,
                        confirmedRequests.getOrDefault(event.getId(), 0L),
                        views.getOrDefault(event.getId(), 0L)
                ))
                .sorted(getComparator(sort))
                .toList();
    }

    @Override
    public EventFullResponseDto getFullEventByIdPublic(Long eventId, HttpServletRequest request) {
        statsClient.save(new RequestEndpointHitDto(
                app,
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now()
        ));

        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return eventRepository.findPublishedEventById(eventId)
                .map(event1 -> {
                    Long confirmedRequests = requestRepository.countByEventIdAndStatus(
                            event1.getId(), Request.Status.CONFIRMED);
                    Long views = getViewsForEvent(event1);
                    return EventMapper.toFullResponseDto(event1, confirmedRequests, views);
                })
                .orElseThrow(() -> new NotFoundException("Event was not found: " + eventId));
    }


    @Override
    public Set<EventShortResponseDto> toShortDtos(Set<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).toList();

        Map<Long, Long> requests = getConfirmedRequestsCounts(eventIds);
        Map<Long, Long> views = getViewsForEvents(eventIds);

        return events.stream()
                .map(e -> EventMapper.toShortResponseDto(e,
                        requests.getOrDefault(e.getId(), 0L),
                        views.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toSet());
    }

    private Event getAndValidateEventForAdminUpdate(Long eventId, UpdateEventAdminDto updateDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));

        if (updateDto.getStateAction() == UpdateEventAdminDto.StateAction.PUBLISH_EVENT) {
            if (event.getState() != Event.EventState.PENDING) {
                throw new ConflictException("Cannot publish event that is not in PENDING state");
            }
            if (updateDto.getEventDate() != null &&
                    updateDto.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ConflictException("Event date must be at least 1 hour after publication");
            }
        }

        return event;
    }

    private void updateEventFieldsByAdmin(Event event, UpdateEventAdminDto updateDto) {
        if (updateDto.getAnnotation() != null) {
            event.setAnnotation(updateDto.getAnnotation());
        }
        if (updateDto.getCategory() != null) {
            Category category = categoryRepository.findById(updateDto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            event.setCategory(category);
        }
        if (updateDto.getDescription() != null) {
            event.setDescription(updateDto.getDescription());
        }
        if (updateDto.getEventDate() != null) {
            event.setEventDate(updateDto.getEventDate());
        }
        if (updateDto.getLocation() != null) {
            event.setLocationLat(updateDto.getLocation().getLat());
            event.setLocationLon(updateDto.getLocation().getLon());
        }
        if (updateDto.getPaid() != null) {
            event.setPaid(updateDto.getPaid());
        }
        if (updateDto.getParticipantLimit() != null) {
            event.setParticipantLimit(updateDto.getParticipantLimit());
        }
        if (updateDto.getRequestModeration() != null) {
            event.setRequestModeration(updateDto.getRequestModeration());
        }
        if (updateDto.getTitle() != null) {
            event.setTitle(updateDto.getTitle());
        }

        if (updateDto.getStateAction() != null) {
            switch (updateDto.getStateAction()) {
                case PUBLISH_EVENT:
                    if (event.getState() != Event.EventState.PENDING) {
                        throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.getState());
                    }
                    event.setState(Event.EventState.PUBLISHED);
                    event.setPublished(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    if (event.getState() == Event.EventState.PUBLISHED) {
                        throw new ConflictException("Cannot reject the event because it's already published");
                    }
                    event.setState(Event.EventState.CANCELED);
                    break;
            }
        }
    }


    private void validationEventDate(LocalDateTime eventDate) {
        log.info("Validating event date: {}, current time: {}", eventDate, LocalDateTime.now());

        if (eventDate == null) {
            throw new EventCreateException("Event date cannot be null");
        }
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new EventCreateException("The event date cannot be earlier than two hours from the current moment");
        }
    }

    private Event getAndValidateEventBeforeUpdate(Long userId, Long eventId, UpdateEventUserDto updateDto) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User was not found " + userId);
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event was not found: " + eventId));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("User " + userId + " does not match the initiator " + event.getInitiator().getId());
        }

        if (event.getState() != Event.EventState.PENDING && event.getState() != Event.EventState.CANCELED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (updateDto.getEventDate() != null) {
            validationEventDate(updateDto.getEventDate());
        }

        return event;
    }

    private void updateEventFields(Event event, UpdateEventUserDto updateDto) {
        if (updateDto.getAnnotation() != null) {
            event.setAnnotation(updateDto.getAnnotation());
        }
        if (updateDto.getCategory() != null) {
            Category category = categoryRepository.findById(updateDto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            event.setCategory(category);
        }
        if (updateDto.getDescription() != null) {
            event.setDescription(updateDto.getDescription());
        }
        if (updateDto.getEventDate() != null) {
            event.setEventDate(updateDto.getEventDate());
        }
        if (updateDto.getLocation() != null) {
            event.setLocationLat(updateDto.getLocation().getLat());
            event.setLocationLon(updateDto.getLocation().getLon());
        }
        if (updateDto.getPaid() != null) {
            event.setPaid(updateDto.getPaid());
        }
        if (updateDto.getParticipantLimit() != null) {
            event.setParticipantLimit(updateDto.getParticipantLimit());
        }
        if (updateDto.getRequestModeration() != null) {
            event.setRequestModeration(updateDto.getRequestModeration());
        }
        if (updateDto.getTitle() != null) {
            event.setTitle(updateDto.getTitle());
        }

        if (updateDto.getStateAction() != null) {
            switch (updateDto.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setState(Event.EventState.PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(Event.EventState.CANCELED);
                    break;
            }
        }
    }

    private EventRequestStatusUpdateResult confirmation(
            Event event, List<Request> requests, Long confirmedRequestsCount) {
        int availableSlots = (int) (event.getParticipantLimit() - confirmedRequestsCount);

        if (availableSlots <= 0) {
            throw new ConflictException("The participant limit has been reached");
        }

        List<Request> toConfirm = requests.stream()
                .limit(availableSlots)
                .peek(request -> request.setStatus(Request.Status.CONFIRMED))
                .collect(Collectors.toList());

        List<Request> toReject = requests.size() > availableSlots
                ? requests.subList(availableSlots, requests.size())
                : Collections.emptyList();

        toReject.forEach(request -> request.setStatus(Request.Status.REJECTED));

        requestRepository.saveAll(toConfirm);
        requestRepository.saveAll(toReject);

        return EventMapper.toEventRequestStatusUpdateResult(toConfirm, toReject);
    }

    private EventRequestStatusUpdateResult rejection(List<Request> requests) {
        requests.forEach(request -> request.setStatus(Request.Status.REJECTED));
        requestRepository.saveAll(requests);

        return EventMapper.toEventRequestStatusUpdateResult(Collections.emptyList(), requests);
    }

    private Map<Long, Long> getConfirmedRequestsCounts(List<Long> eventIds) {
        return requestRepository.countByEventIdInAndStatus(eventIds, Request.Status.CONFIRMED)
                .stream()
                .collect(Collectors.toMap(
                        count -> (Long) ((Object[]) count)[0],
                        count -> (Long) ((Object[]) count)[1]
                ));
    }

    private Map<Long, Long> getViewsForEvents(List<Long> eventIds) {
        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toList());

        ResponseEntity<Object> response = statsClient.getStats(
                LocalDateTime.now().minusYears(100),
                LocalDateTime.now(),
                uris,
                true
        );

        ObjectMapper mapper = new ObjectMapper();
        try {
            List<ResponseViewStatsDto> stats = mapper.convertValue(
                    response.getBody(),
                    new TypeReference<>() {
                    }
            );

            return stats.stream()
                    .collect(Collectors.toMap(
                            stat -> Long.parseLong(stat.getUri().substring("/events/".length())),
                            ResponseViewStatsDto::getHits
                    ));
        } catch (Exception e) {
            throw new StatsClientException("Failed to get stats " + e.getMessage());
        }
    }

    private Long getViewsForEvent(Event event) {
        final String eventUri = "/events/" + event.getId();

        try {
            ResponseEntity<Object> response = statsClient.getStats(
                    event.getPublished() != null ? event.getPublished() : LocalDateTime.now().minusYears(100),
                    LocalDateTime.now(),
                    List.of(eventUri),
                    true
            );

            ObjectMapper mapper = new ObjectMapper();
            List<ResponseViewStatsDto> stats = mapper.convertValue(
                    response.getBody(),
                    new TypeReference<>() {
                    }
            );

            return stats.isEmpty() ? 0L : stats.get(0).getHits();
        } catch (Exception e) {
            log.error("Failed to get stats for event {}: {}", event.getId(), e.getMessage());
            return 0L;
        }
    }

    private Comparator<EventShortResponseDto> getComparator(String sort) {
        if ("VIEWS".equals(sort)) {
            return Comparator.comparingLong(EventShortResponseDto::getViews);
        }
        return Comparator.comparing(EventShortResponseDto::getEventDate);
    }
}
