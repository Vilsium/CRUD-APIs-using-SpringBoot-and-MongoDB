package com.example.tournament_data.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "players")
public class Player {
    @Id
    private String id; //automatically created by MongoDB

    //all other fields
    private String teamId; //will refer to the team Object ID, can also be null if player is not in any team
    
    private String name;
    private String role;
    private String battingStyle;
    private String bowlingStyle;

    //storing stats as embedded document, hence made another model for it
    private Stats stats;

    //default constructor to create empty Player object for JSON/MongoDB mapping
    public Player() {
    }

    public Player(String name, String role, String battingStyle, String bowlingStyle) {
        this.name = name;
        this.role = role;
        this.battingStyle = battingStyle;
        this.bowlingStyle = bowlingStyle;
    }

    //setters and getters
    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getBattingStyle() {
        return battingStyle;
    }

    public void setBattingStyle(String battingStyle) {
        this.battingStyle = battingStyle;
    }

    public String getBowlingStyle() {
        return bowlingStyle;
    }

    public void setBowlingStyle(String bowlingStyle) {
        this.bowlingStyle = bowlingStyle;
    }

    @Override
    public String toString() {
        return "Player [id=" + id + ", teamId=" + teamId + ", name=" + name + ", role=" + role + ", battingStyle="
                + battingStyle + ", bowlingStyle=" + bowlingStyle + ", stats=" + stats + "]";
    }
    
}
