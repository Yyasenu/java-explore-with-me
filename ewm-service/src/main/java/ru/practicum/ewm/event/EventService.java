package ru.practicum.ewm.event;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.request.RequestResponseDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface EventService {

    List<EventFullResponseDto> getEventsAdmin(
            List<Long> userIds, List<String> states, List<Long> categories,
            LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    EventFullResponseDto updateEventAdmin(Long eventId, UpdateEventAdminDto updateDto);

    EventFullResponseDto createEventPrivate(Long userId, NewEventDto eventDto);

    EventFullResponseDto getFullEventByIdPrivate(Long userId, Long eventId);

    List<EventShortResponseDto> getEventsPrivate(Long userId, Integer from, Integer size);

    EventFullResponseDto updateEventPrivate(Long userId, Long eventId, UpdateEventUserDto updateDto);

    EventRequestStatusUpdateResult updateRequestStatusesPrivate(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest);

    List<RequestResponseDto> getRequestsByEventPrivate(Long userId, Long eventId);

    List<EventShortResponseDto> getEventsPublic(
            String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
            LocalDateTime rangeEnd, Boolean onlyAvailable, String sort,
            Integer from, Integer size, HttpServletRequest request);

    EventFullResponseDto getFullEventByIdPublic(Long eventId, HttpServletRequest request);

    Set<EventShortResponseDto> toShortDtos(Set<Event> events);

}
