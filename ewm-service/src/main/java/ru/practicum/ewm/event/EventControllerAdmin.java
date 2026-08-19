package ru.practicum.ewm.event;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class EventControllerAdmin {
    private final EventService eventService;

    @GetMapping
    public List<EventFullResponseDto> getEvents(
            @RequestParam(required = false)
            List<Long> users,

            @RequestParam(required = false)
            List<String> states,

            @RequestParam(required = false)
            List<Long> categories,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime rangeStart,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime rangeEnd,

            @RequestParam(defaultValue = "0")
            Integer from,

            @RequestParam(defaultValue = "10")
            Integer size
    ) {
        log.info("Get events with params {}, {}, {}, {}, {}, {}, {}",
                users, states, categories, rangeStart, rangeEnd, from, size);
        return eventService.getEventsAdmin(
                users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullResponseDto updateEventByAdmin(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventAdminDto updateDto) {
        log.info("Admin updating event {}, body {}", eventId, updateDto);
        return eventService.updateEventAdmin(eventId, updateDto);
    }
}
