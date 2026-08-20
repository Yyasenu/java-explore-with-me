package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler({NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException e) {
        log.warn("Not found error: {}", e.getMessage());
        return buildApiError(
                e.getMessage(),
                "The required object was not found.",
                HttpStatus.NOT_FOUND,
                Collections.singletonList(e.getMessage())
        );
    }

    @ExceptionHandler({InvalidDateRangeException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidDateRangeException(final InvalidDateRangeException e) {
        log.warn("Invalid date range error: {}", e.getMessage());
        return buildApiError(
                e.getMessage(),
                "Incorrectly made request.",
                HttpStatus.BAD_REQUEST,
                Collections.singletonList(e.getMessage())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValid(final MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        log.warn("Validation failed for request: {}", errors);
        return buildApiError(
                "Validation failed",
                "Incorrectly made request.",
                HttpStatus.BAD_REQUEST,
                errors
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingParams(MissingServletRequestParameterException e) {
        String msg = "Required parameter '" + e.getParameterName() + "' is missing";
        log.warn(msg);
        return buildApiError(
                msg,
                "Incorrectly made request",
                HttpStatus.BAD_REQUEST,
                Collections.singletonList(e.getMessage())
        );
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingHeader(final MissingRequestHeaderException e) {
        String headerName = e.getHeaderName();
        String msg = "Missing header: " + headerName;
        log.warn(msg);
        return buildApiError(
                msg,
                "Incorrectly made request.",
                HttpStatus.BAD_REQUEST,
                Collections.singletonList("Отсутствует обязательный заголовок: " + headerName)
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleHttpMessageNotReadable(final HttpMessageNotReadableException e) {
        log.warn("JSON parse error: {}", e.getMessage());
        return buildApiError(
                "JSON parse error",
                "Incorrectly made request.",
                HttpStatus.BAD_REQUEST,
                Collections.singletonList(e.getMessage())
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgument(final IllegalArgumentException e) {
        log.error("Illegal argument error: {}", e.getMessage(), e);
        return buildApiError(
                e.getMessage(),
                "Incorrectly made request.",
                HttpStatus.BAD_REQUEST,
                Collections.singletonList(e.getMessage())
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleErrors(final Throwable e) {
        log.error("Unhandled exception occurred", e);
        return buildApiError(
                "Internal server error",
                "Произошла непредвиденная ошибка.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                Collections.singletonList("An unexpected error occurred")
        );
    }

    private ApiError buildApiError(String message, String reason, HttpStatus status, List<String> errors) {
        return ApiError.builder()
                .message(message)
                .reason(reason)
                .status(status.name())
                .timestamp(LocalDateTime.now().format(TIMESTAMP_FORMATTER))
                .errors(errors)
                .build();
    }
}