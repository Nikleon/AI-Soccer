package org.tero.ai_soccer.sim.env;

import java.util.Arrays;

import org.tero.ai_soccer.ai.NeuralNet;
import org.tero.ai_soccer.sim.Ball;
import org.tero.ai_soccer.sim.GameState;
import org.tero.ai_soccer.sim.Player;
import org.tero.ai_soccer.util.Vector2D;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Transform;

public class Soccer extends Environment {
	private static final double HALF_FIELD_LENGTH = 500;
	private static final double HALF_FIELD_WIDTH = 300;
	private static final double GOAL_HEIGHT = 100;
	private static final double BALL_RADIUS = 15;
	private static final double PLAYER_RADIUS = 20;
	private static final double SHOCKWAVE_INNER_RADIUS = 20;
	private static final Paint LEFT_FILL = Color.ORANGE;
	private static final Paint RIGHT_FILL = Color.BLUE;
	private static final boolean DRAW_TRAIL = true;
	private static final double ANDREW_MAX_SPEED = 3;
	private static final int TEAM_SIZE = 3;

	private static final int COUNTDOWN_FRAMES = 90;
	private int countdown;
	private int time;

	private SoccerPlayer lastHitter;

	private NeuralNet leftAi;
	private NeuralNet rightAi;

	private SoccerContext context;

	private SoccerPlayer[] leftTeam = new SoccerPlayer[TEAM_SIZE];
	private SoccerPlayer[] rightTeam = new SoccerPlayer[TEAM_SIZE];

	public Soccer(NeuralNet ai1, NeuralNet ai2) {
		leftAi = ai1;
		rightAi = ai2;

		for (int id = 0; id < TEAM_SIZE; id++) {
			leftTeam[id] = new SoccerPlayer(leftAi, true, id);
			rightTeam[id] = new SoccerPlayer(rightAi, false, id);
		}

		reset();
	}

	@Override
	public void update() {
		if (countdown > 0) {
			countdown--;
			return;
		}
		if (countdown == -1) {
			return;
		}
		time++;

		for (int id = 0; id < TEAM_SIZE; id++) {
			leftTeam[id].getNextAction().apply();
			rightTeam[id].getNextAction().apply();
		}

		checkPlayerCollisions();
		updateBall();
	}

	private void checkPlayerCollisions() {

	}

