package ru.practicum.ewm.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.request.RequestResponseDto;

import java.util.List;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateResult {
    private List<RequestResponseDto> confirmedRequests;
    private List<RequestResponseDto> rejectedRequests;
}