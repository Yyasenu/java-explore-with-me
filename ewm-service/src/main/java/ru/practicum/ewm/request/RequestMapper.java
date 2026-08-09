package ru.practicum.ewm.request;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RequestMapper {


    public RequestResponseDto toResponseDto(Request request) {
        return RequestResponseDto.builder()
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .id(request.getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus().name())
                .build();
    }


}
