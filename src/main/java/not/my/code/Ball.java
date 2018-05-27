package not.my.code;
import java.awt.Color;
import java.awt.Graphics;

public class Ball {
	public static final int radius = 15;
	public double x,y;
	public double vx, vy;
	public Ball(){
		reset();
	}
	public void reset(){
		x = Math.random()*10-5;
		y = Math.random()*10-5;
		vx = 0;
		vy = 0;
	}
	public void draw(Graphics g){
		g.setColor(Color.white);
		g.fillOval((int)(x-radius), (int)(y-radius), radius*2, radius*2);
		g.setColor(Color.black);
		g.drawOval((int)(x-radius), (int)(y-radius), radius*2, radius*2);
	}
}
