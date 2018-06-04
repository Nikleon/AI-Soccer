package org.tero.evosuite.sim.soccer;

import java.util.Arrays;

import org.tero.ai_soccer.ai.NeuralNet;
import org.tero.evosuite.Drawable;
import org.tero.evosuite.sim.DuelEnv;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

public class SoccerGame extends DuelEnv {
	public static final Paint BORDER_FILL = Color.BLACK;
	public static final Paint FIELD_FILL = Color.GRAY;
	public static final Paint LEFT_GOAL_FILL = SoccerPlayer.LEFT_FILL;
	public static final Paint RIGHT_GOAL_FILL = SoccerPlayer.RIGHT_FILL;
	public static final double BORDER_WIDTH = 50.0;
	public static final int COUNTDOWN_FRAMES = 90;

	public static final double HALF_FIELD_LENGTH = 500.0;
	public static final double HALF_FIELD_WIDTH = 300.0;
	public static final double HALF_GOAL_WIDTH = 100.0;
	public static final double REFERENCE_WIDTH = 2 * (HALF_FIELD_LENGTH + BORDER_WIDTH);
	public static final double REFERENCE_HEIGHT = 2 * (HALF_FIELD_WIDTH + BORDER_WIDTH);

	private boolean countdownEnabled = false;
	private int countdown;

	private final int teamSize;
	private SoccerPlayer[] leftTeam;
	private SoccerPlayer[] rightTeam;
	private final SoccerGame.Ball ball = new Ball();

	public SoccerGame(NeuralNet leftAi, NeuralNet rightAi, int teamSize) {
		super(leftAi, rightAi);
		this.teamSize = teamSize;

		// init teams
		leftTeam = new SoccerPlayer[teamSize];
		rightTeam = new SoccerPlayer[teamSize];
		for (int id = 0; id < teamSize; id++) {
			leftTeam[id] = new SoccerPlayer(leftAi, true);
			rightTeam[id] = new SoccerPlayer(rightAi, false);
		}

		// register drawables
		for (int id = 0; id < teamSize; id++) {
			super.components.add(leftTeam[id]);
			super.components.add(rightTeam[id]);
		}
		super.components.add(ball);

		reset();
	}

	@Override
	public void tick() {
		// TODO
	}

	private void reset() {
		// randomize ball spawn
		ball.pos = new Point2D(3 * (Math.random() - 0.5), 3 * (Math.random() - 0.5));

		// generate spawn points
		Point2D[] spawnPoints = generateSpawnPoints();
		for (int id = 0; id < teamSize; id++) {
			leftTeam[id].pos = spawnPoints[id];
			rightTeam[id].pos = spawnPoints[id].multiply(-1);
		}

		// initialize time counts
		countdown = COUNTDOWN_FRAMES;
	}

	private Point2D[] generateSpawnPoints() {
		Point2D[] spawnPos = new Point2D[teamSize];
		for (int id = 0; id < teamSize; id++) {
			// choose random spawn point in left half
			spawnPos[id] = new Point2D(-Math.random() * HALF_FIELD_LENGTH,
					2 * (Math.random() - 0.5) * HALF_FIELD_WIDTH);

			// scale spawn region away from walls
			spawnPos[id] = spawnPos[id].multiply(0.9);

			// check if spawn is too close to the ball
			if (spawnPos[id].magnitude() < 0.1) {
				id--;
				continue;
			}

			// confirm no collision with other teammate spawn
			for (int prevId = id - 1; prevId >= 0; prevId--) {
				if (spawnPos[id].distance(spawnPos[prevId]) < 2 * SoccerPlayer.RADIUS) {
					id--;
					continue;
				}
			}
		}
		return spawnPos;
	}

