package org.tero.evosuite.sim;

import org.tero.ai_soccer.ai.NeuralNet;

public abstract class DuelEnv extends Environment {

	protected final NeuralNet leftAi;
	protected final NeuralNet rightAi;

	public DuelEnv(NeuralNet leftAi, NeuralNet rightAi) {
		this.leftAi = leftAi;
		this.rightAi = rightAi;
	}

}
