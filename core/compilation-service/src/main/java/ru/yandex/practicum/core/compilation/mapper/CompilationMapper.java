package ru.yandex.practicum.core.compilation.mapper;



import ru.yandex.practicum.core.compilation.model.Compilation;
import ru.yandex.practicum.core.interaction.compilation.dto.CompilationDto;
import ru.yandex.practicum.core.interaction.compilation.dto.NewCompilationDto;
import ru.yandex.practicum.core.interaction.event.dto.EventShortDto;

import java.util.List;

public class CompilationMapper {

    public static CompilationDto toCompilationDto(Compilation compilation, List<EventShortDto> eventsShortDto) {

        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.isPinned())
                .events(eventsShortDto)
                .build();
    }

    public static Compilation toCompilation(CompilationDto compilationDto, List<Long> events) {

        return Compilation.builder()
                .id(compilationDto.getId())
                .title(compilationDto.getTitle())
                .pinned(compilationDto.isPinned())
                .eventsId(events)
                .build();
    }

    public static Compilation toCompilationEntity(NewCompilationDto newCompilationDto) {

        return Compilation.builder()
                .title(newCompilationDto.getTitle())
                .pinned(newCompilationDto.isPinned())
                .eventsId(newCompilationDto.getEvents())
                .build();
    }
}