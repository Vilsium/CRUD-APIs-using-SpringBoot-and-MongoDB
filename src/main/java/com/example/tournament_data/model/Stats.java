package com.example.tournament_data.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Player's career statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stats {

    @PositiveOrZero(message = "Matches cannot be negative")
    @Schema(description = "Total number of matches played", example = "150", minimum = "0")
    private Integer matchesPlayed;

    @PositiveOrZero(message = "Runs cannot be negative")
    @Schema(description = "Total runs scored in career", example = "12000", minimum = "0")
    private Integer runsScored;

    @PositiveOrZero(message = "Wickets cannot be negative")
    @Schema(description = "Total wickets taken in career", example = "45", minimum = "0")
    private Integer wicketsTaken;

    @PositiveOrZero(message = "Catches cannot be negative")
    @Schema(description = "Total catches taken in career", example = "80", minimum = "0")
    private Integer catchesTaken;
}
