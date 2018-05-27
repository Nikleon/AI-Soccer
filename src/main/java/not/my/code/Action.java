package not.my.code;

public class Action {
	public double fx;
	public double fy;
	public boolean hit;
	
	public Action(double x, double y, boolean hit){
		double dist = Math.sqrt(x*x+y*y);
		if (dist > 5){
			x *= 5/dist;
			y *= 5/dist;
		}
		fx = x;
		fy = y;
		this.hit = hit;
	}
}
