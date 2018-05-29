package org.tero.ai_soccer.sim;

import org.tero.ai_soccer.ai.AiPlayer;
import org.tero.ai_soccer.ai.NeuralNet;
import org.tero.ai_soccer.util.Context;
import org.tero.ai_soccer.util.Vector2D;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Affine;
import not.my.code.Action;

public class GameState {
    private static final double FIELD_LENGTH = 500;
    private static final double FIELD_WIDTH = 300;

    private static final double GOAL_HEIGHT = 100;
    private static final double BALL_SLOW_RATIO = 0.99;
    private static final double BALL_HIT_SLOW_RATIO = 0.8;

    private final NeuralNet leftAi;
    private final NeuralNet rightAi;

    private boolean isReplaying;
    private int replayTimeLeft;

    private int time;
    private int pause; // -1 is permanent, 0 is not paused, >0 is a count-down.

    private Ball ball;
    private Vector2D ballStartPos;

    private Player[] lTeam;
    private Player[] rTeam;
    private int lScore;
    private int rScore;

    public GameState(NeuralNet ai1, NeuralNet ai2) {
	isReplaying = false;
	replayTimeLeft = 0;

	time = 0;
	pause = 90;

	ball = new Ball();
	ballStartPos = new Vector2D(ball.x, ball.y);

	lScore = 0;
	rScore = 0;

	leftAi = ai1;
	rightAi = ai2;
	fillTeams(ai1, ai2);
    }

    private void reset() {
	ball.reset();
	ballStartPos = new Vector2D(ball.x, ball.y);
    }

    private void fillTeams(NeuralNet leftAi, NeuralNet rightAi) {
	Vector2D[] initialPos = { Vector2D.of(-100, 100), Vector2D.of(-200, -200), Vector2D.of(-400, 0) };

	lTeam = new Player[3];
	for (int i = 0; i < 3; i++) {
	    if (leftAi != null) {
		lTeam[i] = new AiPlayer(initialPos[i], false, i, leftAi);
	    } else {
		lTeam[i] = new Player(initialPos[i], false, i);
	    }
	}

	rTeam = new Player[3];
	for (int i = 0; i < 3; i++) {
	    if (rightAi != null) {
		rTeam[i] = new AiPlayer(initialPos[i].times(-1), true, i, rightAi);
	    } else {
		rTeam[i] = new Player(initialPos[i].times(-1), true, i);
	    }
	}
    }

    public void update() {
	if (pause > 0) {
	    pause--;
	    return;
	}
	if (pause == -1) {
	    return;
	}
	if (isReplaying) {
	    replayTimeLeft--;
	} else {
	    time++;
	    replayTimeLeft++;
	}

	double[] prevX = new double[6];
	double[] prevY = new double[6];
	prevX[0] = lTeam[0].pos.x;
	prevX[1] = lTeam[1].pos.x;
	prevX[2] = lTeam[2].pos.x;
	prevX[3] = rTeam[0].pos.x;
	prevX[4] = rTeam[1].pos.x;
	prevX[5] = rTeam[2].pos.x;
	prevY[0] = lTeam[0].pos.y;
	prevY[1] = lTeam[1].pos.y;
	prevY[2] = lTeam[2].pos.y;
	prevY[3] = rTeam[0].pos.y;
	prevY[4] = rTeam[1].pos.y;
	prevY[5] = rTeam[2].pos.y;
	Action[] actions = new Action[lTeam.length + rTeam.length];
	Context context = new Context(ball.x - 0.0001, ball.y - 0.0001, ball.vx, ball.vy, lTeam[0].pos.x,
		lTeam[0].pos.y, lTeam[1].pos.x, lTeam[1].pos.y, lTeam[2].pos.x, lTeam[2].pos.y, rTeam[0].pos.x,
		rTeam[0].pos.y, rTeam[1].pos.x, rTeam[1].pos.y, rTeam[2].pos.x, rTeam[2].pos.y);

	for (int i = 0; i < 3; i++) {
	    actions[i] = lTeam[i].takeAction(context);
	    actions[3 + i] = rTeam[i].takeAction(context);
	}
	for (int i = 0; i < 3; i++) {
	    lTeam[i].update(actions[i]);
	    rTeam[i].update(actions[3 + i]);
	}

	checkPlayerCollisions();
	if (updateBall()) {
	    if (isReplaying) {
		pause = 90;

		fillTeams(leftAi, rightAi);

		reset();
		isReplaying = false;
		replayTimeLeft = 0;
	    } else {
		if (ball.x < 0)
		    rScore++;
		else
		    lScore++;
		pause = 90;

		fillTeams(leftAi, rightAi);

		isReplaying = true;
		ball.reset();
		ball.x = ballStartPos.x;
		ball.y = ballStartPos.y;
		int tempTime = replayTimeLeft;
		for (int i = 0; i < tempTime - 100; i++) {
		    update();
		}
	    }
	}
    }