	private void updateBall() {
		Vector2D pos = context.ballPos();
		Vector2D fPos = pos.plus(context.ballVel());

		if (Math.abs(fPos.x) + BALL_RADIUS > HALF_FIELD_LENGTH) {
			if (Math.abs(fPos.y) < GOAL_HEIGHT / 2) {
				awardGoal();
				reset();
				return;
			}

			// TODO: continue this holy shit makes sure you have nothing due!!
		}
		/*
		 * for (int i = 0; i < lTeam.length; i++) { double dist =
		 * Vector2D.of(lTeam[i].pos.x - ball.x, lTeam[i].pos.y - ball.y).magnitude(); if
		 * (dist < Player.RADIUS + Ball.radius) { ball.vx += (ball.x - lTeam[i].pos.x) /
		 * dist * 2.5; ball.vy += (ball.y - lTeam[i].pos.y) / dist * 2.5; ball.x =
		 * lTeam[i].pos.x + (ball.x - lTeam[i].pos.x) * (Player.RADIUS + Ball.radius) /
		 * dist; ball.y = lTeam[i].pos.y + (ball.y - lTeam[i].pos.y) * (Player.RADIUS +
		 * Ball.radius) / dist; } if (dist < Player.RADIUS + Player.SHOCKWAVE_RADIUS &&
		 * lTeam[i].shockwaveAnimationFramesLeft > 0) { ball.vx += (ball.x -
		 * lTeam[i].pos.x) / dist * 20; ball.vy += (ball.y - lTeam[i].pos.y) / dist *
		 * 20; } dist = Vector2D.of(rTeam[i].pos.x - ball.x, rTeam[i].pos.y -
		 * ball.y).magnitude(); if (dist < Player.RADIUS + Ball.radius) { ball.vx +=
		 * (ball.x - rTeam[i].pos.x) / dist * 2.5; ball.vy += (ball.y - rTeam[i].pos.y)
		 * / dist * 2.5; ball.x = rTeam[i].pos.x + (ball.x - rTeam[i].pos.x) *
		 * (Player.RADIUS + Ball.radius) / dist; ball.y = rTeam[i].pos.y + (ball.y -
		 * rTeam[i].pos.y) * (Player.RADIUS + Ball.radius) / dist; } if (dist <
		 * Player.RADIUS + Player.SHOCKWAVE_RADIUS &&
		 * rTeam[i].shockwaveAnimationFramesLeft > 0) { ball.vx += (ball.x -
		 * rTeam[i].pos.x) / dist * 20; ball.vy += (ball.y - rTeam[i].pos.y) / dist *
		 * 20; } } double speed = Vector2D.of(ball.vx, ball.vy).magnitude(); if (speed >
		 * 30) { ball.vx *= 30 / speed; ball.vy *= 30 / speed; } double tempx = ball.vx;
		 * double tempy = ball.vy; boolean clear = false; while (!clear) { clear = true;
		 * boolean hitY = false; if (Math.abs(ball.y + tempy) + Ball.radius >
		 * GameState.FIELD_WIDTH) { clear = false; ball.vy *= -BALL_HIT_SLOW_RATIO; if
		 * (ball.y > 0) { tempy = -BALL_HIT_SLOW_RATIO * (ball.y + tempy + Ball.radius -
		 * GameState.FIELD_WIDTH); ball.y = GameState.FIELD_WIDTH - Ball.radius; } else
		 * { tempy = -BALL_HIT_SLOW_RATIO * (ball.y + tempy - Ball.radius +
		 * GameState.FIELD_WIDTH); ball.y = -GameState.FIELD_WIDTH + Ball.radius; } hitY
		 * = true; } if (Math.abs(ball.x + tempx) + Ball.radius >
		 * GameState.FIELD_LENGTH) { clear = false; ball.vx *= -BALL_HIT_SLOW_RATIO; if
		 * (ball.x > 0) { tempx = -BALL_HIT_SLOW_RATIO * (ball.x + tempx + Ball.radius -
		 * GameState.FIELD_LENGTH); ball.x = GameState.FIELD_LENGTH - Ball.radius; }
		 * else { tempx = -BALL_HIT_SLOW_RATIO * (ball.x + tempx - Ball.radius +
		 * GameState.FIELD_LENGTH); ball.x = -GameState.FIELD_LENGTH + Ball.radius; } if
		 * (hitY && Vector2D.of(ball.vx, ball.vy).magnitude() < 10) { ball.reset();
		 * return false; } if (Math.abs(ball.y) + Ball.radius < GameState.GOAL_HEIGHT) {
		 * return true; } } } ball.x += tempx; ball.y += tempy; ball.vx *=
		 * BALL_SLOW_RATIO; ball.vy *= BALL_SLOW_RATIO;
		 * 
		 * return false;
		 */

	}

	private void awardGoal() {
		if (lastHitter == null)
			return;

		if (context.ballPos().x < 0) {
			if (lastHitter.leftTeam)
				lastHitter.scoreMap.modify("owngoals", +1);
			else
				lastHitter.scoreMap.modify("goals", +1);
		} else {
			if (lastHitter.leftTeam)
				lastHitter.scoreMap.modify("goals", +1);
			else
				lastHitter.scoreMap.modify("owngoals", +1);
		}
	}

	@Override
	public void reset() {
		// randomize ball spawn
		double ballX = 3 * (Math.random() - 0.5);
		double ballY = 3 * (Math.random() - 0.5);

		// generate spawn points
		Vector2D[] spawnPoints = generateSpawnPoints();

		// initialize game state
		context = new SoccerContext(spawnPoints, ballX, ballY, 0, 0);

		// initialize time counts
		countdown = COUNTDOWN_FRAMES;
	}

