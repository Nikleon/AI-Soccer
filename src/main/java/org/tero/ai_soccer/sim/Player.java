package org.tero.ai_soccer.sim;

import org.tero.ai_soccer.util.Context;
import org.tero.ai_soccer.util.Vector2D;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import not.my.code.Action;
import not.my.code.AndrewMethod;
import not.my.code.GameState;

public class Player /* extends not.my.code.Player */ {
	protected static final Transform LEFT = Transform.rotate(0, 0, 0);
	protected static final Transform RIGHT = Transform.rotate(180, 0, 0);

	protected static final double RADIUS = 20;
	protected static final double SHOCKWAVE_RADIUS = 20;
	protected static final double HIT_SLOW_RATIO = 0.95;

	/**
	 * Position vector -> (0,0) is center.
	 */
	protected Vector2D pos;

	/**
	 * Velocity vector (in pixels per frame).
	 */
	protected Vector2D vel;

	/**
	 * Indicated whether coordinates need to be flipped. If false, then flip.
	 */
	protected boolean leftTeam;

	/**
	 * Indicates uniform number of player to distinguish from team members.
	 */
	protected int number;

	/**
	 * Transform to player's relative coordinate system.
	 */
	protected Transform transform;

	/**
	 * Frames left of ongoing shockwave animation, 0 if not ongoing.
	 */
	protected int shockwaveAnimationFramesLeft;

	public Player(Vector2D pos, boolean flipped, int playerId) {
		this.pos = pos.clone();
		vel = new Vector2D(0, 0);

		leftTeam = !flipped;
		number = playerId;
		transform = (leftTeam) ? LEFT : RIGHT;

		shockwaveAnimationFramesLeft = 0;
	}

	/**
	 * Returns this player's preferred next action given a set of data to base its
	 * decision on. Uses AndrewMethod AI. Override this method to add functionality.
	 * 
	 * @param ctx
	 *            the context, i.e. game object positions, etc...
	 * @return the preferred next {@link Action}
	 */
	public Action takeAction(Context ctx) {
		double[] c = ctx.getRelativeContext(leftTeam, number, transform);
		Action andrewAiAction = AndrewMethod.takeAction(c[0], c[1], c[2], c[3], c[4], c[5], c[6], c[7], c[8], c[9],
				c[10], c[11], c[12], c[13], c[14], c[15]);
		return andrewAiAction.relativeToGlobal(transform);
	}

	public void drawPlayer(GraphicsContext g) {
		if (leftTeam)
			g.setFill(Color.ORANGE);
		else
			g.setFill(Color.BLUE);

		g.fillOval(pos.x - RADIUS, pos.y - RADIUS, 2 * RADIUS, 2 * RADIUS);
		g.setStroke(Color.BLACK);
		g.strokeOval(pos.x - RADIUS, pos.y - RADIUS, 2 * RADIUS, 2 * RADIUS);
	}

	public void drawShockwave(GraphicsContext g) {
		if (shockwaveAnimationFramesLeft > 0) {
			g.setFill(Color.WHITE);
			g.fillOval(pos.x - RADIUS - shockwaveAnimationFramesLeft * (SHOCKWAVE_RADIUS) / 5,
					pos.y - RADIUS - shockwaveAnimationFramesLeft * (SHOCKWAVE_RADIUS) / 5,
					2 * RADIUS + shockwaveAnimationFramesLeft * (SHOCKWAVE_RADIUS) / 5 * 2,
					2 * RADIUS + shockwaveAnimationFramesLeft * (SHOCKWAVE_RADIUS) / 5 * 2);
		}
	}

	public void update(Action globalAction) {
		double speed = vel.magnitude();
		if (speed > 40) {
			vel = vel.times(40 / speed);
		}

		// wtf
		double x = pos.x;
		double y = pos.y;
		double xv = vel.x;
		double yv = vel.y;

		double vx = globalAction.fx + xv;
		double vy = globalAction.fy + yv;

		double tempx = vx;
		double tempy = vy;
		boolean clear = false;
		while (!clear) {
			clear = true;
			if (Math.abs(y + tempy) + RADIUS > GameState.HEIGHT) {
				clear = false;
				vy *= -.8;
				if (y > 0) {
					tempy = -.8 * (y + tempy + RADIUS - GameState.HEIGHT);
					y = GameState.HEIGHT - RADIUS;
				} else {
					tempy = -.8 * (y + tempy - RADIUS + GameState.HEIGHT);
					y = -GameState.HEIGHT + RADIUS;
				}
			}
			if (Math.abs(x + tempx) + RADIUS > GameState.LENGTH) {
				clear = false;
				vx *= -.8;
				if (x > 0) {
					tempx = -.8 * (x + tempx + RADIUS - GameState.LENGTH);
					x = GameState.LENGTH - RADIUS;
				} else {
					tempx = -.8 * (x + tempx - RADIUS + GameState.LENGTH);
					x = -GameState.LENGTH + RADIUS;
				}
			}
		}
		x += tempx;
		y += tempy;
		xv *= HIT_SLOW_RATIO;
		yv *= HIT_SLOW_RATIO;
		if (Math.abs(xv) < .5)
			xv = 0;
		if (Math.abs(yv) < .5)
			xv = 0;
		if (globalAction.hit && shockwaveAnimationFramesLeft == 0)
			shockwaveAnimationFramesLeft++;
		if (shockwaveAnimationFramesLeft != 0)
			shockwaveAnimationFramesLeft++;
		if (shockwaveAnimationFramesLeft == 6)
			shockwaveAnimationFramesLeft = -15;

		// un-wtf
		pos = Vector2D.of(x, y);
		vel = Vector2D.of(xv, yv);
	}

}
