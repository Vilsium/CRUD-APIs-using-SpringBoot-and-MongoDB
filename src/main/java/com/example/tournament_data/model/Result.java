package com.example.tournament_data.model;

public class Result {
    private String winner;
    private String margin;
    private String maOfTheMatchId;

    public Result() {
    }

    public Result(String winner, String margin, String maOfTheMatchId) {
        this.winner = winner;
        this.margin = margin;
        this.maOfTheMatchId = maOfTheMatchId;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getMargin() {
        return margin;
    }

    public void setMargin(String margin) {
        this.margin = margin;
    }

    public String getmaOfTheMatchId() {
        return maOfTheMatchId;
    }

    public void setmaOfTheMatchId(String maOfTheMatchId) {
        this.maOfTheMatchId = maOfTheMatchId;
    }

    @Override
    public String toString() {
        return "Result [winner=" + winner + ", margin=" + margin + ", maOfTheMatchId=" + maOfTheMatchId + "]";
    }

}
