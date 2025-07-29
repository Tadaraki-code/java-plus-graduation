package ru.yandex.practicum.core.category.service;

import ru.yandex.practicum.core.interaction.category.dto.CategoryDto;
import ru.yandex.practicum.core.interaction.category.dto.CategoryParams;
import ru.yandex.practicum.core.interaction.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getCategory(CategoryParams categoryParams);

    CategoryDto getCategoryById(Long catId);

    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    void deleteCategory(Long catId);

    CategoryDto updateCategory(CategoryParams updateCategory);

    List<CategoryDto> getCategoryByIds(List<Long> categoryIds);
}
