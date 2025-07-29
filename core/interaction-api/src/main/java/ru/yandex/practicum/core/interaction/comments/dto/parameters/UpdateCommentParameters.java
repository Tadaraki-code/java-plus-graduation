package ru.yandex.practicum.core.interaction.comments.dto.parameters;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.core.interaction.comments.dto.UpdateCommentDto;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCommentParameters {

    Long userId;
    Long commentId;
    UpdateCommentDto updateCommentDto;
}
