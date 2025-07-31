package ru.yandex.practicum.core.category.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.core.category.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
