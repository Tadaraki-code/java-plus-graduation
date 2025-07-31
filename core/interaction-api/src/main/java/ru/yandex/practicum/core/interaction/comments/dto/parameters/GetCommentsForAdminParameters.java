package ru.yandex.practicum.core.interaction.comments.dto.parameters;

import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.core.interaction.comments.enums.CommentStatus;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GetCommentsForAdminParameters {
    CommentStatus status;

    @Min(value = 0, message = "Parameters 'from' can not be less then zero")
    Integer from;

    @Min(value = 1, message = "Parameters 'size' can not be less then one")
    Integer size;

    public GetCommentsForAdminParameters() {
        this.from = 0;
        this.size = 10;
    }
}
