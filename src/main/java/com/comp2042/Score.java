package com.comp2042;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public final class Score {

    private final IntegerProperty score = new SimpleIntegerProperty(0);
    private int currentLevel = 1;

    public IntegerProperty scoreProperty() {
        return score;
    }

    public void add(int points){
        score.setValue(score.getValue() + points);
    }

    public void addWithLevelMultiplier(int basePoints) {
        int multipliedPoints = basePoints * currentLevel;
        score.setValue(score.getValue() + multipliedPoints);
    }

    public void setLevel(int level) {
        this.currentLevel = level;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void reset() {
        score.setValue(0);
        currentLevel = 1;
    }

    public int getScore() {
        return score.getValue();
    }
}
