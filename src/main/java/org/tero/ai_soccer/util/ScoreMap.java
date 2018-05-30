package org.tero.ai_soccer.util;

import java.util.HashMap;

public class ScoreMap {

	private HashMap<String, Double> scores;

	public ScoreMap() {
		scores = new HashMap<String, Double>();
	}

	public void set(String skillTag, double value) {
		scores.put(skillTag, value);
	}

	public double get(String skillTag) {
		return scores.get(skillTag);
	}

	public void modify(String skillTag, double changeInValue) {
		System.out.println(scores.merge(skillTag, changeInValue, Double::sum));
	}

}
