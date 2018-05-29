package org.tero.ai_soccer.ai;

public class GeneticNeuralNet extends NeuralNet {

    public GeneticNeuralNet(int[] shape) {
	super(shape);
    }

    public GeneticNeuralNet(int[] shape, ActivationFunctions[] aFuncs) {
	super(shape, aFuncs);
    }

    public GeneticNeuralNet(int[] shape, WeightMatrix[] weights) {
	super(shape, weights);
    }

    public void mutate(double mu) {
	for (int layer = 0; layer < depth; layer++)
	    for (int i = 0; i < weights[layer].getNumElements(); i++)
		if (RNG.nextDouble() < mu) {
		    double dw = RNG.nextGaussian();
		    weights[layer].set(i, weights[layer].get(i) + dw);
		}
    }

    @Override
    public GeneticNeuralNet clone() {
	return (GeneticNeuralNet) super.clone();
    }
}
