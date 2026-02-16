package com.example.tournament_data.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating a team with player names")
public class TeamCreateRequest {

    @NotBlank(message = "Team Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(description = "Official name of the team", example = "Mumbai Indians", requiredMode = Schema.RequiredMode.REQUIRED)
    private String teamName;

    @NotBlank(message = "Home Ground is required")
    @Size(min = 2, max = 100, message = "Ground name must be between 2 and 100 characters")
    @Schema(description = "Home stadium/ground", example = "Wankhede Stadium, Mumbai", requiredMode = Schema.RequiredMode.REQUIRED)
    private String homeGround;

    @Schema(description = "Name of the team captain (must be a player in the team)", example = "Rohit Sharma", nullable = true)
    private String captainName;

    @NotBlank(message = "Coach Name is required")
    @Size(min = 2, max = 100, message = "Coach name must be between 2 and 100 characters")
    @Schema(description = "Full name of the team's head coach", example = "Mahela Jayawardene", requiredMode = Schema.RequiredMode.REQUIRED)
    private String coach;

    @Size(max = 25, message = "Team cannot have more than 25 players")
    @Schema(description = "List of player names belonging to this team", example = "[\"Rohit Sharma\", \"Jasprit Bumrah\"]")
    private List<String> playerNames = new ArrayList<>();
}
