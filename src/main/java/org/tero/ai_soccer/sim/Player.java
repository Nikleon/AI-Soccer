package org.tero.ai_soccer.sim;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import not.my.code.Action;
import not.my.code.AndrewMethod;

public class Player {
    public final static int radius = 20;
    public final static int shockwaveRadius = 20;
    public final static double hitSlowRatio = 0.95;

    public double x, y;
    public double xv, yv;
    public boolean t;
    public int shockwaveFrame;
    public double[] weights;

    public Action takeAction(double ballx, double bally, double ballvx, double ballvy, double a1x, double a1y,
	    double a2x, double a2y, double e1x, double e1y, double e2x, double e2y, double e3x, double e3y) {
	if (t) {
	    Action action = AndrewMethod.takeAction(-x, y, -ballx, bally, -ballvx, ballvy, -a1x, a1y, -a2x, a2y, -e1x,
		    e1y, -e2x, e2y, -e3x, e3y);
	    return new Action(-action.fx, action.fy, action.hit);
	} else {
	    return AndrewMethod.takeAction(x, y, ballx, bally, ballvx, ballvy, a1x, a1y, a2x, a2y, e1x, e1y, e2x, e2y,
		    e3x, e3y);
	}
    }

    public Player(int x, int y, boolean team) {
	this.xv = 0;
	this.yv = 0;
	this.x = x;
	this.y = y;
	t = team;
	shockwaveFrame = 0;
    }

    public void update(Action a) {
	double speed = dist(0, 0, xv, yv);
	if (speed > 40) {
	    xv *= 40 / speed;
	    yv *= 40 / speed;
	}
	double vx = a.fx + xv;
	double vy = a.fy + yv;

	double tempx = vx;
	double tempy = vy;
	boolean clear = false;
	while (!clear) {
	    clear = true;
	    // boolean hitY = false;
	    if (Math.abs(y + tempy) + radius > GameState.HEIGHT) {
		clear = false;
		vy *= -.8;
		if (y > 0) {
		    tempy = -.8 * (y + tempy + radius - GameState.HEIGHT);
		    y = GameState.HEIGHT - radius;
		} else {
		    tempy = -.8 * (y + tempy - radius + GameState.HEIGHT);
		    y = -GameState.HEIGHT + radius;
		}
		// hitY = true;
	    }
	    if (Math.abs(x + tempx) + radius > GameState.LENGTH) {
		clear = false;
		vx *= -.8;
		if (x > 0) {
		    tempx = -.8 * (x + tempx + radius - GameState.LENGTH);
		    x = GameState.LENGTH - radius;
		} else {
		    tempx = -.8 * (x + tempx - radius + GameState.LENGTH);
		    x = -GameState.LENGTH + radius;
		}
	    }
	}
	x += tempx;
	y += tempy;
	xv *= hitSlowRatio;
	yv *= hitSlowRatio;
	if (Math.abs(xv) < .5)
	    xv = 0;
	if (Math.abs(yv) < .5)
	    xv = 0;
	if (a.hit && shockwaveFrame == 0)
	    shockwaveFrame++;
	if (shockwaveFrame != 0)
	    shockwaveFrame++;
	if (shockwaveFrame == 6)
	    shockwaveFrame = -15;
    }

    public void drawPlayer(GraphicsContext g) {
	if (t)
	    g.setFill(Color.BLUE);
	else
	    g.setFill(Color.ORANGE);

	g.fillOval((int) (x - radius), (int) (y - radius), (int) (2 * radius), (int) (2 * radius));
	g.setStroke(Color.BLACK);
	g.strokeOval((int) (x - radius), (int) (y - radius), (int) (2 * radius), (int) (2 * radius));
    }

    public void drawShockwave(GraphicsContext g) {
	if (shockwaveFrame > 0) {
	    g.setFill(Color.WHITE);
	    g.fillOval((int) (x - radius - shockwaveFrame * (shockwaveRadius) / 5),
		    (int) (y - radius - shockwaveFrame * (shockwaveRadius) / 5),
		    (int) (2 * radius + shockwaveFrame * (shockwaveRadius) / 5 * 2),
		    (int) (2 * radius + shockwaveFrame * (shockwaveRadius) / 5 * 2));
	}
    }

    private double dist(double x1, double y1, double x2, double y2) {
	return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

}