	private Vector2D[] generateSpawnPoints() {
		Vector2D[] spawnPos = new Vector2D[TEAM_SIZE];
		for (int id = 0; id < TEAM_SIZE; id++) {
			// choose random spawn point in left half
			spawnPos[id] = new Vector2D(-Math.random() * HALF_FIELD_LENGTH,
					2 * (Math.random() - 0.5) * HALF_FIELD_WIDTH);

			// scale spawn region away from walls
			spawnPos[id] = spawnPos[id].times(0.9);

			// check if spawn is too close to the ball
			if (spawnPos[id].magnitude() < 0.1) {
				id--;
				continue;
			}

			// confirm no collision with other teammate spawn
			for (int prevId = id - 1; prevId >= 0; prevId--)
				if (spawnPos[id].distTo(spawnPos[prevId]) < 2 * PLAYER_RADIUS) {
					id--;
					continue;
				}
		}
		return spawnPos;
	}

	@Override
	public double[] getScores() {
		double leftScoreTotal = Arrays.stream(leftTeam).mapToDouble(player -> player.getScore()).sum();
		double rightScoreTotal = Arrays.stream(rightTeam).mapToDouble(player -> player.getScore()).sum();
		return new double[] { leftScoreTotal, rightScoreTotal };
	}

	@Override
	public void draw(double w, double h, GraphicsContext g) {
		g.translate(w / 2, h / 2);

		// draw field
		g.setFill(Color.GRAY);
		g.fillRect(-HALF_FIELD_LENGTH, -HALF_FIELD_WIDTH, 2 * HALF_FIELD_LENGTH, 2 * HALF_FIELD_WIDTH);

		// draw shockwaves
		g.setFill(Color.WHITE);
		for (int id = 0; id < TEAM_SIZE; id++) {
			if (leftTeam[id].shockwaveWtf > 0) {
				Vector2D pos = context.playerPos(true, id);
				// TODO: whats this /5 doing here?
				double shockwaveRadius = PLAYER_RADIUS + (leftTeam[id].shockwaveWtf * SHOCKWAVE_INNER_RADIUS) / 5;
				g.fillOval(pos.x - shockwaveRadius, pos.y - shockwaveRadius, 2 * shockwaveRadius, 2 * shockwaveRadius);
			}
			if (rightTeam[id].shockwaveWtf > 0) {
				Vector2D pos = context.playerPos(false, id);
				double shockwaveRadius = PLAYER_RADIUS + (rightTeam[id].shockwaveWtf * SHOCKWAVE_INNER_RADIUS) / 5;
				g.fillOval(pos.x - shockwaveRadius, pos.y - shockwaveRadius, 2 * shockwaveRadius, 2 * shockwaveRadius);
			}
		}

		// draw left players
		g.setFill(LEFT_FILL);
		g.setStroke(Color.BLACK);
		for (int id = 0; id < TEAM_SIZE; id++) {
			Vector2D pos = context.playerPos(true, id);
			g.fillOval(pos.x - PLAYER_RADIUS, pos.y - PLAYER_RADIUS, 2 * PLAYER_RADIUS, 2 * PLAYER_RADIUS);
			g.strokeOval(pos.x - PLAYER_RADIUS, pos.y - PLAYER_RADIUS, 2 * PLAYER_RADIUS, 2 * PLAYER_RADIUS);
		}

		// draw right players
		g.setFill(RIGHT_FILL);
		g.setStroke(Color.BLACK);
		for (int id = 0; id < TEAM_SIZE; id++) {
			Vector2D pos = context.playerPos(true, id);
			g.fillOval(pos.x - PLAYER_RADIUS, pos.y - PLAYER_RADIUS, 2 * PLAYER_RADIUS, 2 * PLAYER_RADIUS);
			g.strokeOval(pos.x - PLAYER_RADIUS, pos.y - PLAYER_RADIUS, 2 * PLAYER_RADIUS, 2 * PLAYER_RADIUS);
		}

		// draw trail
		if (DRAW_TRAIL && lastHitter != null) {
			if (lastHitter.leftTeam)
				g.setStroke(LEFT_FILL);
			else
				g.setStroke(RIGHT_FILL);
			Vector2D pos = context.ballPos();
			Vector2D vel = context.ballVel();
			double scale = 5;
			g.strokeLine(pos.x, pos.y, pos.x + scale * vel.x, pos.y + scale * vel.y);
		}

		// draw ball
		g.setFill(Color.WHITE);
		g.setStroke(Color.BLACK);
		Vector2D ballPos = context.ballPos();
		g.fillOval(ballPos.x - BALL_RADIUS, ballPos.y - BALL_RADIUS, BALL_RADIUS * 2, BALL_RADIUS * 2);
		g.strokeOval(ballPos.x - BALL_RADIUS, ballPos.y - BALL_RADIUS, BALL_RADIUS * 2, BALL_RADIUS * 2);

		// draw walls
		g.setFill(Color.BLACK);
		g.fillRect(-w / 2, -h / 2, w / 2 - HALF_FIELD_LENGTH, h);
		g.fillRect(HALF_FIELD_LENGTH, -h / 2, w / 2 - HALF_FIELD_LENGTH, h);
		g.fillRect(-HALF_FIELD_LENGTH, -h / 2, 2 * HALF_FIELD_LENGTH, h / 2 - HALF_FIELD_LENGTH);
		g.fillRect(-HALF_FIELD_LENGTH, HALF_FIELD_LENGTH, 2 * HALF_FIELD_LENGTH, h / 2 - HALF_FIELD_LENGTH);

		// draw goals
		g.setFill(LEFT_FILL);
		g.fillRect(-w / 2, -GOAL_HEIGHT, w / 2 - HALF_FIELD_LENGTH, GOAL_HEIGHT * 2);
		g.setFill(RIGHT_FILL);
		g.fillRect(HALF_FIELD_LENGTH, -GOAL_HEIGHT, w / 2 - HALF_FIELD_LENGTH, GOAL_HEIGHT * 2);

		// draw countdown if applicable
		if (countdown != 0) {
			g.setFill(Color.DARKGRAY);
			g.fillRect(-w / 2, -h / 2, 80, 70);

			g.setFill(Color.WHITE);
			g.fillRect(-w / 2 + 20, -h / 2 + 10, 10, 50);
			g.fillRect(-w / 2 + 50, -h / 2 + 10, 10, 50);

			if (countdown > 0) {
				g.setFont(Font.font("timesRoman", 70));
				g.fillText("" + (int) (countdown / 30 + 1), -w / 2 + 100, -h / 2 + 70);
			}
		}

		g.setFill(Color.WHITE);
		g.setFont(Font.font("timesRoman", 70));
		double[] scores = getScores();
		g.fillText(String.format("%.2f", scores[0]), -w / 2 + 50, h / 2 - 20);
		g.fillText(String.format("%.2f", scores[1]), w / 2 - 250, h / 2 - 20);

		g.translate(-w / 2, -h / 2);
	}

