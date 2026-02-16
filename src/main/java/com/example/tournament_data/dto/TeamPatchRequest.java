package com.example.tournament_data.dto;  
  
import java.util.List;  
  
import io.swagger.v3.oas.annotations.media.Schema;  
import jakarta.validation.constraints.Size;  
import lombok.AllArgsConstructor;  
import lombok.Builder;  
import lombok.Data;  
import lombok.NoArgsConstructor;  
  
@Data  
@NoArgsConstructor  
@AllArgsConstructor  
@Builder  
@Schema(description = "Request DTO for partially updating a team")  
public class TeamPatchRequest {  
  
    @Size(min = 2, max = 100, message = "Team name must be between 2 and 100 characters")  
    @Schema(description = "Official name of the team", example = "Mumbai Indians", nullable = true)  
    private String teamName;  
  
    @Size(min = 2, max = 100, message = "Ground name must be between 2 and 100 characters")  
    @Schema(description = "Home stadium/ground", example = "Wankhede Stadium, Mumbai", nullable = true)  
    private String homeGround;  
  
    @Size(min = 2, max = 100, message = "Coach name must be between 2 and 100 characters")  
    @Schema(description = "Name of the team's head coach", example = "Mahela Jayawardene", nullable = true)  
    private String coach;  
  
    @Schema(description = "Name of the team captain (must be a player in the team)", example = "Rohit Sharma", nullable = true)  
    private String captainName;  
  
    @Size(max = 25, message = "Team cannot have more than 25 players")  
    @Schema(description = "List of player names to set for the team", nullable = true)  
    private List<String> playerNames;  
}  
