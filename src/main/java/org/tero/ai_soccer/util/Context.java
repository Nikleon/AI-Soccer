package org.tero.ai_soccer.util;

import java.util.Arrays;

import javafx.scene.transform.Transform;
import not.my.code.GameState;

/**
 * Immutable container for storing elements of the game state that are relevant
 * to the game AI.
 */
public class Context {
	public final int BALL_X = 0;
	public final int BALL_Y = 1;
	public final int BALL_VX = 2;
	public final int BALL_VY = 3;

	public final int LEFT_1_X = 4;
	public final int LEFT_1_Y = 5;
	public final int LEFT_2_X = 6;
	public final int LEFT_2_Y = 7;
	public final int LEFT_3_X = 8;
	public final int LEFT_3_Y = 9;

	public final int RIGHT_1_X = 10;
	public final int RIGHT_1_Y = 11;
	public final int RIGHT_2_X = 12;
	public final int RIGHT_2_Y = 13;
	public final int RIGHT_3_X = 14;
	public final int RIGHT_3_Y = 15;

	/**
	 * Game state data such as position of ball and players (not transformed).
	 */
	public final double[] data;

	/**
	 * Creates an immutable Context object given game state data corresponding to
	 * the above defined indices.
	 * 
	 * @param data
	 *            the ordered data
	 */
	public Context(double... data) {
		this.data = data;
	}

	/**
	 * Returns the ball position.
	 * 
	 * @return the ball position
	 */
	public Vector2D ballPos() {
		return Vector2D.of(data[BALL_X], data[BALL_Y]);
	}

	/**
	 * Returns the ball velocity
	 * 
	 * @return the ball velocity
	 */
	public Vector2D ballVel() {
		return Vector2D.of(data[BALL_VX], data[BALL_VY]);
	}

	/**
	 * Returns an array containing the left team's positions.
	 * 
	 * @return the position array
	 */
	public Vector2D[] leftTeamPos() {
		return new Vector2D[] { Vector2D.of(data[LEFT_1_X], data[LEFT_1_Y]),
				Vector2D.of(data[LEFT_2_X], data[LEFT_2_Y]), Vector2D.of(data[LEFT_3_X], data[LEFT_3_Y]) };
	}

	/**
	 * Returns an array containing the right team's positions.
	 * 
	 * @return the position array
	 */
	public Vector2D[] rightTeamPos() {
		return new Vector2D[] { Vector2D.of(data[RIGHT_1_X], data[RIGHT_1_Y]),
				Vector2D.of(data[RIGHT_2_X], data[RIGHT_2_Y]), Vector2D.of(data[RIGHT_3_X], data[RIGHT_3_Y]) };
	}

	/**
	 * Return the game state data from the perspective of a given transform.
	 * 
	 * @param transform
	 *            the transform
	 * @return the transformed data
	 */
	public double[] getRelativeContext(boolean leftTeam, int playerId, Transform transform) {
		double[] relativeData = new double[data.length];
		transform.transform2DPoints(data, 0, relativeData, 0, data.length / 2); // each player sees coordinates from
		// their side's perspective

		int[] order = { playerId, (playerId + 1) % 3, (playerId + 2) % 3 }; // each player appears in each position once
		Vector2D[][] playerPos = {
				{ Vector2D.of(relativeData[LEFT_1_X], relativeData[LEFT_1_Y]),
						Vector2D.of(relativeData[LEFT_2_X], relativeData[LEFT_2_Y]),
						Vector2D.of(relativeData[LEFT_3_X], relativeData[LEFT_3_Y]) },
				{ Vector2D.of(relativeData[RIGHT_1_X], relativeData[RIGHT_1_Y]),
						Vector2D.of(relativeData[RIGHT_2_X], relativeData[RIGHT_2_Y]),
						Vector2D.of(relativeData[RIGHT_3_X], relativeData[RIGHT_3_Y]) } };
		int allyTeamId = (leftTeam) ? 0 : 1; // each player has their own teammates first

		// double[] unbiasedData = { playerPos[allyTeamId][order[0]].x,
		// playerPos[allyTeamId][order[0]].y,
		// relativeData[BALL_X], relativeData[BALL_Y], relativeData[BALL_VX],
		// relativeData[BALL_VY],
		// playerPos[allyTeamId][order[1]].x, playerPos[allyTeamId][order[1]].y,
		// playerPos[allyTeamId][order[2]].x,
		// playerPos[allyTeamId][order[2]].y, playerPos[1 - allyTeamId][order[0]].x,
		// playerPos[1 - allyTeamId][order[0]].y, playerPos[1 - allyTeamId][order[1]].x,
		// playerPos[1 - allyTeamId][order[1]].y, playerPos[1 - allyTeamId][order[2]].x,
		// playerPos[1 - allyTeamId][order[2]].y };
		double[] unbiasedData = { playerPos[allyTeamId][order[0]].x / GameState.LENGTH,
				playerPos[allyTeamId][order[0]].y / GameState.HEIGHT, relativeData[BALL_X] / GameState.LENGTH,
				relativeData[BALL_Y] / GameState.HEIGHT, relativeData[BALL_VX] / 30.0, relativeData[BALL_VY] / 30.0,
				playerPos[allyTeamId][order[1]].x / GameState.LENGTH,
				playerPos[allyTeamId][order[1]].y / GameState.HEIGHT,
				playerPos[allyTeamId][order[2]].x / GameState.LENGTH,
				playerPos[allyTeamId][order[2]].y / GameState.HEIGHT,
				playerPos[1 - allyTeamId][order[0]].x / GameState.LENGTH,
				playerPos[1 - allyTeamId][order[0]].y / GameState.HEIGHT,
				playerPos[1 - allyTeamId][order[1]].x / GameState.LENGTH,
				playerPos[1 - allyTeamId][order[1]].y / GameState.HEIGHT,
				playerPos[1 - allyTeamId][order[2]].x / GameState.LENGTH,
				playerPos[1 - allyTeamId][order[2]].y / GameState.HEIGHT };
		return unbiasedData;
	}

	@Override
	public String toString() {
		String out = "Context: {";
		out += "\n\tBall Pos:\t" + ballPos();
		out += "\n\tBall Vel:\t" + ballVel();
		out += "\n\tLeft Team Pos:\t" + Arrays.toString(leftTeamPos());
		out += "\n\tRight Team Pos:\t" + Arrays.toString(rightTeamPos());
		out += "\n }";
		return out;
	}

}
