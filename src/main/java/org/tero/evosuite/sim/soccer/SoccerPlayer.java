package org.tero.evosuite.sim.soccer;

import org.tero.ai_soccer.ai.NeuralNet;
import org.tero.evosuite.sim.Player;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class SoccerPlayer extends Player {
	public static final Paint LEFT_FILL = Color.ORANGE;
	public static final Paint RIGHT_FILL = Color.BLUE;
	public static final Paint OUTLINE = Color.BLACK;

	public static final double RADIUS = 20.0;

	public boolean leftTeam;
	public Point2D pos;

	private final NeuralNet brainz;

	public SoccerPlayer(NeuralNet ai, boolean left) {
		super(16, 3);
		brainz = ai;

		leftTeam = left;
		pos = Point2D.ZERO;
	}

	@Override
	public double getScore() {
		return getScore("goals");
	}

	@Override
	public void draw(GraphicsContext g) {
		// TODO: draw shockwave

		g.setFill((leftTeam) ? LEFT_FILL : RIGHT_FILL);
		g.fillOval(pos.getX() - RADIUS, pos.getY() - RADIUS, 2 * RADIUS, 2 * RADIUS);

		g.setStroke(OUTLINE);
		g.strokeOval(pos.getX() - RADIUS, pos.getY() - RADIUS, 2 * RADIUS, 2 * RADIUS);
	}

	@Override
	public void takeAction(double[] inputs) {
		double[] outputs = brainz.predict(inputs /* TODO: relative inputs */);
		// TODO Auto-generated method stub
	}

}
