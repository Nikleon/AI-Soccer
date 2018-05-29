package org.tero.ai_soccer.ai;

import java.util.Random;

import org.ejml.alg.generic.GenericMatrixOps;
import org.ejml.ops.RandomMatrices;
import org.ejml.simple.SimpleMatrix;

public class WeightMatrix extends SimpleMatrix {
    private static final long serialVersionUID = 1L;

    public WeightMatrix(int numRows, int numCols) {
	super(numRows, numCols);
    }

    public static WeightMatrix glorotInit(int preLayerWidth, int postLayerWidth, Random rand) {
	WeightMatrix ret = new WeightMatrix(postLayerWidth, preLayerWidth);
	RandomMatrices.setRandom(ret.mat, -1.0 / Math.sqrt(preLayerWidth), 1.0 / Math.sqrt(preLayerWidth), rand);
	return ret;
    }

    @Override
    public String toString() {
	StringBuilder saveStr = new StringBuilder();

	for (int r = 0; r < this.numRows(); r++) {
	    for (int c = 0; c < this.numCols(); c++) {
		saveStr.append(String.format("%.3f", this.getMatrix().get(r, c))).append('\t');
	    }
	    saveStr.append('\n');
	}

	return saveStr.toString();
    }

    @Override
    public WeightMatrix copy() {
	WeightMatrix copy = new WeightMatrix(this.numRows(), this.numCols());
	GenericMatrixOps.copy(this.getMatrix(), copy.getMatrix());
	return copy;
    }
}
