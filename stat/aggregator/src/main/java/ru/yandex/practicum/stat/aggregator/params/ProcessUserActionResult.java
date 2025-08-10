package ru.yandex.practicum.stat.aggregator.params;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProcessUserActionResult {
    Boolean result;
    Double oldWeight;
}