    private boolean updateBall() {
	for (int i = 0; i < lTeam.length; i++) {
	    double dist = Vector2D.of(lTeam[i].pos.x - ball.x, lTeam[i].pos.y - ball.y).magnitude();
	    if (dist < Player.RADIUS + Ball.radius) {
		ball.vx += (ball.x - lTeam[i].pos.x) / dist * 2.5;
		ball.vy += (ball.y - lTeam[i].pos.y) / dist * 2.5;
		ball.x = lTeam[i].pos.x + (ball.x - lTeam[i].pos.x) * (Player.RADIUS + Ball.radius) / dist;
		ball.y = lTeam[i].pos.y + (ball.y - lTeam[i].pos.y) * (Player.RADIUS + Ball.radius) / dist;
	    }
	    if (dist < Player.RADIUS + Player.SHOCKWAVE_RADIUS && lTeam[i].shockwaveAnimationFramesLeft > 0) {
		ball.vx += (ball.x - lTeam[i].pos.x) / dist * 20;
		ball.vy += (ball.y - lTeam[i].pos.y) / dist * 20;
	    }
	    dist = Vector2D.of(rTeam[i].pos.x - ball.x, rTeam[i].pos.y - ball.y).magnitude();
	    if (dist < Player.RADIUS + Ball.radius) {
		ball.vx += (ball.x - rTeam[i].pos.x) / dist * 2.5;
		ball.vy += (ball.y - rTeam[i].pos.y) / dist * 2.5;
		ball.x = rTeam[i].pos.x + (ball.x - rTeam[i].pos.x) * (Player.RADIUS + Ball.radius) / dist;
		ball.y = rTeam[i].pos.y + (ball.y - rTeam[i].pos.y) * (Player.RADIUS + Ball.radius) / dist;
	    }
	    if (dist < Player.RADIUS + Player.SHOCKWAVE_RADIUS && rTeam[i].shockwaveAnimationFramesLeft > 0) {
		ball.vx += (ball.x - rTeam[i].pos.x) / dist * 20;
		ball.vy += (ball.y - rTeam[i].pos.y) / dist * 20;
	    }
	}
	double speed = Vector2D.of(ball.vx, ball.vy).magnitude();
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
	    if (Math.abs(ball.y + tempy) + Ball.radius > GameState.FIELD_WIDTH) {
		clear = false;
		ball.vy *= -BALL_HIT_SLOW_RATIO;
		if (ball.y > 0) {
		    tempy = -BALL_HIT_SLOW_RATIO * (ball.y + tempy + Ball.radius - GameState.FIELD_WIDTH);
		    ball.y = GameState.FIELD_WIDTH - Ball.radius;
		} else {
		    tempy = -BALL_HIT_SLOW_RATIO * (ball.y + tempy - Ball.radius + GameState.FIELD_WIDTH);
		    ball.y = -GameState.FIELD_WIDTH + Ball.radius;
		}
		hitY = true;
	    }
	    if (Math.abs(ball.x + tempx) + Ball.radius > GameState.FIELD_LENGTH) {
		clear = false;
		ball.vx *= -BALL_HIT_SLOW_RATIO;
		if (ball.x > 0) {
		    tempx = -BALL_HIT_SLOW_RATIO * (ball.x + tempx + Ball.radius - GameState.FIELD_LENGTH);
		    ball.x = GameState.FIELD_LENGTH - Ball.radius;
		} else {
		    tempx = -BALL_HIT_SLOW_RATIO * (ball.x + tempx - Ball.radius + GameState.FIELD_LENGTH);
		    ball.x = -GameState.FIELD_LENGTH + Ball.radius;
		}
		if (hitY && Vector2D.of(ball.vx, ball.vy).magnitude() < 10) {
		    ball.reset();
		    return false;
		}
		if (Math.abs(ball.y) + Ball.radius < GameState.GOAL_HEIGHT) {
		    return true;
		}
	    }
	}
	ball.x += tempx;
	ball.y += tempy;
	ball.vx *= BALL_SLOW_RATIO;
	ball.vy *= BALL_SLOW_RATIO;
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
		double dist = Vector2D
			.of(allPlayers[i].pos.x - allPlayers[j].pos.x, allPlayers[i].pos.y - allPlayers[j].pos.y)
			.magnitude();
		if (dist < 2 * Player.RADIUS) {
		    allPlayers[i].pos = Vector2D.of(
			    (allPlayers[i].pos.x + allPlayers[j].pos.x) / 2
				    + (allPlayers[i].pos.x - allPlayers[j].pos.x) * Player.RADIUS / dist,
			    (allPlayers[i].pos.y + allPlayers[j].pos.y) / 2
				    + (allPlayers[i].pos.y - allPlayers[j].pos.y) * Player.RADIUS / dist);

		    allPlayers[j].pos = Vector2D.of(
			    (allPlayers[i].pos.x + allPlayers[j].pos.x) / 2
				    + (allPlayers[i].pos.x - allPlayers[j].pos.x) * Player.RADIUS / -dist,
			    (allPlayers[i].pos.y + allPlayers[j].pos.y) / 2
				    + (allPlayers[i].pos.y - allPlayers[j].pos.y) * Player.RADIUS / -dist);

		    allPlayers[i].vel = allPlayers[i].vel
			    .plus(Vector2D.of((allPlayers[i].pos.x - allPlayers[j].pos.x) / dist,
				    (allPlayers[i].pos.y - allPlayers[j].pos.y) / dist));
		    allPlayers[j].vel = allPlayers[j].vel
			    .plus(Vector2D.of((allPlayers[i].pos.x - allPlayers[j].pos.x) / -dist,
				    (allPlayers[i].pos.y - allPlayers[j].pos.y) / -dist));
		}
		if (dist < 2 * Player.RADIUS + Player.SHOCKWAVE_RADIUS
			&& allPlayers[i].shockwaveAnimationFramesLeft > 0) {
		    allPlayers[j].vel = allPlayers[j].vel
			    .plus(Vector2D.of((allPlayers[i].pos.x - allPlayers[j].pos.x) * 10 / -dist,
				    (allPlayers[i].pos.y - allPlayers[j].pos.y) * 10 / -dist));
		}
		if (dist < 2 * Player.RADIUS + Player.SHOCKWAVE_RADIUS
			&& allPlayers[j].shockwaveAnimationFramesLeft > 0) {
		    allPlayers[i].vel = allPlayers[i].vel
			    .plus(Vector2D.of((allPlayers[i].pos.x - allPlayers[j].pos.x) * 10 / dist,
				    (allPlayers[i].pos.y - allPlayers[j].pos.y) * 10 / dist));
		}
	    }
	}
    }

    public void draw(GraphicsContext g, int width, int height) {
	Affine origTransform = g.getTransform().clone();
	g.translate(width / 2, height / 2);

	g.setFill(Color.GRAY);
	g.fillRect(-FIELD_LENGTH, -FIELD_WIDTH, 2 * FIELD_LENGTH, 2 * FIELD_WIDTH);
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
	g.fillRect(-width / 2, -height / 2, width / 2 - FIELD_LENGTH, height);
	g.fillRect(FIELD_LENGTH, -height / 2, width / 2 - FIELD_LENGTH, height);
	g.fillRect(-FIELD_LENGTH, -height / 2, 2 * FIELD_LENGTH, height / 2 - FIELD_WIDTH);
	g.fillRect(-FIELD_LENGTH, FIELD_WIDTH, 2 * FIELD_LENGTH, height / 2 - FIELD_WIDTH);
	g.setFill(Color.ORANGE);
	g.fillRect(-width / 2, -GOAL_HEIGHT, width / 2 - FIELD_LENGTH, GOAL_HEIGHT * 2);
	g.setFill(Color.BLUE);
	g.fillRect(FIELD_LENGTH, -GOAL_HEIGHT, width / 2 - FIELD_LENGTH, GOAL_HEIGHT * 2);
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
	if (isReplaying) {
	    g.fillText("" + replayTimeLeft / 60, 0, -height / 2 + 50);
	    g.setFill(Color.YELLOW);
	    g.fillText("REPLAY", width / 2 - 300, -height / 2 + 50);
	} else {
	    g.fillText("" + time / 60, 0, -height / 2 + 50);
	}
	g.setTransform(origTransform);
    }

    public int getLeftScore() {
	return lScore;
    }

    public int getRightScore() {
	return rScore;
    }
}
