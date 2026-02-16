package com.example.tournament_data.dto;

import com.example.tournament_data.model.Stats;

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
@Schema(description = "Request DTO for partially updating a player")
public class PlayerPatchRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(description = "Full name of the player", example = "Virat Kohli", nullable = true)
    private String name;

    @Size(min = 2, max = 100, message = "Team name must be between 2 and 100 characters")
    @Schema(description = "Name of the team to transfer player to", example = "Mumbai Indians", nullable = true)
    private String teamName;

    @Pattern(regexp = "^(Batsman|Bowler|All-Rounder|Wicket-Keeper)$", message = "Role must be: Batsman, Bowler, All-Rounder, or Wicket-Keeper")
    @Schema(description = "Playing role of the player", example = "Batsman", nullable = true)
    private String role;

    @Pattern(regexp = "^(Right-Handed|Left-Handed)$", message = "Batting style must be: Right-Handed or Left-Handed")
    @Schema(description = "Batting hand preference", example = "Right-Handed", nullable = true)
    private String battingStyle;

    @Pattern(regexp = "^(Right-Arm Fast|Left-Arm Fast|Right-Arm Medium|Left-Arm Medium|Right-Arm Spin|Left-Arm Spin|None)$", message = "Invalid bowling style")
    @Schema(description = "Bowling style of the player", example = "Right-Arm Fast", nullable = true)
    private String bowlingStyle;

    @Valid
    @Schema(description = "Player's career statistics (partial update supported)", nullable = true)
    private Stats stats;
}
