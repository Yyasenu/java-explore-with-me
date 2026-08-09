package ru.practicum.ewm.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class NewCategoryDto {

    @NotBlank
    @Size(min = 1, max = 50, message = "Название должно содержать от 1 до 50 символов")
    private String name;
}
