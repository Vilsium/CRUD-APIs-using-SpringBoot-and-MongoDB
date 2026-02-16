package com.example.tournament_data.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request DTO for partially updating a match")
public class MatchPatchRequest {

    @Size(min = 2, max = 150, message = "Venue must be between 2 and 150 characters")
    @Schema(description = "Stadium/Ground where the match is being played", example = "Wankhede Stadium, Mumbai", nullable = true)
    private String venue;

    @Schema(description = "Date and time of the match", example = "2024-04-15T19:30:00", nullable = true)
    private LocalDateTime date;

    @Size(min = 2, max = 100, message = "Team name must be between 2 and 100 characters")
    @Schema(description = "Name of the first team", example = "Mumbai Indians", nullable = true)
    private String firstTeamName;

    @Size(min = 2, max = 100, message = "Team name must be between 2 and 100 characters")
    @Schema(description = "Name of the second team", example = "Chennai Super Kings", nullable = true)
    private String secondTeamName;

    @Pattern(regexp = "^(SCHEDULED|COMPLETED)$", message = "Status must be: SCHEDULED or COMPLETED")
    @Schema(description = "Current status of the match", example = "COMPLETED", nullable = true)
    private String status;

    @Valid
    @Schema(description = "Match result details (only when changing to COMPLETED)", nullable = true)
    private ResultCreateRequest result;
}
