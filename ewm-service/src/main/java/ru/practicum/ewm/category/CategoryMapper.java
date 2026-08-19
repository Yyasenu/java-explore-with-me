package ru.practicum.ewm.category;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;

@UtilityClass
public class CategoryMapper {
    public Category toCategory(NewCategoryDto newCategoryDto) {
        return Category.builder()
                .id(null)
                .name(newCategoryDto.getName())
                .events(new ArrayList<>())
                .build();
    }

    public CategoryResponseDto toCategoryDto(Category category) {
        return CategoryResponseDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
