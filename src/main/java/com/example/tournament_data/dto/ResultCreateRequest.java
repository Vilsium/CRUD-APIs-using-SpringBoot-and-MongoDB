package com.example.tournament_data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request DTO for match result with names")
public class ResultCreateRequest {

    @NotBlank(message = "Winner team name is required")
    @Schema(description = "Name of the winning team", example = "Mumbai Indians", requiredMode = Schema.RequiredMode.REQUIRED)
    private String winner;

    @Schema(description = "Winning margin description", example = "13 runs")
    private String margin;

    @NotBlank(message = "Man of the match name is required")
    @Schema(description = "Name of the player of the match", example = "Rohit Sharma", requiredMode = Schema.RequiredMode.REQUIRED)
    private String manOfTheMatchName;
}
