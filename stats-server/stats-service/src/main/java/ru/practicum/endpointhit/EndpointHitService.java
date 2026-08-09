package ru.practicum.endpointhit;

import dto.RequestEndpointHitDto;
import dto.ResponseViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EndpointHitService {

    void addHit(RequestEndpointHitDto hitDto);

    List<ResponseViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
