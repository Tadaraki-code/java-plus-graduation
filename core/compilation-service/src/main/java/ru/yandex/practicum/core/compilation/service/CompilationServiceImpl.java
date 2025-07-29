package ru.yandex.practicum.core.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.core.compilation.mapper.CompilationMapper;
import ru.yandex.practicum.core.compilation.model.Compilation;
import ru.yandex.practicum.core.compilation.storage.CompilationRepository;
import ru.yandex.practicum.core.interaction.clients.EventClient;
import ru.yandex.practicum.core.interaction.compilation.dto.CompilationDto;
import ru.yandex.practicum.core.interaction.compilation.dto.CompilationParams;
import ru.yandex.practicum.core.interaction.compilation.dto.NewCompilationDto;
import ru.yandex.practicum.core.interaction.error.exception.ClientApiException;
import ru.yandex.practicum.core.interaction.error.exception.NotFoundException;
import ru.yandex.practicum.core.interaction.error.exception.ValidationException;
import ru.yandex.practicum.core.interaction.event.dto.EventShortDto;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventClient eventClient;

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(CompilationParams compilationParams) {
        if (compilationParams.getFrom() < 0 || compilationParams.getSize() <= 0) {
            throw new ValidationException("Invalid pagination parameters");
        }

        int pageNumber = (int) (compilationParams.getFrom() / compilationParams.getSize());
        int pageSize = compilationParams.getSize().intValue();

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<Compilation> compilationPage = compilationParams.getPinned() != null
                ? compilationRepository.findByPinned(compilationParams.getPinned(), pageable)
                : compilationRepository.findAll(pageable);

        log.info("Fetched compilations: from={}, size={}, pinned={}",
                compilationParams.getFrom(), compilationParams.getSize(), compilationParams.getPinned());

        return compilationPage.stream()
                .map(compilation -> CompilationMapper
                        .toCompilationDto(compilation, getEventShortDtoList(compilation.getEventsId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = checkAndGetCompilation(compId);
        log.info("Get compilations with id = {}", compId);
        return CompilationMapper.toCompilationDto(compilation, getEventShortDtoList(compilation.getEventsId()));
    }

    @Override
    @Transactional
    public CompilationDto createdCompilation(NewCompilationDto newCompilationDto) {
        if (newCompilationDto.getTitle() == null || newCompilationDto.getTitle().isBlank()) {
            throw new ValidationException("Compilation title cannot be null or blank");
        }

        if (newCompilationDto.getEvents() == null) {
            newCompilationDto.setEvents(new ArrayList<>());
        }

        try {
            if (!eventClient.chekEventExistingByIds(newCompilationDto.getEvents())) {
                throw new NotFoundException("Not all received events were found");
            }
        } catch (ClientApiException e) {
            throw e;
        }

        Compilation compilation = CompilationMapper.toCompilationEntity(newCompilationDto);
        Compilation savedCompilation = compilationRepository.save(compilation);

        log.info("Created compilation id={} with title '{}'", savedCompilation.getId(), savedCompilation.getTitle());

        return CompilationMapper
                .toCompilationDto(savedCompilation, getEventShortDtoList(savedCompilation.getEventsId()));
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {

        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException(String.format("Compilation with id %d not found", compId));
        }

        compilationRepository.deleteById(compId);
        log.info("Deleted compilation id={}", compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(CompilationParams compilationParams) {
        NewCompilationDto newCompilationDto = compilationParams.getNewCompilationDto();

        if (newCompilationDto.getEvents() == null) {
            newCompilationDto.setEvents(new ArrayList<>());
        }

        Compilation compilation = checkAndGetCompilation(compilationParams.getCompId());

        if (newCompilationDto.getTitle() != null && !newCompilationDto.getTitle().isBlank()) {
            compilation.setTitle(newCompilationDto.getTitle());
        }

        compilation.setPinned(newCompilationDto.isPinned());

        if (!newCompilationDto.getEvents().isEmpty()) {
            try {
                if (!eventClient.chekEventExistingByIds(newCompilationDto.getEvents())) {
                    throw new NotFoundException("Not all received events were found");
                }
                compilation.setEventsId(newCompilationDto.getEvents());
            } catch (ClientApiException e) {
                throw e;
            }
        }

        Compilation updatedCompilation = compilationRepository.save(compilation);
        log.info("Updated compilation id={} with title '{}'", updatedCompilation.getId(),
                updatedCompilation.getTitle());

        return CompilationMapper.toCompilationDto(updatedCompilation,
                getEventShortDtoList(updatedCompilation.getEventsId()));
    }

    private List<EventShortDto> getEventShortDtoList(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return new ArrayList<>();
        }

        return eventClient.getEventsShortDtoByIds(eventIds);
    }

    private Compilation checkAndGetCompilation(Long compId) {
        return compilationRepository
                .findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id %d not found", compId)));
    }
}
