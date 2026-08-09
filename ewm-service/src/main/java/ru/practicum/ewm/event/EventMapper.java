package ru.practicum.ewm.event;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.category.CategoryRepository;
import ru.practicum.ewm.category.CategoryResponseDto;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.Request;
import ru.practicum.ewm.request.RequestMapper;
import ru.practicum.ewm.request.RequestResponseDto;
import ru.practicum.ewm.user.UserRepository;
import ru.practicum.ewm.user.UserShortResponseDto;

import java.time.LocalDateTime;
import java.util.List;

@UtilityClass
public class EventMapper {

    public Event toEvent(Long userId, NewEventDto dto,
                                CategoryRepository categoryRepository,
                                UserRepository userRepository) {
        return Event.builder()
                .title(dto.getTitle())
                .annotation(dto.getAnnotation())
                .description(dto.getDescription())
                .eventDate(dto.getEventDate())
                .category(categoryRepository.findById(dto.getCategory())
                        .orElseThrow(() -> new NotFoundException("Category not found")))
                .initiator(userRepository.findById(userId)
                        .orElseThrow(() -> new NotFoundException("User not found")))
                .created(LocalDateTime.now())
                .locationLat(dto.getLocation().getLat())
                .locationLon(dto.getLocation().getLon())
                .paid(dto.getPaid())
                .participantLimit(dto.getParticipantLimit())
                .requestModeration(dto.getRequestModeration())
                .state(Event.EventState.PENDING)
                .build();
    }

    public EventFullResponseDto toFullResponseDto(Event event, Long requests, Long views) {
        return EventFullResponseDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .description(event.getDescription())
                .createdOn(event.getCreated())
                .publishedOn(event.getPublished())
                .eventDate(event.getEventDate())
                .category(new CategoryResponseDto(event.getCategory().getId(), event.getCategory().getName()))
                .initiator(new UserShortResponseDto(event.getInitiator().getId(), event.getInitiator().getName()))
                .location(new Location(event.getLocationLat(), event.getLocationLon()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .state(event.getState().name())
                .confirmedRequests(requests)
                .views(views)
                .build();
    }

    public EventShortResponseDto toShortResponseDto(Event event, Long requests, Long views) {
        return EventShortResponseDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .eventDate(event.getEventDate())
                .category(new CategoryResponseDto(event.getCategory().getId(), event.getCategory().getName()))
                .initiator(new UserShortResponseDto(event.getInitiator().getId(), event.getInitiator().getName()))
                .paid(event.getPaid())
                .confirmedRequests(requests)
                .views(views)
                .build();
    }

    public EventRequestStatusUpdateResult toEventRequestStatusUpdateResult(
            List<Request> toConfirm, List<Request> toReject) {
        List<RequestResponseDto> toConfirmDto = toConfirm.stream()
                .map(RequestMapper::toResponseDto)
                .toList();
        List<RequestResponseDto> toRejectDto = toReject.stream()
                .map(RequestMapper::toResponseDto)
                .toList();
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(toConfirmDto)
                .rejectedRequests(toRejectDto)
                .build();
    }
}
