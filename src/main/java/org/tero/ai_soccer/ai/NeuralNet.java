package org.tero.ai_soccer.ai;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

public class NeuralNet {
    protected final Random RNG;

    protected int depth;
    protected WeightMatrix[] weights; // dims: (post X pre)

    protected ActivationFunctions[] activationFunctions; // by layer, aFuncs[0] -> layer 1 aFunc
    protected SimpleMatrix[] activationCache; // cache[0] -> layer 1 activations
    protected boolean activationCacheValid = false;

    public NeuralNet(int[] shape) {
	depth = shape.length - 1;
	RNG = new Random();

	activationFunctions = new ActivationFunctions[depth];

	weights = new WeightMatrix[depth];
	for (int preLayer = 0; preLayer < depth; preLayer++)
	    weights[preLayer] = WeightMatrix.glorotInit(shape[preLayer + 1], shape[preLayer], RNG);

	activationCache = new SimpleMatrix[depth];
    }

    public NeuralNet(int[] shape, ActivationFunctions[] aFuncs) {
	this(shape);
	activationFunctions = aFuncs.clone();
    }

    public double[] predict(double[] input) {
	SimpleMatrix inputColumn = new SimpleMatrix(input.length, 1, false, input);
	return forwardPropagate(inputColumn).getMatrix().getData().clone();
    }

    // returns output layer activations
    protected SimpleMatrix forwardPropagate(SimpleMatrix inputColumn) {
	for (int layer = 0; layer < depth; layer++) {
	    SimpleMatrix prevLayer = (layer == 0) ? inputColumn : activationCache[layer - 1];
	    activationCache[layer] = activationFunctions[layer].apply(weights[layer].mult(prevLayer) /* TODO +bias */);
	}
	activationCacheValid = true;
	return activationCache[depth - 1];
    }

    protected SimpleMatrix getActivationCache(int layer) {
	if (!activationCacheValid)
	    return null;
	return activationCache[layer - 1];
    }

    // call when properties of network are changed which affect future activations
    protected void invalidateCache() {
	this.activationCacheValid = false;
    }

}
