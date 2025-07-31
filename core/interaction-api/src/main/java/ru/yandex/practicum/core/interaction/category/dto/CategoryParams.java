package ru.yandex.practicum.core.interaction.category.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryParams {

    NewCategoryDto newCategoryDto;

    Long catId;

    Long from;

    Long size;
}
