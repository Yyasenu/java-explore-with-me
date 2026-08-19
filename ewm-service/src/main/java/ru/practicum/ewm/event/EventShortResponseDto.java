package ru.practicum.ewm.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.category.CategoryResponseDto;
import ru.practicum.ewm.user.UserShortResponseDto;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class EventShortResponseDto {
    private Long id;
    private String title;
    private String annotation;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private CategoryResponseDto category;
    private UserShortResponseDto initiator;
    private Boolean paid;
    private Long confirmedRequests;
    private Long views;
}
