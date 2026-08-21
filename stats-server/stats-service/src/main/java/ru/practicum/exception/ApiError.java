package main.java.ru.practicum.exception;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ApiError {
    private final String message;
    private final String reason;
    private final String status;
    private final String timestamp;
    private final List<String> errors;
}