package ru.yandex.practicum.core.interaction.event.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.core.interaction.event.enums.UserUpdateRequestAction;


import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventRequestStatusUpdateRequest {
    @NotNull
    @Size(min = 1)
    List<Long> requestIds;

    @NotNull
    UserUpdateRequestAction status;
}