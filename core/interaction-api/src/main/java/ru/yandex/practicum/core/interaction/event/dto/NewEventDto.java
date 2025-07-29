package ru.yandex.practicum.core.interaction.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventDto {
    @NotBlank
    @Size(min = 3, max = 120)
    String title;

    @NotBlank
    @Size(min = 20, max = 7000)
    String description;

    @NotBlank
    @Size(min = 20, max = 2000)
    String annotation;

    @NotNull
    Integer category;

    @NotNull
    LocationDto location;

    @Builder.Default
    Boolean requestModeration = true;

    @Builder.Default
    Boolean paid = false;

    @Builder.Default
    Integer participantLimit = 0;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;
}