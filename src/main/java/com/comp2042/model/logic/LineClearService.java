package com.comp2042.model.logic;

import com.comp2042.model.ClearRow;
import com.comp2042.model.MatrixOperations;

public class LineClearService {

    public ClearRow clearFullLines(int[][] matrix) {
        return MatrixOperations.checkRemoving(matrix);
    }
}
