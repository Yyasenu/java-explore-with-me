package ru.practicum.ewm.compilation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.event.EventShortResponseDto;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class CompilationResponseDto {
    private Long id;
    private String title;
    private Boolean pinned;
    private Set<EventShortResponseDto> events;
}
