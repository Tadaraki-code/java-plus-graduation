package ru.yandex.practicum.core.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.core.category.mapper.CategoryMapper;
import ru.yandex.practicum.core.category.model.Category;
import ru.yandex.practicum.core.category.storage.CategoryRepository;
import ru.yandex.practicum.core.interaction.category.dto.CategoryDto;
import ru.yandex.practicum.core.interaction.category.dto.CategoryParams;
import ru.yandex.practicum.core.interaction.category.dto.NewCategoryDto;
import ru.yandex.practicum.core.interaction.clients.EventClient;
import ru.yandex.practicum.core.interaction.error.exception.ConflictException;
import ru.yandex.practicum.core.interaction.error.exception.NotFoundException;
import ru.yandex.practicum.core.interaction.error.exception.ValidationException;


import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventClient eventClient;


    @Override
    public List<CategoryDto> getCategory(CategoryParams categoryParams) {
        if (categoryParams.getFrom() < 0 || categoryParams.getSize() <= 0) {
            throw new IllegalArgumentException(
                    "Parameters 'from' must be >= 0 and 'size' must be > 0 for category pagination"
            );
        }

        int pageNumber = (int) (categoryParams.getFrom() / categoryParams.getSize());
        int pageSize = categoryParams.getSize().intValue();

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        if (categoryParams.getFrom() > categoryPage.getTotalElements()) {
            return Collections.emptyList();
        }
        log.info("Fetching categories: from={}, size={}", categoryParams.getFrom(), pageSize);
        return categoryPage.stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        log.info("Fetching category by id: {}", catId);
        Category category = checkAndGetCategory(catId);

        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        log.info("Creating category: {}", newCategoryDto.getName());
        if (newCategoryDto.getName() == null || newCategoryDto.getName().isBlank()) {
            throw new ValidationException("Category name can't be null or blank");
        }

        if (newCategoryDto.getName().length() > 50) {
            throw new ValidationException("Category name can't be bigger then 50");
        }

        Category category = CategoryMapper.toCategoryEntity(newCategoryDto);

        try {
            Category savedCategory = categoryRepository.save(category);
            return CategoryMapper.toCategoryDto(savedCategory);
        } catch (DataIntegrityViolationException e) {
            log.error("Conflict when create category with name: {}", newCategoryDto.getName());
            throw new ConflictException(String.format("Category with name '%s' already exists", newCategoryDto.getName()));
        }
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        checkAndGetCategory(catId);

        if (eventClient.getEventsCountByCategoryId(catId) > 0) {
            throw new ConflictException(String.format("Category with id '%d' is not empty", catId));
        }
        log.info("Deleting category id: {}", catId);
        categoryRepository.deleteById(catId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(CategoryParams updateCategory) {
        Long categoryId = updateCategory.getCatId();
        NewCategoryDto newCategoryDto = updateCategory.getNewCategoryDto();

        Category oldCategory = checkAndGetCategory(updateCategory.getCatId());

        if (newCategoryDto.getName() == null || newCategoryDto.getName().isBlank()) {
            throw new IllegalArgumentException("Category name for update can't be null or blank");
        }

        if (newCategoryDto.getName().length() > 50) {
            throw new ValidationException("Category name can't be bigger then 50");
        }

        log.info("Updating category id={} with new name: {}", categoryId, newCategoryDto.getName());

        try {
            oldCategory.setName(newCategoryDto.getName());
            categoryRepository.saveAndFlush(oldCategory);
            return CategoryMapper.toCategoryDto(oldCategory);
        } catch (DataIntegrityViolationException e) {
            log.error("Conflict when trying update category id={} with new name: {}",
                    categoryId, newCategoryDto.getName());
            throw new ConflictException(String.format("Cannot update category id %d : name '%s' already exists",
                    categoryId, newCategoryDto.getName()));
        }
    }

    @Override
    public List<CategoryDto> getCategoryByIds(List<Long> categoryIds) {
        log.info("Fetching category by ids: ");
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }
        List<Category> category = categoryRepository.findAllById(categoryIds);

        return category.stream()
                .map(CategoryMapper::toCategoryDto)
                .toList();
    }

    private Category checkAndGetCategory(Long catId) {
        return categoryRepository
                .findById(catId)
                .orElseThrow(() -> new NotFoundException(String.format("Category with id '%d' not found", catId)));
    }
}
