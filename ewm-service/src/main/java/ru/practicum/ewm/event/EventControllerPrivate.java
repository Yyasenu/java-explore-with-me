package ru.practicum.ewm.event;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.request.RequestResponseDto;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class EventControllerPrivate {
    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullResponseDto createEvent(
            @PathVariable Long userId,
            @RequestBody @Valid NewEventDto eventDto) {
        log.info("Create event with title {} by user {}", eventDto.getTitle(), userId);
        return eventService.createEventPrivate(userId, eventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullResponseDto getFullEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        log.info("Get event {} by user {}", eventId, userId);
        return eventService.getFullEventByIdPrivate(userId, eventId);
    }

    @GetMapping
    public List<EventShortResponseDto> getEvents(
            @PathVariable Long userId,

            @RequestParam(defaultValue = "0")
            @PositiveOrZero
            Integer from,

            @RequestParam(defaultValue = "10")
            @Positive
            Integer size) {
        log.info("Get events by user {}", userId);
        return eventService.getEventsPrivate(userId, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullResponseDto updateEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventUserDto updateDto) {
        log.info("Update event {} by user {}", eventId, userId);
        return eventService.updateEventPrivate(userId, eventId, updateDto);
    }

    @GetMapping("/{eventId}/requests")
    public List<RequestResponseDto> getRequestsByEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        log.info("Get requests by event {} by user {}", eventId, userId);
        return eventService.getRequestsByEventPrivate(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatuses(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        log.info("Update status of requests by event {} by user {}", eventId, userId);
        return eventService.updateRequestStatusesPrivate(userId, eventId, updateRequest);
    }
}
