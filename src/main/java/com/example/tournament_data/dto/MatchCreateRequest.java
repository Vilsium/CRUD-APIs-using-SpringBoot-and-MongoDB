package com.example.tournament_data.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Request DTO for creating a match with team names")
public class MatchCreateRequest {

    @NotBlank(message = "Venue is required")
    @Size(min = 2, max = 150, message = "Venue must be between 2 and 150 characters")
    @Schema(description = "Stadium/Ground where the match is being played", example = "Wankhede Stadium, Mumbai", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 150)
    private String venue;

    @NotNull(message = "Match date and time is required")
    @Schema(description = "Date and time of the match (ISO 8601 format)", example = "2024-04-15T19:30:00", requiredMode = Schema.RequiredMode.REQUIRED, type = "string", format = "date-time")
    private LocalDateTime date;

    @NotBlank(message = "First team name is required")
    @Size(min = 2, max = 100, message = "Team name must be between 2 and 100 characters")
    @Schema(description = "Name of the first team (home team)", example = "Mumbai Indians", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 100)
    private String firstTeamName;

    @NotBlank(message = "Second team name is required")
    @Size(min = 2, max = 100, message = "Team name must be between 2 and 100 characters")
    @Schema(description = "Name of the second team (away team)", example = "Chennai Super Kings", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 100)
    private String secondTeamName;

    @NotBlank(message = "Match status is required")
    @Pattern(regexp = "^(SCHEDULED|COMPLETED)$", message = "Status must be: SCHEDULED or COMPLETED")
    @Schema(description = "Current status of the match", example = "SCHEDULED", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {
            "SCHEDULED", "COMPLETED" })
    private String status;

    @Valid
    @Schema(description = "Match result details (only applicable when status is COMPLETED)", nullable = true)
    private ResultCreateRequest result;
}