	private class SoccerContext extends Soccer.Context {
		static final int BALL_POS_OFFSET = 0;
		static final int BALL_VEL_OFFSET = 2;
		static final int LEFT_POS_OFFSET = 4;
		static final int RIGHT_POS_OFFSET = LEFT_POS_OFFSET + 2 * TEAM_SIZE;

		SoccerContext(Vector2D[] spawnPoints, double... data) {
			super(new double[data.length + 2 * TEAM_SIZE]);
			System.arraycopy(data, 0, this.data, 0, data.length);

			for (int id = 0; id < TEAM_SIZE; id++) {
				this.data[LEFT_POS_OFFSET + 2 * id] = spawnPoints[id].x;
				this.data[LEFT_POS_OFFSET + 2 * id + 1] = spawnPoints[id].y;
				this.data[RIGHT_POS_OFFSET + 2 * id] = -spawnPoints[id].x;
				this.data[RIGHT_POS_OFFSET + 2 * id + 1] = -spawnPoints[id].y;
			}
		}

		@Override
		double[] getRelativeData(Transform transform) {
			double[] data = this.data.clone();
			transform.transform2DPoints(data, 0, data, 0, data.length / 2);
			return data;
		}

		double[] getRelativeData(Transform transform, boolean leftTeam, int id) {
			double[] relativeData = getRelativeData(transform);
			double[] unbiasedData = new double[relativeData.length];

			// determine input ordering
			int[] idOrder = new int[TEAM_SIZE];
			for (int i = 0; i < TEAM_SIZE; i++)
				idOrder[i] = (id + i) % TEAM_SIZE;
			int allyOffset = (leftTeam) ? LEFT_POS_OFFSET : RIGHT_POS_OFFSET;
			int enemyOffset = (leftTeam) ? RIGHT_POS_OFFSET : LEFT_POS_OFFSET;
			int index = 0;

			// fill data array
			unbiasedData[index++] = relativeData[allyOffset + 2 * idOrder[0]];
			unbiasedData[index++] = relativeData[allyOffset + 2 * idOrder[0] + 1];

			unbiasedData[index++] = relativeData[BALL_POS_OFFSET];
			unbiasedData[index++] = relativeData[BALL_POS_OFFSET + 1];
			unbiasedData[index++] = relativeData[BALL_VEL_OFFSET];
			unbiasedData[index++] = relativeData[BALL_VEL_OFFSET + 1];

			for (int i = 1; i < TEAM_SIZE; i++) {
				unbiasedData[index++] = relativeData[allyOffset + 2 * idOrder[i]];
				unbiasedData[index++] = relativeData[allyOffset + 2 * idOrder[i] + 1];
			}

			for (int i = 0; i < TEAM_SIZE; i++) {
				unbiasedData[index++] = relativeData[enemyOffset + 2 * idOrder[i]];
				unbiasedData[index++] = relativeData[enemyOffset + 2 * idOrder[i] + 1];
			}

			return unbiasedData;
		}

