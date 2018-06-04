package org.tero.ai_soccer.sim;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Ball {
	public static final int radius = 15;
	public double x, y;
	public double vx, vy;

	public Ball() {
		reset();
	}

	public void reset() {
		 x = Math.random() * 10 - 5;
		 y = Math.random() * 10 - 5;
		// x = 0;
		// y = 0;
		vx = 0;
		vy = 0;
	}

	public void draw(GraphicsContext g) {
		g.setFill(Color.WHITE);
		g.fillOval((int) (x - radius), (int) (y - radius), radius * 2, radius * 2);
		g.setStroke(Color.BLACK);
		g.strokeOval((int) (x - radius), (int) (y - radius), radius * 2, radius * 2);
	}
}
