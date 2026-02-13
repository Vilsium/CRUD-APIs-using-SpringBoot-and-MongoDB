package com.example.tournament_data.dto;

import java.util.List;

public class TeamDetailsResponse {
    private String id;
    private String teamName;
    private String homeGround;
    private String captainId;
    private String coach;

    // for storing the squad
    private List<PlayerInfo> squad;

    public TeamDetailsResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getCaptainId() {
        return captainId;
    }

    public void setCaptainId(String captainId) {
        this.captainId = captainId;
    }

    public String getCoach() {
        return coach;
    }

    public void setCoach(String coach) {
        this.coach = coach;
    }

    public List<PlayerInfo> getSquad() {
        return squad;
    }

    public void setSquad(List<PlayerInfo> squad) {
        this.squad = squad;
    }

    public static class PlayerInfo {
        private String id;
        private String name;
        private String role;

        public PlayerInfo() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
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

    }
}
