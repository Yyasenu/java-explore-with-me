package ru.practicum.ewm.category;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryResponseDto addCategoryAdmin(NewCategoryDto newCategoryDto) {
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new ConflictException("Category with name '" + newCategoryDto.getName() + "' already exists");
        }

        Category category = CategoryMapper.toCategory(newCategoryDto);
        Category savedCategory = categoryRepository.save(category);

        return CategoryMapper.toCategoryDto(savedCategory);
    }

    @Override
    @Transactional
    public CategoryResponseDto updateCategoryAdmin(Long catId, UpdateCategoryDto updateCategoryDto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category " + catId + " was not found"));

        if (updateCategoryDto.getName() != null &&
                !updateCategoryDto.getName().equals(category.getName()) &&
                categoryRepository.existsByName(updateCategoryDto.getName())) {
            throw new ConflictException("Category with name '" + updateCategoryDto.getName() + "' already exists");
        }

        if (updateCategoryDto.getName() != null) {
            category.setName(updateCategoryDto.getName());
        }

        Category updatedCategory = categoryRepository.save(category);
        return CategoryMapper.toCategoryDto(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategoryAdmin(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));

        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("The category is not empty");
        }

        categoryRepository.delete(category);
    }

    @Override
    public List<CategoryResponseDto> getCategoriesPublic(Integer from, Integer size) {
        Page<Category> categories = categoryRepository.findAll(
                PageRequest.of(from / size, size, Sort.by("id").ascending()));

        return categories.stream()
                .map(CategoryMapper::toCategoryDto)
                .toList();
    }

    @Override
    public CategoryResponseDto getCategoryByIdPublic(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id " + catId + " was not found"));

        return CategoryMapper.toCategoryDto(category);
    }

}
