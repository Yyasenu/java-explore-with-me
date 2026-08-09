package ru.practicum.ewm.category;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class UpdateCategoryDto {
    @Size(min = 1, max = 50, message = "Название должно содержать от 1 до 50 символов")
    private String name;
}