		Vector2D ballPos() {
			return new Vector2D(data[BALL_POS_OFFSET], data[BALL_POS_OFFSET + 1]);
		}

		void setBallPos(Vector2D pos) {
			data[BALL_POS_OFFSET] = pos.x;
			data[BALL_POS_OFFSET + 1] = pos.y;
		}

		Vector2D ballVel() {
			return new Vector2D(data[BALL_VEL_OFFSET], data[BALL_VEL_OFFSET + 1]);
		}

		void setBallVel(Vector2D vel) {
			data[BALL_VEL_OFFSET] = vel.x;
			data[BALL_VEL_OFFSET + 1] = vel.y;
		}

		Vector2D playerPos(boolean leftTeam, int id) {
			int teamOffset = (leftTeam) ? LEFT_POS_OFFSET : RIGHT_POS_OFFSET;
			return new Vector2D(data[teamOffset + 2 * id], data[teamOffset + 2 * id + 1]);
		}
	}

	private class SoccerPlayerAction extends Soccer.Action {
		// { MOVE_X, MOVE_Y, HIT }

		SoccerPlayerAction(double[] commands) {
			super(commands);
		}

		@Override
		SoccerPlayerAction toGlobal(Transform transform) {
			try {
				double[] globalMoveCommands = this.commands.clone();
				transform.inverseTransform2DPoints(globalMoveCommands, 0, globalMoveCommands, 0, 1);
				return new SoccerPlayerAction(globalMoveCommands);
			} catch (NonInvertibleTransformException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		void apply() {
			// TODO
		}
	}

	private class SoccerPlayer extends Soccer.Entity {
		Transform transform;
		boolean leftTeam;
		int id;
		double shockwaveWtf; // TODO: this shouldnt have to be here. also wtf does this represent?

		SoccerPlayer(NeuralNet controller, boolean leftTeam, int id) {
			super(controller);
			transform = (leftTeam) ? new Affine() : Transform.rotate(180, 0, 0);
			this.leftTeam = leftTeam;
			this.id = id;
			shockwaveWtf = 0;
		}

		@Override
		SoccerPlayerAction getNextAction() {
			// get relative inputs
			double[] relativeData = context.getRelativeData(this.transform, leftTeam, id);

			SoccerPlayerAction relativeAction = null;
			if (controller != null) {
				// decide on action
				relativeAction = new SoccerPlayerAction(this.controller.predict(relativeData));
			} else {
				// use default bot
				relativeAction = applyAndrewMethod(relativeData);
			}

			// transform relative action to global action
			return relativeAction.toGlobal(this.transform);
		}

		@Override
		double getScore() {
			return scoreMap.get("goals");
		}
	}

	SoccerPlayerAction applyAndrewMethod(double[] relativeData) {
		int index = 0;
		double x = relativeData[index++];
		double y = relativeData[index++];
		double b_x = relativeData[index++];
		double b_y = relativeData[index++];
		double b_vx = relativeData[index++];
		double b_vy = relativeData[index++];
		double a1_x = relativeData[index++];
		double a1_y = relativeData[index++];
		double a2_x = relativeData[index++];
		double a2_y = relativeData[index++];

		// Farthest away from the ball
		if (dist(x, y, b_x, b_y) > dist(a1_x, a1_y, b_x, b_y) && dist(x, y, b_x, b_y) > dist(a2_x, a2_y, b_x, b_y)
				&& x < b_x) {
			double moveX = 0;
			double moveY = 0;
			boolean hit = false;

			moveX = -500 + PLAYER_RADIUS - x;
			moveY = b_y - y;
			if (b_y > GOAL_HEIGHT - 30) {
				moveY = GOAL_HEIGHT - y - 30;
			}
			if (b_y < -GOAL_HEIGHT + 30) {
				moveY = -GOAL_HEIGHT - y + 30;
			}
			if (dist(x, y, b_x, b_y) < PLAYER_RADIUS + SHOCKWAVE_INNER_RADIUS && x < b_x) {
				hit = true;
			}
			return new SoccerPlayerAction(new double[] { moveX, moveY, (hit) ? 1 : -1 });
		}

		// Other 2
		double moveX = 0;
		double moveY = 0;
		boolean hit = false;

		// Check movement
		if (x > b_x)
			moveX = -5;
		else if (Math.abs((y - b_y) / (x - b_x)) > .5) {
			moveY = b_y - y;
			if (x - b_x < -100)
				moveX = b_x - x;
		} else {
			moveX = b_x - x;
			moveY = b_y - y;
		}

		if (x < -400 && Math.abs(b_y + b_vy / b_vx * (-HALF_FIELD_LENGTH - b_x - BALL_RADIUS)) < GOAL_HEIGHT
				&& b_vx < 0) {
			if (b_y > y)
				moveY = b_y + 7 + b_vy / b_vx * (-HALF_FIELD_LENGTH - b_x + PLAYER_RADIUS) - y;
			else
				moveY = b_y - 7 + b_vy / b_vx * (-HALF_FIELD_LENGTH - b_x + PLAYER_RADIUS) - y;
			moveX = 0;
		}

		if (dist(x, y, b_x, b_y) < PLAYER_RADIUS + SHOCKWAVE_INNER_RADIUS && x < b_x) {
			hit = true;
		}

		double norm = Vector2D.of(moveX, moveY).magnitude();
		if (norm > ANDREW_MAX_SPEED) {
			moveX *= ANDREW_MAX_SPEED / norm;
			moveY *= ANDREW_MAX_SPEED / norm;
		}
		return new SoccerPlayerAction(new double[] { moveX, moveY, (hit) ? 1 : -1 });
	}

	// TODO: get this method replaced by vector funcs
	private static double dist(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}
}