package ru.yandex.practicum.core.category.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.core.category.model.Category;
import ru.yandex.practicum.core.interaction.category.dto.CategoryDto;
import ru.yandex.practicum.core.interaction.category.dto.NewCategoryDto;

@Component
public class CategoryMapper {

    public static CategoryDto toCategoryDto(Category category) {

        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public static Category toCategoryEntity(NewCategoryDto newCategoryDto) {

        return Category.builder()
                .name(newCategoryDto.getName())
                .build();
    }

}

