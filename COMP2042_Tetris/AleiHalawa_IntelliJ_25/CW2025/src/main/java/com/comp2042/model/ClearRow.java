package com.comp2042.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable result of a line clear operation:
 * indices of lines removed, new board matrix, and score bonus.
 */
public final class ClearRow {

    private final List<Integer> linesRemoved;
    private final int[][] newMatrix;
    private final int scoreBonus;

    public ClearRow(List<Integer> linesRemoved, int[][] newMatrix, int scoreBonus) {
        this.linesRemoved = new ArrayList<>(linesRemoved);
        this.newMatrix = newMatrix;
        this.scoreBonus = scoreBonus;
    }

    public List<Integer> getLinesRemoved() {
        return new ArrayList<>(linesRemoved);
    }

    public int getLinesRemovedCount() {
        return linesRemoved.size();
    }

    public int[][] getNewMatrix() {
        return MatrixOperations.copy(newMatrix);
    }

    public int getScoreBonus() {
        return scoreBonus;
    }
}
