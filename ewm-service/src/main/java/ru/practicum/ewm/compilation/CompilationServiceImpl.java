package ru.practicum.ewm.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.event.EventService;
import ru.practicum.ewm.event.EventShortResponseDto;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;

    @Override
    public CompilationResponseDto createCompilationAdmin(NewCompilationDto dto) {
        Compilation compilation = CompilationMapper.toCompilation(dto);

        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(dto.getEvents()));
            compilation.setEvents(events);
        }

        Compilation saved = compilationRepository.save(compilation);
        return toFullResponseDto(saved);
    }

    @Override
    public CompilationResponseDto updateCompilationAdmin(Long compId, UpdateCompilationDto updateDto) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found " + compId));

        if (updateDto.getTitle() != null) {
            compilation.setTitle(updateDto.getTitle());
        }

        if (updateDto.getPinned() != null) {
            compilation.setPinned(updateDto.getPinned());
        }

        if (updateDto.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(updateDto.getEvents()));
            compilation.setEvents(events);
        }

        Compilation updated = compilationRepository.save(compilation);
        return toFullResponseDto(updated);
    }

    @Override
    public void deleteCompilationAdmin(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id " + compId + " was not found"));
        compilationRepository.delete(compilation);
    }

    @Override
    public List<CompilationResponseDto> getCompilationsPublic(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Compilation> compilations;

        if (pinned != null) {
            compilations = compilationRepository.findByPinned(pinned, pageable);
        } else {
            compilations = compilationRepository.findAll(pageable);
        }

        return compilations.stream()
                .map(this::toFullResponseDto)
                .toList();
    }

    @Override
    public CompilationResponseDto getCompilationByIdPublic(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id " + compId + " was not found"));

        return toFullResponseDto(compilation);
    }


    private CompilationResponseDto toFullResponseDto(Compilation compilation) {
        Set<EventShortResponseDto> eventDtos = compilation.getEvents().isEmpty() ?
                Set.of() :
                eventService.toShortDtos(compilation.getEvents());

        return CompilationResponseDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(eventDtos)
                .build();
    }

}
