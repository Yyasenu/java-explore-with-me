package ru.practicum.ewm.compilation;

import java.util.List;

public interface CompilationService {
    CompilationResponseDto createCompilationAdmin(NewCompilationDto compilationDto);

    CompilationResponseDto updateCompilationAdmin(Long compId, UpdateCompilationDto updateDto);

    void deleteCompilationAdmin(Long compId);

    List<CompilationResponseDto> getCompilationsPublic(Boolean pinned, int from, int size);

    CompilationResponseDto getCompilationByIdPublic(Long compId);
}
