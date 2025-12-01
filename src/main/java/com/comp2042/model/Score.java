package com.comp2042.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public final class Score {

    private final IntegerProperty score = new SimpleIntegerProperty(0);
    private final IntegerProperty level = new SimpleIntegerProperty(1);
    private final IntegerProperty lines = new SimpleIntegerProperty(0);

    public IntegerProperty scoreProperty() {
        return score;
    }

    public IntegerProperty levelProperty() {
        return level;
    }

    public IntegerProperty linesProperty() {
        return lines;
    }

    public void add(int i){
        score.setValue(score.getValue() + i);
    }

    public void addLines(int count) {
        if (count > 0) {
            lines.setValue(lines.getValue() + count);
            // Calculate new level: level = (total_lines / 5) + 1
            int totalLines = lines.getValue();
            int calculatedLevel = (totalLines / 5) + 1;
            // Update level if calculated level is greater than current level
            if (calculatedLevel > level.getValue()) {
                level.setValue(calculatedLevel);
            }
        }
    }

    public void reset() {
        score.setValue(0);
        lines.setValue(0);
        level.setValue(1);
    }
}
