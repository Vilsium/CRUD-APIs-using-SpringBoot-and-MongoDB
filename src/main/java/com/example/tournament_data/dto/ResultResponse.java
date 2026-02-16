package com.example.tournament_data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Match result response with names")
public class ResultResponse {

    @Schema(description = "Name of the winning team", example = "Mumbai Indians")
    private String winner;

    @Schema(description = "Winning margin description", example = "5 wickets")
    private String margin;

    @Schema(description = "Name of the man of the match", example = "Rohit Sharma")
    private String manOfTheMatch;
}
