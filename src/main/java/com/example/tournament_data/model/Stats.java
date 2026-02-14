package com.example.tournament_data.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Player's career statistics")
public class Stats {

    @PositiveOrZero(message = "Matches cannot be negative")
    @Schema(description = "Total number of matches played", example = "150", minimum = "0")
    private Integer matchesPlayed;

    @PositiveOrZero(message = "Runs cannot be negative")
    @Schema(description = "Total runs scored in career", example = "12000", minimum = "0")
    private Integer runsScored;

    @PositiveOrZero(message = "Wickets cannot be negative")
    @Schema(description = "Total wickets taken in career", example = "45", minimum = "0")
    private Integer wicketsTaken;

    @PositiveOrZero(message = "Catches cannot be negative")
    @Schema(description = "Total catches taken in career", example = "80", minimum = "0")
    private Integer catchesTaken;

    public Stats() {
    }

    public Stats(Integer matchesPlayed, Integer runsScored, Integer wicketsTaken, Integer catchesTaken) {
        this.matchesPlayed = matchesPlayed;
        this.runsScored = runsScored;
        this.wicketsTaken = wicketsTaken;
        this.catchesTaken = catchesTaken;
    }

    public Integer getMatchesPlayed() {
        return matchesPlayed;
    }

    public void setMatchesPlayed(Integer matchesPlayed) {
        this.matchesPlayed = matchesPlayed;
    }

    public Integer getRunsScored() {
        return runsScored;
    }

    public void setRunsScored(Integer runsScored) {
        this.runsScored = runsScored;
    }

    public Integer getWicketsTaken() {
        return wicketsTaken;
    }

    public void setWicketsTaken(Integer wicketsTaken) {
        this.wicketsTaken = wicketsTaken;
    }

    public Integer getCatchesTaken() {
        return catchesTaken;
    }

    public void setCatchesTaken(Integer catchesTaken) {
        this.catchesTaken = catchesTaken;
    }

    @Override
    public String toString() {
        return "Stats [matchesPlayed=" + matchesPlayed + ", runsScored=" + runsScored + ", wicketsTaken=" + wicketsTaken
                + ", catchesTaken=" + catchesTaken + "]";
    }

}
