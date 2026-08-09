package ru.practicum.ewm.category;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class CategoryControllerAdmin {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDto createCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        log.info("Creating new category with name {}", newCategoryDto.getName());
        return categoryService.addCategoryAdmin(newCategoryDto);
    }

    @PatchMapping("/{catId}")
    public CategoryResponseDto updateCategory(
            @PathVariable Long catId,
            @Valid @RequestBody UpdateCategoryDto updateCategoryDto) {
        log.info("Updating category with id: {}", catId);
        return categoryService.updateCategoryAdmin(catId, updateCategoryDto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        log.info("Deleting category with id: {}", catId);
        categoryService.deleteCategoryAdmin(catId);
    }
}
