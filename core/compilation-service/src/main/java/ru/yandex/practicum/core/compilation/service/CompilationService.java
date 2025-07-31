package ru.yandex.practicum.core.compilation.service;


import ru.yandex.practicum.core.interaction.compilation.dto.CompilationDto;
import ru.yandex.practicum.core.interaction.compilation.dto.CompilationParams;
import ru.yandex.practicum.core.interaction.compilation.dto.NewCompilationDto;

import java.util.List;

public interface CompilationService {

    List<CompilationDto> getCompilations(CompilationParams compilationParams);

    CompilationDto getCompilationById(Long compId);

    CompilationDto createdCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(Long compId);

    CompilationDto updateCompilation(CompilationParams compilationParams);
}
