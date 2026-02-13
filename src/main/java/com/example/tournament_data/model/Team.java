package com.example.tournament_data.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "teams")
public class Team {
    @Id
    private String id;

    private String teamName;
    private String homeGround;

    //referencing to a player
    private String captainId;

    private String coach;

    private List<String> playerIds = new ArrayList<>();

    public Team() {
    }

    public Team(String teamName, String homeGround, String captainId, String coach, List<String> playerIds) {
        this.teamName = teamName;
        this.homeGround = homeGround;
        this.coach = coach;
        this.captainId = captainId;
        this.playerIds = playerIds;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCaptainId() {
        return captainId;
    }

    public void setCaptainId(String captainId) {
        this.captainId = captainId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getHomeGround() {
        return homeGround;
    }

    public void setHomeGround(String homeGround) {
        this.homeGround = homeGround;
    }

    public String getCoach() {
        return coach;
    }

    public void setCoach(String coach) {
        this.coach = coach;
    }

    public List<String> getPlayerIds() {
        return playerIds;
    }

    public void setPlayerIds(List<String> playerIds) {
        this.playerIds = playerIds;
    }

    @Override
    public String toString() {
        return "Team [id=" + id + ", teamName=" + teamName + ", homeGround=" + homeGround + ", captainId=" + captainId
                + ", coach=" + coach + ", playerIds=" + playerIds + "]";
    }
    
}
