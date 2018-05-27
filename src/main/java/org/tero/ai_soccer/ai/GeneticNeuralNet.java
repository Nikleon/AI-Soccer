package org.tero.ai_soccer.ai;

public class GeneticNeuralNet extends NeuralNet {

    public GeneticNeuralNet(int[] shape) {
	super(shape);
    }

    public void mutate(double mu) {
	for (int layer = 0; layer < depth; layer++)
	    for (int i = 0; i < weights[layer].getNumElements(); i++)
		if (RNG.nextDouble() < mu) {
		    double dw = RNG.nextGaussian() / weights[layer].numCols();
		    weights[layer].set(i, weights[layer].get(i) + dw);
		}
    }
}
