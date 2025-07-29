package ru.yandex.practicum.core.interaction.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class Util {
    public static LocalDateTime getNowTruncatedToSeconds() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
}
