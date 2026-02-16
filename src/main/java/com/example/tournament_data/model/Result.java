package com.example.tournament_data.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Match result details containing winner, margin, and man of the match")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result {
    @NotBlank(message = "Winner team ID is required")
    @Pattern(regexp = "^[a-fA-F0-9]{24}$", message = "Invalid winner team ID format")
    @Schema(description = "Team ID of the winning team", example = "64a1b2c3d4e5f6g7h8i9j0k2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer winner;

    @NotBlank(message = "Winning margin is required")
    @Schema(description = "Victory margin (e.g., '50 runs', '5 wickets', 'Super Over')", example = "45 runs", requiredMode = Schema.RequiredMode.REQUIRED)
    private String margin;

    @NotBlank(message = "Man of the match is required")
    @Schema(description = "Player ID of the Man of the Match", example = "64a1b2c3d4e5f6g7h8i9j0k4", nullable = true)
    private Integer manOfTheMatchId;
}
