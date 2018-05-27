package not.my.code;
import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
public class Player {
	final static int radius = 20;
	final static int shockwaveRadius = 20;
	final static double hitSlowRatio = 0.95;
	
	public double x,y;
	public double xv, yv;
	public boolean t;
	public int shockwaveFrame;
	public double[] weights;
	
	public Action takeAction(double ballx, double bally, double ballvx, double ballvy, double a1x, double a1y, double a2x, double a2y, double e1x, double e1y, double e2x, double e2y, double e3x, double e3y){
		if (weights != null){
			double[] startNodes = new double[Constants.INPUTS];
			startNodes[0] = (x - GameState.LENGTH/2)/(GameState.LENGTH/2);
			startNodes[1] = (y - GameState.HEIGHT/2)/(GameState.HEIGHT/2);
			startNodes[2] = (ballx - GameState.LENGTH/2)/(GameState.LENGTH/2);
			startNodes[3] = (bally - GameState.HEIGHT/2)/(GameState.HEIGHT/2);
			startNodes[4] = (ballvx)/(5);
			startNodes[5] = (ballvy)/(5);
			startNodes[6] = (a1x - GameState.LENGTH/2)/(GameState.LENGTH/2);
			startNodes[7] = (a1y - GameState.HEIGHT/2)/(GameState.HEIGHT/2);
			startNodes[8] = (a2x - GameState.LENGTH/2)/(GameState.LENGTH/2);
			startNodes[9] = (a2y - GameState.HEIGHT/2)/(GameState.HEIGHT/2);
			startNodes[10] = (e1x - GameState.LENGTH/2)/(GameState.LENGTH/2);
			startNodes[11] = (e1y - GameState.HEIGHT/2)/(GameState.HEIGHT/2);
			startNodes[12] = (e2x - GameState.LENGTH/2)/(GameState.LENGTH/2);
			startNodes[13] = (e2y - GameState.HEIGHT/2)/(GameState.HEIGHT/2);
			startNodes[14] = (e3x - GameState.LENGTH/2)/(GameState.LENGTH/2);
			startNodes[15] = (e3y - GameState.HEIGHT/2)/(GameState.HEIGHT/2);

			if (t){
				startNodes[0] *= -1;
				startNodes[2]*= -1;
				startNodes[4] *= -1;
				startNodes[6] *= -1;
				startNodes[8] *= -1;
				startNodes[10] *= -1;
				startNodes[12] *= -1;
				startNodes[14] *= -1;
			}
			return Constants.takeAction(weights, startNodes);
		}
		if (t){
			Action action = AndrewMethod.takeAction(-x,y,-ballx,bally,-ballvx,ballvy,-a1x, a1y,-a2x,a2y,-e1x,e1y, -e2x, e2y, -e3x, e3y);
			return new Action(-action.fx,action.fy,action.hit);
		}
		else{
			return AndrewMethod.takeAction(x,y,ballx,bally,ballvx,ballvy,a1x,a1y,a2x,a2y,e1x,e1y,e2x,e2y,e3x,e3y);
		}
	}
	
	public Player(int x, int y, boolean team){
		this.xv = 0;
		this.yv = 0;
		this.x = x;
		this.y = y;
		t = team;
		shockwaveFrame = 0;
	}
	
	public Player(int x, int y, boolean team, int instruct){
		this.xv = 0;
		this.yv = 0;
		this.x = x;
		this.y = y;
		t = team;
		shockwaveFrame = 0;
		if (instruct < 0){
			return;
		}
		weights = new double[Constants.WEIGHTS];
		String file;
		file = "Weights" + String.format("%03d", instruct) + ".txt";
		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			for (int i = 0; i < weights.length; i++) {
				String s = bufferedReader.readLine();
				weights[i] = Double.parseDouble(s);
			}
			bufferedReader.close();

		} catch (IOException e) {
			System.out.println("Unable to open file '" + file + "'.");
		}
	}
	
	public void update(Action a){
		double speed = dist(0,0,xv,yv);
		if (speed > 40){
			xv *= 40/speed;
			yv *= 40/speed;
		}
		double vx = a.fx + xv;
		double vy = a.fy + yv;
		
		double tempx = vx;
		double tempy = vy;
		boolean clear = false;
		while (!clear){
			clear = true;
			boolean hitY = false;
			if (Math.abs(y + tempy)+radius > GameState.HEIGHT){
				clear = false;
				vy*= -.8;
				if (y > 0){
					tempy = -.8*(y + tempy + radius - GameState.HEIGHT);
					y = GameState.HEIGHT-radius;
				}
				else{
					tempy = -.8*(y + tempy - radius + GameState.HEIGHT);
					y = -GameState.HEIGHT+radius;
				}
				hitY = true;
			}
			if (Math.abs(x + tempx)+radius > GameState.LENGTH){
				clear = false;
				vx*= -.8;
				if (x > 0){
					tempx = -.8*(x + tempx + radius - GameState.LENGTH);
					x = GameState.LENGTH-radius;
				}
				else{
					tempx = -.8*(x + tempx - radius + GameState.LENGTH);
					x = -GameState.LENGTH+radius;
				}
			}
		}
		x += tempx;
		y += tempy;
		xv *= hitSlowRatio;
		yv *= hitSlowRatio;
		if (Math.abs(xv) < .5) xv = 0;
		if (Math.abs(yv) < .5) xv = 0;
		if (a.hit && shockwaveFrame == 0) shockwaveFrame++;
		if (shockwaveFrame != 0) shockwaveFrame++;
		if (shockwaveFrame == 6) shockwaveFrame = -15;
	}
	
	public void drawPlayer(Graphics g){
		if (t) g.setColor(Color.blue);
		else g.setColor(Color.orange);
		
		g.fillOval((int)(x-radius), (int)(y-radius), (int)(2*radius), (int)(2*radius));
		g.setColor(Color.black);
		g.drawOval((int)(x-radius), (int)(y-radius), (int)(2*radius), (int)(2*radius));
	}
	public void drawShockwave(Graphics g){
		if (shockwaveFrame  > 0){
			g.setColor(Color.white);
			g.fillOval((int)(x - radius - shockwaveFrame*(shockwaveRadius)/5), (int)(y - radius - shockwaveFrame*(shockwaveRadius)/5), (int)(2*radius + shockwaveFrame*(shockwaveRadius)/5*2), (int)(2*radius + shockwaveFrame*(shockwaveRadius)/5*2));
		}
	}
	
	private double dist(double x1, double y1, double x2, double y2){
		return Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
	}
	
}
