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
@Schema(description = "Detailed team response with player information from aggregation")
public class TeamDetailsResponse {

    @Schema(description = "Unique identifier of the team", example = "1")
    private Integer id;

    @Schema(description = "Official name of the team", example = "Mumbai Indians")
    private String teamName;

    @Schema(description = "Home stadium/ground", example = "Wankhede Stadium, Mumbai")
    private String homeGround;

    @Schema(description = "Name of the team's head coach", example = "Mahela Jayawardene")
    private String coach;

    @Schema(description = "Player ID of the team captain", example = "1")
    private Integer captainId;

    @Schema(description = "List of players with their details")
    private List<PlayerSummary> squad;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Player summary information")
    public static class PlayerSummary {

        @Schema(description = "Player ID", example = "1")
        private Integer id;

        @Schema(description = "Player name", example = "Rohit Sharma")
        private String name;

        @Schema(description = "Player role", example = "Batsman")
        private String role;
    }
}
