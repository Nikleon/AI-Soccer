package org.tero.evosuite.sim;

import org.tero.evosuite.Drawable;

public abstract class Player extends Entity implements Drawable {

	public Player(int inputSize, int outputSize) {
		super(inputSize, outputSize);
	}

}
