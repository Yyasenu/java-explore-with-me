package ru.practicum.ewm.event;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventControllerPublic {
    private final EventService eventService;


    @GetMapping
    public List<EventShortResponseDto> getEvents(
            @RequestParam(required = false)
            String text,

            @RequestParam(required = false)
            List<Long> categories,

            @RequestParam(required = false)
            Boolean paid,

            @RequestParam(required = false)
            LocalDateTime rangeStart,

            @RequestParam(required = false)
            LocalDateTime rangeEnd,

            @RequestParam(defaultValue = "false")
            Boolean onlyAvailable,

            @RequestParam(defaultValue = "EVENT_DATE")
            String sort,

            @RequestParam(defaultValue = "0")
            @PositiveOrZero
            Integer from,

            @RequestParam(defaultValue = "10")
            @Positive
            Integer size,

            HttpServletRequest request) {
        logIpAndPath(request);
        log.info("Get events with params {}, {}, {}, {}, {}, {}, {}, {}, {}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        return eventService.getEventsPublic(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, request);
    }

    @GetMapping("/{id}")
    public EventFullResponseDto getFullEventById(@PathVariable Long id, HttpServletRequest request) {
        logIpAndPath(request);
        log.info("Get event with id {}", id);
        return eventService.getFullEventByIdPublic(id, request);
    }

    private void logIpAndPath(HttpServletRequest request) {
        log.info("client ip: {}", request.getRemoteAddr());
        log.info("endpoint path: {}", request.getRequestURI());
    }
}
