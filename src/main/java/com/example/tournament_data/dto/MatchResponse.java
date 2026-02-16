package com.example.tournament_data.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response DTO for match with team names")
public class MatchResponse {

    @Schema(description = "Unique identifier of the match", example = "1")
    private Integer id;

    @Schema(description = "Stadium/Ground where the match is being played", example = "Wankhede Stadium, Mumbai")
    private String venue;

    @Schema(description = "Date and time of the match", example = "2024-04-15T19:30:00")
    private LocalDateTime date;

    @Schema(description = "Name of the first team", example = "Mumbai Indians")
    private String firstTeamName;

    @Schema(description = "Name of the second team", example = "Chennai Super Kings")
    private String secondTeamName;

    @Schema(description = "Current status of the match", example = "COMPLETED")
    private String status;

    @Schema(description = "Match result details")
    private ResultResponse result;
}
