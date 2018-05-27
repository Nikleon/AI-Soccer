package not.my.code;

public class AndrewMethod {
	
	public static final int LENGTH = 500;
	public static final int HEIGHT = 300;
	public static final int goalHeight = 100;
	public static final double ballSlowRatio = 0.99;
	public static final double ballHitSlowRatio = 0.8;
	public static final int radius = 20;
	public static final int shockwaveRadius = 20;
	public static final double hitSlowRatio = 0.95;
	
	public static Action takeAction(double x, double y, double ballx, double bally, double ballvx, double ballvy, double a1x, double a1y, double a2x, double a2y, double e1x, double e1y, double e2x, double e2y, double e3x, double e3y){
		
		// Farthest away from the ball
		if (dist(x, y, ballx, bally) > dist(a1x, a1y, ballx, bally) && dist(x, y, ballx, bally) > dist(a2x, a2y, ballx, bally) && x < ballx) {
			return goalieAction(x,y,ballx,bally,ballvx,ballvy,a1x,a1y,a2x,a2y,e1x,e1y,e2x,e2y,e3x,e3y);
		}
		
		// Other 2
		double moveX = 0;
		double moveY = 0;
		boolean hit = false;
		
		// Check movement
		if (x > ballx) {
			moveX = -5;
		} else if (Math.abs((y - bally) / (x - ballx)) > .5) {
			moveY = bally - y;
			if (x - ballx < -100)
				moveX = ballx - x;
		} else {
			moveX = ballx - x;
			moveY = bally - y;
		}
		
		
		if (x < -400 && Math.abs(bally + ballvy / ballvx * (-GameState.LENGTH - ballx - Ball.radius)) < GameState.goalHeight && ballvx < 0) {
			if (bally > y){
				moveY = bally + 7 + ballvy / ballvx * (-GameState.LENGTH - ballx + radius) - y; // Used to return shots not directly back
			}
			else{
				moveY = bally - 7 + ballvy / ballvx * (-GameState.LENGTH - ballx + radius) - y; // Used to return shots not directly back 
			}
			moveX = 0; //-GameState.LENGTH + radius - x;
		}
		if (dist(x, y, ballx, bally) < radius + shockwaveRadius && x < ballx) {
			hit = true;
		}
		return new Action(moveX, moveY, hit);
	}
	
	public static Action goalieAction(double x, double y, double ballx, double bally, double ballvx, double ballvy, double a1x, double a1y, double a2x, double a2y, double e1x, double e1y, double e2x, double e2y, double e3x, double e3y){
		double moveX = 0;
		double moveY = 0;
		boolean hit = false;
		
		moveX = -500 + radius - x;
		moveY = bally - y;
		if (bally > GameState.goalHeight - 30) {
			moveY = GameState.goalHeight - y - 30;
		}
		if (bally < -GameState.goalHeight + 30) {
			moveY = -GameState.goalHeight - y + 30;
		}
		if (dist(x, y, ballx, bally) < radius + shockwaveRadius && x < ballx) {
			hit = true;
		}
		return new Action(moveX, moveY, hit);
	}
	
	/**************************************************************************/
	
	private static double dist(double x1, double y1, double x2, double y2){
		return Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
	}
}
