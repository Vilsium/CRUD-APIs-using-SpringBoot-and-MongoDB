package com.example.tournament_data.dto;  
  
import java.util.List;  
  
import io.swagger.v3.oas.annotations.media.Schema;  
import lombok.AllArgsConstructor;  
import lombok.Builder;  
import lombok.Data;  
import lombok.NoArgsConstructor;  
  
@Data  
@NoArgsConstructor  
@AllArgsConstructor  
@Builder  
@Schema(description = "Response DTO for team with names instead of IDs")  
public class TeamResponse {  
  
    @Schema(description = "Unique identifier of the team", example = "1")  
    private Integer id;  
  
    @Schema(description = "Official name of the team", example = "Mumbai Indians")  
    private String teamName;  
  
    @Schema(description = "Home stadium/ground", example = "Wankhede Stadium, Mumbai")  
    private String homeGround;  
  
    @Schema(description = "Name of the team's head coach", example = "Mahela Jayawardene")  
    private String coach;  
  
    @Schema(description = "Name of the team captain", example = "Rohit Sharma")  
    private String captainName;  
  
    @Schema(description = "List of player names in the team")  
    private List<String> playerNames;  
}  
