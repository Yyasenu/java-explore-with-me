package ru.practicum.endpointhit;

import dto.RequestEndpointHitDto;
import dto.ResponseViewStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.InvalidDateRangeException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EndpointHitServiceImpl implements EndpointHitService {
    private final EndpointHitRepository endpointHitRepository;
    private final Set<String> services = Set.of("ewm-main-service");


    @Override
    @Transactional
    public void addHit(RequestEndpointHitDto hitDto) {
        validateHit(hitDto);
        EndpointHit endpointHit = EndpointHitMapper.toEndpointHit(hitDto);
        endpointHitRepository.save(endpointHit);
    }

    @Override
    public List<ResponseViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {

        if (start.isAfter(end)) {
            throw new InvalidDateRangeException("Start date cannot be after end date");
        }

        return unique
                ? endpointHitRepository.getUniqueStats(start, end, uris)
                : endpointHitRepository.getAllStats(start, end, uris);
    }


    private void validateHit(RequestEndpointHitDto hitDto) {
        if (!services.contains(hitDto.getApp())) {
            throw new NotFoundException("Application " + hitDto.getApp() + " not found");
        }
    }
}