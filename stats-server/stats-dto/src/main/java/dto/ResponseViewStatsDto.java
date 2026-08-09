package dto;

import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseViewStatsDto {
    private String app;
    private String uri;
    private Long hits;
}