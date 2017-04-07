package acb.godetroit.notsafe.godetroit.Entities;

import acb.godetroit.notsafe.godetroit.Utils.ScoreUpTask;

/**
 * Created by benlalah on 10/01/17.
 */

public class Alert {

    private String content;
    private int score;
    private double distance;
    private double danger;
    private int id;

    public Alert(int id, String content, int score, double distance, double danger){
        this.score = score;
        this.content = content;
        this.distance = distance;
        this.danger = danger;
        this.id = id;
    }

    public String getDistance(){
        if(distance < 2)
            return "nearby";
        else
            return Math.round(distance)+" mi away";
    }

    public String getContent(){
        return content;
    }

    public int getScore() { return score; }

    public double getDanger(){ return danger; }

    public void addScore(){
        this.score = this.score + 1;
        new ScoreUpTask().execute(id);
    }

    public String getFormattedScore() {
        long count = (long) score;
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f%c",
                count / Math.pow(1000, exp),
                "kMGTPE".charAt(exp-1));
    }
}
