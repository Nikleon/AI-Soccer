package not.my.code;

import javafx.geometry.Point2D;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Transform;

public class Action {
    public double fx;
    public double fy;
    public boolean hit;

    public Action(double x, double y, boolean hit) {
	double dist = Math.sqrt(x * x + y * y);
	if (dist > 5) {
	    x *= 5 / dist;
	    y *= 5 / dist;
	}
	fx = x;
	fy = y;
	this.hit = hit;
    }

    public Action relativeToGlobal(Transform transform) {
	try {
	    Point2D inverseAction = transform.inverseTransform(fx, fy);
	    return new Action(inverseAction.getX(), inverseAction.getY(), hit);
	} catch (NonInvertibleTransformException e) {
	    e.printStackTrace();
	    return null;
	}
    }
}