	@Override
	public void draw(GraphicsContext g) {
		countdownEnabled = true;

		double w = REFERENCE_WIDTH;
		double h = REFERENCE_HEIGHT;

		// draw walls
		g.setFill(BORDER_FILL);
		g.fillRect(-w / 2, -h / 2, w, h);

		// draw goals
		g.setFill(LEFT_GOAL_FILL);
		g.fillRect(-w / 2, -HALF_GOAL_WIDTH, BORDER_WIDTH, 2 * HALF_GOAL_WIDTH);
		g.setFill(RIGHT_GOAL_FILL);
		g.fillRect(HALF_FIELD_LENGTH, -HALF_GOAL_WIDTH, BORDER_WIDTH, 2 * HALF_GOAL_WIDTH);

		// draw field
		g.setFill(FIELD_FILL);
		g.fillRect(-HALF_FIELD_LENGTH, -HALF_FIELD_WIDTH, 2 * HALF_FIELD_LENGTH, 2 * HALF_FIELD_WIDTH);

		// draw countdown if applicable
		if (countdownEnabled && countdown > 0) {
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

		// draw scores
		g.setFill(Color.WHITE);
		g.setFont(Font.font("timesRoman", 70));
		double lScore = Arrays.stream(leftTeam).mapToDouble(SoccerPlayer::getScore).sum();
		double rScore = Arrays.stream(rightTeam).mapToDouble(SoccerPlayer::getScore).sum();
		g.fillText(String.format("%.2f", lScore), -w / 2 + 50, h / 2 - 20);
		g.fillText(String.format("%.2f", rScore), w / 2 - 250, h / 2 - 20);

		// draw game objects
		super.drawComponents(g);
	}

	// public class Context {
	// public final Map<String, Double> gameState = new HashMap<String, Double>(16);
	//
	// static final int BALL_POS_OFFSET = 0;
	// static final int BALL_VEL_OFFSET = 2;
	// static final int LEFT_POS_OFFSET = 4;
	// static final int RIGHT_POS_OFFSET = LEFT_POS_OFFSET + 2 * teamSize;
	//
	// SoccerContext(Vector2D[] spawnPoints, double... data) {
	// super(new double[data.length + 2 * TEAM_SIZE]);
	// System.arraycopy(data, 0, this.data, 0, data.length);
	//
	// for (int id = 0; id < TEAM_SIZE; id++) {
	// this.data[LEFT_POS_OFFSET + 2 * id] = spawnPoints[id].x;
	// this.data[LEFT_POS_OFFSET + 2 * id + 1] = spawnPoints[id].y;
	// this.data[RIGHT_POS_OFFSET + 2 * id] = -spawnPoints[id].x;
	// this.data[RIGHT_POS_OFFSET + 2 * id + 1] = -spawnPoints[id].y;
	// }
	// }
	//
	// double[] getRelativeData(Transform transform) {
	// double[] data = this.data.clone();
	// transform.transform2DPoints(data, 0, data, 0, data.length / 2);
	// return data;
	// }
	//
	// double[] getRelativeData(Transform transform, boolean leftTeam, int id) {
	// double[] relativeData = getRelativeData(transform);
	// double[] unbiasedData = new double[relativeData.length];
	//
	// // determine input ordering
	// int[] idOrder = new int[teamSize];
	// for (int i = 0; i < teamSize; i++)
	// idOrder[i] = (id + i) % teamSize;
	// int allyOffset = (leftTeam) ? LEFT_POS_OFFSET : RIGHT_POS_OFFSET;
	// int enemyOffset = (leftTeam) ? RIGHT_POS_OFFSET : LEFT_POS_OFFSET;
	// int index = 0;
	//
	// // fill data array
	// unbiasedData[index++] = relativeData[allyOffset + 2 * idOrder[0]];
	// unbiasedData[index++] = relativeData[allyOffset + 2 * idOrder[0] + 1];
	//
	// unbiasedData[index++] = relativeData[BALL_POS_OFFSET];
	// unbiasedData[index++] = relativeData[BALL_POS_OFFSET + 1];
	// unbiasedData[index++] = relativeData[BALL_VEL_OFFSET];
	// unbiasedData[index++] = relativeData[BALL_VEL_OFFSET + 1];
	//
	// for (int i = 1; i < teamSize; i++) {
	// unbiasedData[index++] = relativeData[allyOffset + 2 * idOrder[i]];
	// unbiasedData[index++] = relativeData[allyOffset + 2 * idOrder[i] + 1];
	// }
	//
	// for (int i = 0; i < teamSize; i++) {
	// unbiasedData[index++] = relativeData[enemyOffset + 2 * idOrder[i]];
	// unbiasedData[index++] = relativeData[enemyOffset + 2 * idOrder[i] + 1];
	// }
	//
	// return unbiasedData;
	// }
	//
	// Vector2D ballPos() {
	// return new Vector2D(data[BALL_POS_OFFSET], data[BALL_POS_OFFSET + 1]);
	// }
	//
	// void setBallPos(Vector2D pos) {
	// data[BALL_POS_OFFSET] = pos.x;
	// data[BALL_POS_OFFSET + 1] = pos.y;
	// }
	//
	// Vector2D ballVel() {
	// return new Vector2D(data[BALL_VEL_OFFSET], data[BALL_VEL_OFFSET + 1]);
	// }
	//
	// void setBallVel(Vector2D vel) {
	// data[BALL_VEL_OFFSET] = vel.x;
	// data[BALL_VEL_OFFSET + 1] = vel.y;
	// }
	//
	// Vector2D playerPos(boolean leftTeam, int id) {
	// int teamOffset = (leftTeam) ? LEFT_POS_OFFSET : RIGHT_POS_OFFSET;
	// return new Vector2D(data[teamOffset + 2 * id], data[teamOffset + 2 * id +
	// 1]);
	// }
	// }

	public static class Ball implements Drawable {
		public static final Paint FILL = Color.WHITE;
		public static final Paint OUTLINE = Color.BLACK;

		public static final double RADIUS = 15.0;

		public Point2D pos = Point2D.ZERO;
		public Point2D vel = Point2D.ZERO;
		public int lastHit = 0; // -1: left, 1: right

		@Override
		public void draw(GraphicsContext g) {
			g.setFill(Ball.FILL);
			g.fillOval(pos.getX() - Ball.RADIUS, pos.getY() - Ball.RADIUS, 2 * Ball.RADIUS, 2 * Ball.RADIUS);

			g.setStroke(Ball.OUTLINE);
			g.strokeOval(pos.getX() - Ball.RADIUS, pos.getY() - Ball.RADIUS, 2 * Ball.RADIUS, 2 * Ball.RADIUS);
		}

	}

}
