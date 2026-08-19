package ru.practicum.ewm.category;

import java.util.List;

public interface CategoryService {

    CategoryResponseDto addCategoryAdmin(NewCategoryDto newCategoryDto);

    CategoryResponseDto updateCategoryAdmin(Long catId, UpdateCategoryDto updateCategoryDto);

    void deleteCategoryAdmin(Long catId);

    List<CategoryResponseDto> getCategoriesPublic(Integer from, Integer size);

    CategoryResponseDto getCategoryByIdPublic(Long catId);
}
