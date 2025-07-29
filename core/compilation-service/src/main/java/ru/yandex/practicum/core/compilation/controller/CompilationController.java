package ru.yandex.practicum.core.compilation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.core.compilation.service.CompilationService;
import ru.yandex.practicum.core.interaction.compilation.dto.CompilationDto;
import ru.yandex.practicum.core.interaction.compilation.dto.CompilationParams;
import ru.yandex.practicum.core.interaction.compilation.dto.NewCompilationDto;

import java.util.List;

import static ru.yandex.practicum.core.interaction.compilation.constants.CompilationConstants.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CompilationController {


    private final CompilationService compilationService;

    @GetMapping(PUBLIC_API_PREFIX)
    @ResponseStatus(HttpStatus.OK)
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @RequestParam(defaultValue = "0") Long from,
                                                @RequestParam(defaultValue = "10") Long size) {
        log.info("Request: get compilations with pinned={}, from={}, size={}", pinned, from, size);
        CompilationParams compilationParams = CompilationParams
                .builder()
                .pinned(pinned)
                .from(from)
                .size(size)
                .build();
        return compilationService.getCompilations(compilationParams);
    }

    @GetMapping(PUBLIC_API_PREFIX + COMP_ID_PATH)
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto getCompilationById(@PathVariable(COMP_ID) Long compId) {
        log.info("Request: get compilation with id={}", compId);
        return compilationService.getCompilationById(compId);
    }

    @PostMapping(ADMIN_API_PREFIX)
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createdCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        log.info("Request: post compilation={}", newCompilationDto);
        return compilationService.createdCompilation(newCompilationDto);
    }

    @DeleteMapping(ADMIN_API_PREFIX + COMP_ID_PATH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable(COMP_ID) Long compId) {
        log.info("Request: delete compilation with id={}", compId);
        compilationService.deleteCompilation(compId);
    }

    @PatchMapping(ADMIN_API_PREFIX + COMP_ID_PATH)
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto updateCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto,
                                            @PathVariable(COMP_ID) Long compId) {
        log.info("Request: update compilation with id={}, update compilation={}", compId, newCompilationDto);

        CompilationParams compilationParams = CompilationParams
                .builder()
                .compId(compId)
                .newCompilationDto(newCompilationDto)
                .build();

        return compilationService.updateCompilation(compilationParams);
    }

}
