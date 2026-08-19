package ru.practicum.ewm.compilation;

import lombok.experimental.UtilityClass;

import java.util.HashSet;

@UtilityClass
public class CompilationMapper {

    public Compilation toCompilation(NewCompilationDto dto) {
        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned())
                .events(new HashSet<>())
                .build();
    }

    public CompilationResponseDto toResponseDto(Compilation compilation) {
        return CompilationResponseDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(new HashSet<>())
                .build();
    }
}
