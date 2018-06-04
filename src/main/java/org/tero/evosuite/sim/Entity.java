package org.tero.evosuite.sim;

import java.util.HashMap;
import java.util.Map;

public abstract class Entity {

	private final Map<String, Double> scoreMap = new HashMap<String, Double>();

	protected final int inputSize;
	protected final int outputSize;

	public Entity(int inputSize, int outputSize) {
		this.inputSize = inputSize;
		this.outputSize = outputSize;
	}

	public double getScore(String skillTag) {
		return scoreMap.get(skillTag);
	}

	public void setScore(String skillTag, double value) {
		scoreMap.put(skillTag, value);
	}

	public void modifyScore(String skillTag, double changeInValue) {
		scoreMap.merge(skillTag, changeInValue, Double::sum);
	}

	public abstract void takeAction(double[] inputs);

	public abstract double getScore();

}
