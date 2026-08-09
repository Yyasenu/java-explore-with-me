package ru.practicum.ewm.category;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryControllerPublic {
    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponseDto> getCategories(
            @RequestParam(defaultValue = "0")
            @PositiveOrZero
            Integer from,

            @RequestParam(defaultValue = "10")
            @Positive
            Integer size) {

        log.info("Getting categories with params {}, {}", from, size);
        return categoryService.getCategoriesPublic(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryResponseDto getCategoryById(@PathVariable Long catId) {
        log.info("Getting category by id: {}", catId);
        return categoryService.getCategoryByIdPublic(catId);
    }
}
