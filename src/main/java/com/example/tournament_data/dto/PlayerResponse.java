package com.example.tournament_data.dto;  
  
import com.example.tournament_data.model.Stats;  
  
import io.swagger.v3.oas.annotations.media.Schema;  
import lombok.AllArgsConstructor;  
import lombok.Builder;  
import lombok.Data;  
import lombok.NoArgsConstructor;  
  
@Data  
@NoArgsConstructor  
@AllArgsConstructor  
@Builder  
@Schema(description = "Response DTO for player with team name")  
public class PlayerResponse {  
      
    @Schema(description = "Unique identifier of the player", example = "2")  
    private Integer id;  
      
    @Schema(description = "Full name of the player", example = "Virat Kohli")  
    private String name;  
      
    @Schema(description = "Name of the team the player belongs to", example = "Royal Challengers Bangalore")  
    private String teamName;  
      
    @Schema(description = "Playing role of the player", example = "Batsman")  
    private String role;  
      
    @Schema(description = "Batting hand preference", example = "Right-Handed")  
    private String battingStyle;  
      
    @Schema(description = "Bowling style of the player", example = "Right-Arm Medium")  
    private String bowlingStyle;  
      
    @Schema(description = "Player's career statistics")  
    private Stats stats;  
}  
