package ru.practicum.endpointhit;

import dto.RequestEndpointHitDto;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EndpointHitMapper {
    public EndpointHit toEndpointHit(RequestEndpointHitDto dto) {
        return EndpointHit.builder()
                .id(null)
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }
}
