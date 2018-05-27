package org.tero.ai_soccer.sim;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Affine;
import not.my.code.Action;

public class GameState {
    public static double dist(double x, double y, double x1, double y1) {
	return (Math.sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1)));
    }

    public static final int LENGTH = 500;
    public static final int HEIGHT = 300;
    public static final int goalHeight = 100;
    public static final double ballSlowRatio = 0.99;
    public static final double ballHitSlowRatio = 0.8;
    private int pause; // -1 is permanent, 0 is not paused, >0 is a count-down.
    private Player[] lTeam;
    private Player[] rTeam;
    private Ball ball;
    public int lScore;
    public int rScore;
    private int time;
    int replayTime;
    double ballStartx;
    double ballStarty;
    boolean replay;

    public GameState() {
	replay = false;
	replayTime = 0;
	time = 0;
	lScore = 0;
	rScore = 0;
	pause = 90;
	lTeam = new Player[] { new Player(-100, 100, false), new Player(-200, -200, false),
		new Player(-400, 0, false) };
	rTeam = new Player[] { new Player(100, -100, true), new Player(200, 200, true), new Player(400, 0, true) };
	ball = new Ball();
	ballStartx = ball.x;
	ballStarty = ball.y;
    }

    public void update() {
	if (pause > 0) {
	    pause--;
	    return;
	}
	if (pause == -1) {
	    return;
	}
	if (replay) {
	    replayTime--;
	} else {
	    time++;
	    replayTime++;
	}
	double[] prevX = new double[6];
	double[] prevY = new double[6];
	prevX[0] = lTeam[0].x;
	prevX[1] = lTeam[1].x;
	prevX[2] = lTeam[2].x;
	prevX[3] = rTeam[0].x;
	prevX[4] = rTeam[1].x;
	prevX[5] = rTeam[2].x;
	prevY[0] = lTeam[0].y;
	prevY[1] = lTeam[1].y;
	prevY[2] = lTeam[2].y;
	prevY[3] = rTeam[0].y;
	prevY[4] = rTeam[1].y;
	prevY[5] = rTeam[2].y;
	Action[] actions = new Action[lTeam.length + rTeam.length];
	actions[0] = lTeam[0].takeAction(ball.x - 0.0001, ball.y - 0.0001, ball.vx, ball.vy, lTeam[1].x, lTeam[1].y,
		lTeam[2].x, lTeam[2].y, rTeam[0].x, rTeam[0].y, rTeam[1].x, rTeam[1].y, rTeam[2].x, rTeam[2].y);
	actions[1] = lTeam[1].takeAction(ball.x - 0.0001, ball.y - 0.0001, ball.vx, ball.vy, lTeam[0].x, lTeam[0].y,
		lTeam[2].x, lTeam[2].y, rTeam[0].x, rTeam[0].y, rTeam[1].x, rTeam[1].y, rTeam[2].x, rTeam[2].y);
	actions[2] = lTeam[2].takeAction(ball.x - 0.0001, ball.y - 0.0001, ball.vx, ball.vy, lTeam[0].x, lTeam[0].y,
		lTeam[1].x, lTeam[1].y, rTeam[0].x, rTeam[0].y, rTeam[1].x, rTeam[1].y, rTeam[2].x, rTeam[2].y);
	actions[3] = rTeam[0].takeAction(ball.x + 0.0001, ball.y + 0.0001, ball.vx, ball.vy, rTeam[1].x, rTeam[1].y,
		rTeam[2].x, rTeam[2].y, lTeam[0].x, lTeam[0].y, lTeam[1].x, lTeam[1].y, lTeam[2].x, lTeam[2].y);
	actions[4] = rTeam[1].takeAction(ball.x + 0.0001, ball.y + 0.0001, ball.vx, ball.vy, rTeam[0].x, rTeam[0].y,
		rTeam[2].x, rTeam[2].y, lTeam[0].x, lTeam[0].y, lTeam[1].x, lTeam[1].y, lTeam[2].x, lTeam[2].y);
	actions[5] = rTeam[2].takeAction(ball.x + 0.0001, ball.y + 0.0001, ball.vx, ball.vy, rTeam[0].x, rTeam[0].y,
		rTeam[1].x, rTeam[1].y, lTeam[0].x, lTeam[0].y, lTeam[1].x, lTeam[1].y, lTeam[2].x, lTeam[2].y);
	lTeam[0].update(actions[0]);
	lTeam[1].update(actions[1]);
	lTeam[2].update(actions[2]);
	rTeam[0].update(actions[3]);
	rTeam[1].update(actions[4]);
	rTeam[2].update(actions[5]);
	checkPlayerCollisions();
	if (updateBall()) {
	    if (replay) {
		pause = 90;
		lTeam = new Player[] { new Player(-100, 100, false), new Player(-200, -200, false),
			new Player(-400, 0, false) };
		rTeam = new Player[] { new Player(100, -100, true), new Player(200, 200, true),
			new Player(400, 0, true) };
		ball.reset();
		ballStartx = ball.x;
		ballStarty = ball.y;
		replay = false;
		replayTime = 0;
	    } else {
		if (ball.x < 0)
		    rScore++;
		else
		    lScore++;
		pause = 90;
		lTeam = new Player[] { new Player(-100, 100, false), new Player(-200, -200, false),
			new Player(-400, 0, false) };
		rTeam = new Player[] { new Player(100, -100, true), new Player(200, 200, true),
			new Player(400, 0, true) };
		replay = true;
		ball.reset();
		ball.x = ballStartx;
		ball.y = ballStarty;
		int tempTime = replayTime;
		for (int i = 0; i < tempTime - 100; i++) {
		    update();
		}
	    }
	}
    }

    private boolean updateBall() {
	for (int i = 0; i < lTeam.length; i++) {
	    double dist = dist(ball.x, ball.y, lTeam[i].x, lTeam[i].y);
	    if (dist < Player.radius + Ball.radius) {
		ball.vx += (ball.x - lTeam[i].x) / dist * 2.5;
		ball.vy += (ball.y - lTeam[i].y) / dist * 2.5;
		ball.x = lTeam[i].x + (ball.x - lTeam[i].x) * (Player.radius + Ball.radius) / dist;
		ball.y = lTeam[i].y + (ball.y - lTeam[i].y) * (Player.radius + Ball.radius) / dist;
	    }
	    if (dist < Player.radius + Player.shockwaveRadius && lTeam[i].shockwaveFrame > 0) {
		ball.vx += (ball.x - lTeam[i].x) / dist * 20;
		ball.vy += (ball.y - lTeam[i].y) / dist * 20;
	    }
	    dist = dist(ball.x, ball.y, rTeam[i].x, rTeam[i].y);
	    if (dist < Player.radius + Ball.radius) {
		ball.vx += (ball.x - rTeam[i].x) / dist * 2.5;
		ball.vy += (ball.y - rTeam[i].y) / dist * 2.5;
		ball.x = rTeam[i].x + (ball.x - rTeam[i].x) * (Player.radius + Ball.radius) / dist;
		ball.y = rTeam[i].y + (ball.y - rTeam[i].y) * (Player.radius + Ball.radius) / dist;
	    }
	    if (dist < Player.radius + Player.shockwaveRadius && rTeam[i].shockwaveFrame > 0) {
		ball.vx += (ball.x - rTeam[i].x) / dist * 20;
		ball.vy += (ball.y - rTeam[i].y) / dist * 20;
	    }
	}
	double speed = dist(0, 0, ball.vx, ball.vy);
	if (speed > 30) {
	    ball.vx *= 30 / speed;
	    ball.vy *= 30 / speed;
	}
	double tempx = ball.vx;
	double tempy = ball.vy;
	boolean clear = false;
	while (!clear) {
	    clear = true;
	    boolean hitY = false;
	    if (Math.abs(ball.y + tempy) + Ball.radius > GameState.HEIGHT) {
		clear = false;
		ball.vy *= -ballHitSlowRatio;
		if (ball.y > 0) {
		    tempy = -ballHitSlowRatio * (ball.y + tempy + Ball.radius - GameState.HEIGHT);
		    ball.y = GameState.HEIGHT - Ball.radius;
		} else {
		    tempy = -ballHitSlowRatio * (ball.y + tempy - Ball.radius + GameState.HEIGHT);
		    ball.y = -GameState.HEIGHT + Ball.radius;
		}
		hitY = true;
	    }
	    if (Math.abs(ball.x + tempx) + Ball.radius > GameState.LENGTH) {
		clear = false;
		ball.vx *= -ballHitSlowRatio;
		if (ball.x > 0) {
		    tempx = -ballHitSlowRatio * (ball.x + tempx + Ball.radius - GameState.LENGTH);
		    ball.x = GameState.LENGTH - Ball.radius;
		} else {
		    tempx = -ballHitSlowRatio * (ball.x + tempx - Ball.radius + GameState.LENGTH);
		    ball.x = -GameState.LENGTH + Ball.radius;
		}
		if (hitY && dist(0, 0, ball.vx, ball.vy) < 10) {
		    ball.reset();
		    return false;
		}
		if (Math.abs(ball.y) + Ball.radius < GameState.goalHeight) {
		    return true;
		}
	    }
	}
	ball.x += tempx;
	ball.y += tempy;
	ball.vx *= ballSlowRatio;
	ball.vy *= ballSlowRatio;
	return false;

    }

    private void checkPlayerCollisions() {
	Player[] allPlayers = new Player[lTeam.length + rTeam.length];
	for (int i = 0; i < lTeam.length; i++) {
	    allPlayers[i] = lTeam[i];
	}
	for (int i = 0; i < rTeam.length; i++) {
	    allPlayers[i + lTeam.length] = rTeam[i];
	}
	for (int i = 0; i < allPlayers.length; i++) {
	    for (int j = 0; j < allPlayers.length; j++) {
		if (i == j)
		    continue;
		double dist = dist(allPlayers[i].x, allPlayers[i].y, allPlayers[j].x, allPlayers[j].y);
		if (dist < 2 * Player.radius) {
		    allPlayers[i].x = (allPlayers[i].x + allPlayers[j].x) / 2
			    + (allPlayers[i].x - allPlayers[j].x) * Player.radius / dist;
		    allPlayers[j].x = (allPlayers[i].x + allPlayers[j].x) / 2
			    + (allPlayers[i].x - allPlayers[j].x) * Player.radius / -dist;
		    allPlayers[i].y = (allPlayers[i].y + allPlayers[j].y) / 2
			    + (allPlayers[i].y - allPlayers[j].y) * Player.radius / dist;
		    allPlayers[j].y = (allPlayers[i].y + allPlayers[j].y) / 2
			    + (allPlayers[i].y - allPlayers[j].y) * Player.radius / -dist;

		    allPlayers[i].xv += (allPlayers[i].x - allPlayers[j].x) / dist;
		    allPlayers[i].yv += (allPlayers[i].y - allPlayers[j].y) / dist;
		    allPlayers[j].xv += (allPlayers[i].x - allPlayers[j].x) / -dist;
		    allPlayers[j].yv += (allPlayers[i].y - allPlayers[j].y) / -dist;

		}
		if (dist < 2 * Player.radius + Player.shockwaveRadius && allPlayers[i].shockwaveFrame > 0) {
		    allPlayers[j].xv += (allPlayers[i].x - allPlayers[j].x) * 10 / -dist;
		    allPlayers[j].yv += (allPlayers[i].y - allPlayers[j].y) * 10 / -dist;
		}
		if (dist < 2 * Player.radius + Player.shockwaveRadius && allPlayers[j].shockwaveFrame > 0) {
		    allPlayers[i].xv += (allPlayers[i].x - allPlayers[j].x) * 10 / dist;
		    allPlayers[i].yv += (allPlayers[i].y - allPlayers[j].y) * 10 / dist;
		}
	    }
	}
    }

    public void draw(GraphicsContext g, int width, int height) {
	Affine origTransform = g.getTransform().clone();
	g.translate(width / 2, height / 2);

	g.setFill(Color.GRAY);
	g.fillRect(-LENGTH, -HEIGHT, 2 * LENGTH, 2 * HEIGHT);
	for (int i = 0; i < lTeam.length; i++) {
	    lTeam[i].drawShockwave(g);
	}
	for (int i = 0; i < rTeam.length; i++) {
	    rTeam[i].drawShockwave(g);
	}
	for (int i = 0; i < lTeam.length; i++) {
	    lTeam[i].drawPlayer(g);
	}
	for (int i = 0; i < rTeam.length; i++) {
	    rTeam[i].drawPlayer(g);
	}
	ball.draw(g);

	g.setFill(Color.BLACK);
	g.fillRect(-width / 2, -height / 2, width / 2 - LENGTH, height);
	g.fillRect(LENGTH, -height / 2, width / 2 - LENGTH, height);
	g.fillRect(-LENGTH, -height / 2, 2 * LENGTH, height / 2 - HEIGHT);
	g.fillRect(-LENGTH, HEIGHT, 2 * LENGTH, height / 2 - HEIGHT);
	g.setFill(Color.ORANGE);
	g.fillRect(-width / 2, -goalHeight, width / 2 - LENGTH, goalHeight * 2);
	g.setFill(Color.BLUE);
	g.fillRect(LENGTH, -goalHeight, width / 2 - LENGTH, goalHeight * 2);
	g.setFont(new Font("timesRoman", 70));
	if (pause != 0) {
	    g.setFill(Color.DARKGRAY);
	    g.fillRect(-width / 2, -height / 2, 80, 70);
	    g.setFill(Color.WHITE);
	    g.fillRect(-width / 2 + 20, -height / 2 + 10, 10, 50);
	    g.fillRect(-width / 2 + 50, -height / 2 + 10, 10, 50);
	    if (pause > 0) {
		g.fillText("" + (int) (pause / 30 + 1), -width / 2 + 100, -height / 2 + 70);
	    }
	}
	g.setFill(Color.WHITE);
	g.fillText("" + lScore, -width / 2 + 50, height / 2 - 20);
	g.fillText("" + rScore, width / 2 - 100, height / 2 - 20);
	if (replay) {
	    g.fillText("" + replayTime / 60, 0, -height / 2 + 50);
	    g.setFill(Color.YELLOW);
	    g.fillText("REPLAY", width / 2 - 300, -height / 2 + 50);
	} else {
	    g.fillText("" + time / 60, 0, -height / 2 + 50);
	}
	g.setTransform(origTransform);
    }
}
