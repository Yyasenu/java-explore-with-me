package client;

import dto.RequestEndpointHitDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StatsClient extends BaseClient {
    private static final String API_PREFIX = "";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(
            @Value("${stats-service.url}") String serverUrl,
            RestTemplateBuilder builder
    ) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .build()
        );
    }

    public ResponseEntity<Object> saveHit(RequestEndpointHitDto endpointHitDto) {
        return post("/hit", endpointHitDto);
    }

    public ResponseEntity<Object> getStats(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            boolean unique
    ) {
        try {
            Map<String, Object> parameters = Map.of(
                    "start", URLEncoder.encode(start.format(FORMATTER), StandardCharsets.UTF_8),
                    "end", URLEncoder.encode(end.format(FORMATTER), StandardCharsets.UTF_8),
                    "uris", uris.stream()
                            .map(uri -> URLEncoder.encode(uri, StandardCharsets.UTF_8))
                            .collect(Collectors.joining(",")),
                    "unique", unique
            );

            return get("/stats", parameters);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode URL parameters", e);
        }
    }
}