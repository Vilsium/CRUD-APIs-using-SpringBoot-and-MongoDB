package com.example.tournament_data.model;

public class Stats {
    private Integer matchesPlayed;
    private Integer runsScored;
    private Integer wicketsTaken;
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
