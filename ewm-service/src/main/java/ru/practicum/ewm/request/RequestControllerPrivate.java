package ru.practicum.ewm.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/users/{userId}/requests")
public class RequestControllerPrivate {
    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestResponseDto createRequest(
            @PathVariable Long userId,
            @RequestParam Long eventId) {

        log.info("Creating request from user {} to event {}", userId, eventId);
        return requestService.addRequestPrivate(userId, eventId);
    }

    @GetMapping
    public List<RequestResponseDto> getUserRequests(@PathVariable Long userId) {
        log.info("Getting requests for user with id: {}", userId);
        return requestService.getUserRequestsPrivate(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public RequestResponseDto cancelRequest(
            @PathVariable Long userId,
            @PathVariable Long requestId) {

        log.info("User {} canceling request {}", userId, requestId);
        return requestService.cancelRequestPrivate(userId, requestId);
    }

}
