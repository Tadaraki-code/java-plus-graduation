package ru.yandex.practicum.core.interaction.event.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LocationDto {
    Float lat;
    Float lon;
}