package ru.practicum.ewm.events.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.events.enums.UserUpdateRequestAction;

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