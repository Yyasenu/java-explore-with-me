package ru.practicum.endpointhit;


import dto.RequestEndpointHitDto;
import dto.ResponseViewStatsDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class EndpointHitController {
    private final EndpointHitService endpointHitService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void addHit(@Valid @RequestBody RequestEndpointHitDto hitDto) {
        log.info("Add hit {}", hitDto);
        endpointHitService.addHit(hitDto);
    }

    @GetMapping("/stats")
    public List<ResponseViewStatsDto> getStats(
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime start,

            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime end,

            @RequestParam(required = false)
            List<String> uris,

            @RequestParam(defaultValue = "false")
            boolean unique
    ) {
        log.info("Get stats {} - {}, uris {}, unique {}", start, end, uris, unique);
        return endpointHitService.getStats(start, end, uris, unique);
    }

}
