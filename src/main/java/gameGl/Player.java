package gameGl;

import learnGL.tools.Shape;

public class Player {
    private Shape corps;
    private int vie;
    private int score;
    private int level;

    public Player(Shape corps, int vie, int score, int level) {
        this.corps = corps;
        this.vie = vie;
        this.score = score;
        this.level = level;
    }

    public Shape getCorps() {
        return corps;
    }

    public void setCorps(Shape corps) {
        this.corps = corps;
    }

    public int getVie() {
        return vie;
    }

    public void setVie(int vie) {
        this.vie = vie;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void reset() {
        this.vie = 5;
        this.score = 0;
        this.level = 0;
    }

    public void incrementScore(int points) {
        this.score += points;
    }

    public void decrementVie() {
        if (this.vie > 0) {
            this.vie--;
        }
    }

    public void incrementLevel() {
        this.level++;
    }

}
