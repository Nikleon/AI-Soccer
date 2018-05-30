package org.tero.ai_soccer.sim.env;

import org.tero.ai_soccer.ai.NeuralNet;
import org.tero.ai_soccer.util.ScoreMap;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Transform;

public abstract class Environment {

	public abstract void update();

	public abstract void reset();
	
	public abstract double[] getScores();

	public abstract void draw(double w, double h, GraphicsContext g);

	protected abstract class Context {
		double[] data; // game state data

		Context(double... data) {
			this.data = data;
		}

		abstract double[] getRelativeData(Transform transform);
	}

	protected abstract class Action {
		double[] commands; // intended changes

		Action(double[] commands) {
			this.commands = commands;
		}

		abstract Action toGlobal(Transform transform);

		abstract void apply();
	}

	protected abstract class Entity {
		NeuralNet controller; // brain
		ScoreMap scoreMap; // fitness cache

		Entity(NeuralNet controller) {
			this.controller = controller;
			scoreMap = new ScoreMap();
		}

		abstract Action getNextAction();
		
		abstract double getScore();
	}

}
