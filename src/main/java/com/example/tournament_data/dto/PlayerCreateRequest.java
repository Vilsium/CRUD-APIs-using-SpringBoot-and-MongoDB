package com.example.tournament_data.dto;  
  
import com.example.tournament_data.model.Stats;

import io.swagger.v3.oas.annotations.media.Schema;  
import jakarta.validation.Valid;  
import jakarta.validation.constraints.NotBlank;  
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
@Schema(description = "Request DTO for creating a player with team name")  
public class PlayerCreateRequest {  
      
    @NotBlank(message = "Player Name is required")  
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")  
    @Schema(description = "Full name of the player", example = "Virat Kohli", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 100)  
    private String name;  
      
    @NotBlank(message = "Team name is required")  
    @Size(min = 2, max = 100, message = "Team name must be between 2 and 100 characters")  
    @Schema(description = "Name of the team the player belongs to (required)", example = "Royal Challengers Bangalore", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 100)  
    private String teamName;  
      
    @NotBlank(message = "Role is required")  
    @Pattern(regexp = "^(Batsman|Bowler|All-Rounder|Wicket-Keeper)$", message = "Role must be: Batsman, Bowler, All-Rounder, or Wicket-Keeper")  
    @Schema(description = "Playing role of the player in the team", example = "Batsman", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {  
            "Batsman", "Bowler", "All-Rounder", "Wicket-Keeper" })  
    private String role;  
      
    @NotBlank(message = "Batting style is required")  
    @Pattern(regexp = "^(Right-Handed|Left-Handed)$", message = "Batting style must be: Right-Handed or Left-Handed")  
    @Schema(description = "Batting hand preference of the player", example = "Right-Handed", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {  
            "Right-Handed", "Left-Handed" })  
    private String battingStyle;  
      
    @Pattern(regexp = "^(Right-Arm Fast|Left-Arm Fast|Right-Arm Medium|Left-Arm Medium|Right-Arm Spin|Left-Arm Spin|None)$", message = "Invalid bowling style")  
    @Schema(description = "Bowling style of the player (optional, use 'None' if player doesn't bowl)", example = "Right-Arm Fast", nullable = true, allowableValues = {  
            "Right-Arm Fast", "Left-Arm Fast", "Right-Arm Medium", "Left-Arm Medium", "Right-Arm Spin", "Left-Arm Spin",  
            "None" })  
    private String bowlingStyle;  
      
    @Valid  
    @Schema(description = "Player's initial career statistics (optional, defaults to zeros if not provided)")  
    private Stats stats;  
}  
