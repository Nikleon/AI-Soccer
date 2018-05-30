package org.tero.ai_soccer.sim.env;

import java.util.Arrays;

import org.tero.ai_soccer.ai.NeuralNet;
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
	private static final int TEAM_SIZE = 3;

	private static final int COUNTDOWN_FRAMES = 90;
	private int countdown;

	private int lastHit = 0; // -1: left, 1: right

	private NeuralNet leftAi;
	private NeuralNet rightAi;

	private SoccerContext context;

	private SoccerPlayer[] leftTeam = new SoccerPlayer[TEAM_SIZE];
	private SoccerPlayer[] rightTeam = new SoccerPlayer[TEAM_SIZE];

	public Soccer(NeuralNet ai1, NeuralNet ai2) {
		leftAi = ai1;
		rightAi = ai2;

		for (int id = 0; id < TEAM_SIZE; id++) {
			leftTeam[id] = new SoccerPlayer(leftAi, true);
			rightTeam[id] = new SoccerPlayer(rightAi, false);
		}

		reset();
	}

	@Override
	public void update() {
		for (int id = 0; id < TEAM_SIZE; id++) {
			leftTeam[id].getNextAction().apply();
			rightTeam[id].getNextAction().apply();
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

		// initialize countdown
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
		if (DRAW_TRAIL && lastHit != 0) {
			if (lastHit == -1)
				g.setStroke(LEFT_FILL);
			else if (lastHit == 1)
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

		Vector2D ballPos() {
			return new Vector2D(data[BALL_POS_OFFSET], data[BALL_POS_OFFSET + 1]);
		}

		Vector2D ballVel() {
			return new Vector2D(data[BALL_VEL_OFFSET], data[BALL_VEL_OFFSET + 1]);
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
			// TODO: this is basically the update method
		}
	}

	private class SoccerPlayer extends Soccer.Entity {
		Transform transform;
		double shockwaveWtf; // TODO: this shouldnt have to be here. also wtf does this represent?

		SoccerPlayer(NeuralNet controller, boolean leftTeam) {
			super(controller);
			transform = (leftTeam) ? new Affine() : Transform.rotate(180, 0, 0);
			shockwaveWtf = 0;
		}

		@Override
		SoccerPlayerAction getNextAction() {
			// get relative inputs
			double[] relativeData = context.getRelativeData(this.transform);

			SoccerPlayerAction relativeAction = null;
			if (controller != null) {
				// decide on action
				relativeAction = new SoccerPlayerAction(this.controller.predict(relativeData));
			} else {
				// use default bot
				// TODO: AndrewMethod
			}

			// transform relative action to global action
			return relativeAction.toGlobal(this.transform);
		}

		@Override
		double getScore() {
			return scoreMap.get("goals");
		}
	}
}