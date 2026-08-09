package ru.practicum.ewm.request;

import java.util.List;

public interface RequestService {

    RequestResponseDto addRequestPrivate(Long userId, Long eventId);

    List<RequestResponseDto> getUserRequestsPrivate(Long userId);

    RequestResponseDto cancelRequestPrivate(Long userId, Long requestId);
}
