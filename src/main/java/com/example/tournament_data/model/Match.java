package com.example.tournament_data.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "matches")
public class Match {
    @Id
    private String id;

    private String venue;
    private LocalDateTime date;

    private String firstTeam; // id of team
    private String secondTeam; // id of team

    private String status; // COMPLETE or SCHEDULED

    private Result result;

    public Match() {
    }

    public Match(String venue, LocalDateTime date, String firstTeam, String secondTeam, String status, Result result) {
        this.venue = venue;
        this.date = date;
        this.firstTeam = firstTeam;
        this.secondTeam = secondTeam;
        this.status = status;
        this.result = result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getFirstTeam() {
        return firstTeam;
    }

    public void setFirstTeam(String firstTeam) {
        this.firstTeam = firstTeam;
    }

    public String getSecondTeam() {
        return secondTeam;
    }

    public void setSecondTeam(String secondTeam) {
        this.secondTeam = secondTeam;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Match [id=" + id + ", venue=" + venue + ", date=" + date + ", firstTeam=" + firstTeam + ", secondTeam="
                + secondTeam + ", status=" + status + ", result=" + result + "]";
    }

}
