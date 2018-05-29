package org.tero.ai_soccer.ai;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

public class NeuralNet implements Comparable<NeuralNet>, Cloneable {
    public static final int[] DEFAULT_SHAPE = { 16, 16, 16, 3 };
    public static final int NUM_WEIGHTS;
    static {
	int num = 0;
	for (int i = 0; i < DEFAULT_SHAPE.length - 1; i++)
	    num += DEFAULT_SHAPE[i] * DEFAULT_SHAPE[i + 1];
	NUM_WEIGHTS = num;
    }

    protected final Random RNG;

    protected final int depth;

    protected WeightMatrix[] weights; // dims: (post X pre)
    // protected SimpleMatrix[] biases;

    protected ActivationFunctions[] activationFunctions; // by layer, aFuncs[0] -> layer 1 aFunc
    protected SimpleMatrix[] activationCache; // cache[0] -> layer 1 activations
    protected boolean activationCacheValid = false;

    private double scoreCache = -1;

    private NeuralNet(int depth) {
	RNG = new Random();

	this.depth = depth;

	weights = new WeightMatrix[depth];
	// biases = new SimpleMatrix[depth];
	activationFunctions = new ActivationFunctions[depth];
	activationCache = new SimpleMatrix[depth];
    }

    public NeuralNet(int[] shape) {
	this(shape.length - 1);

	for (int preLayer = 0; preLayer < depth; preLayer++)
	    weights[preLayer] = WeightMatrix.glorotInit(shape[preLayer], shape[preLayer + 1], RNG);
    }

    public NeuralNet(int[] shape, ActivationFunctions[] aFuncs) {
	this(shape);

	activationFunctions = aFuncs.clone();
    }

    public NeuralNet(int[] shape, WeightMatrix[] weights) {
	this(shape.length - 1);

	for (int i = 0; i < weights.length; i++) {
	    this.weights[i] = new WeightMatrix(weights[i].numRows(), weights[i].numCols());
	    this.weights[i].getMatrix().setData(weights[i].copy().getMatrix().data);
	}
    }

    public double[] predict(double[] input) {
	SimpleMatrix inputColumn = new SimpleMatrix(input.length, 1, false, input);
	return forwardPropagate(inputColumn).getMatrix().getData().clone();
    }

    // returns output layer activations
    protected SimpleMatrix forwardPropagate(SimpleMatrix inputColumn) {
	for (int layer = 0; layer < depth; layer++) {
	    SimpleMatrix prevLayer = (layer == 0) ? inputColumn : activationCache[layer - 1];
	    activationCache[layer] = (activationFunctions[layer] == null ? ActivationFunctions.DEFAULT
		    : activationFunctions[layer]).apply(weights[layer].mult(prevLayer) /* TODO +bias */);
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

    public WeightMatrix[] getWeights() {
	return weights;
    }

    public int getDepth() {
	return depth;
    }

    public int[] getShape() {
	int[] shape = new int[depth + 1];
	shape[0] = weights[0].numCols();
	for (int layer = 0; layer < depth; layer++) {
	    shape[layer + 1] = weights[layer].numRows();
	}
	return shape;
    }

    public void cacheScore(double score) {
	scoreCache = score;
    }

    public double getScoreCache() {
	return scoreCache;
    }

    @Override
    public int compareTo(NeuralNet that) {
	return -Double.compare(this.getScoreCache(), that.getScoreCache());
    }

    @Override
    public NeuralNet clone() {
	try {
	    NeuralNet clone = (NeuralNet) super.clone();

	    WeightMatrix[] weightsCopy = new WeightMatrix[depth];
	    for (int i = 0; i < depth; i++)
		weightsCopy[i] = weights[i].copy();
	    clone.weights = weightsCopy;

	    return clone;
	} catch (CloneNotSupportedException e) {
	    e.printStackTrace();
	    return null;
	}
    }

}
