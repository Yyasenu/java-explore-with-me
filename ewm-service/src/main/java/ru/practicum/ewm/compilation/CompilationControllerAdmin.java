package ru.practicum.ewm.compilation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/compilations")
public class CompilationControllerAdmin {
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationResponseDto createCompilation(
            @Valid @RequestBody NewCompilationDto newCompilationDto) {
        log.info("Create compilation with title {}", newCompilationDto.getTitle());
        return compilationService.createCompilationAdmin(newCompilationDto);
    }

    @PatchMapping("/{compId}")
    public CompilationResponseDto updateCompilation(
            @PathVariable Long compId,
            @Valid @RequestBody UpdateCompilationDto updateDto) {
        log.info("Update compilation id {}", compId);
        return compilationService.updateCompilationAdmin(compId, updateDto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        log.info("Delete compilation id {}", compId);
        compilationService.deleteCompilationAdmin(compId);
    }
}
