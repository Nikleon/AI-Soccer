package org.tero.ai_soccer.ai;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

public class WeightMatrix extends SimpleMatrix {
    private static final long serialVersionUID = 1L;

    public WeightMatrix() {
	super();
    }

    public static WeightMatrix glorotInit(int preLayerWidth, int postLayerWidth, Random rand) {
	return (WeightMatrix) SimpleMatrix.random(postLayerWidth, preLayerWidth, -1.0 / Math.sqrt(preLayerWidth),
		1.0 / Math.sqrt(preLayerWidth), rand);
    }
}
