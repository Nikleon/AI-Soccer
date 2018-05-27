package org.tero.ai_soccer.util;

public class Vector2D {

    private double x, y;

    public Vector2D(double x, double y) {
	this.x = x;
	this.y = y;
    }

    public double getX() {
	return x;
    }

    public double getY() {
	return y;
    }

    public Vector2D plus(Vector2D vec) {
	return new Vector2D(this.x + vec.x, this.y + vec.y);
    }

    public void add(Vector2D vec) {
	x += vec.getX();
	y += vec.getY();
    }

    public Vector2D times(double factor) {
	return new Vector2D(factor * x, factor * y);
    }

    public void scale(double factor) {
	x *= factor;
	y *= factor;
    }

    public double magnitude() {
	return Math.sqrt(x * x + y * y);
    }

    @Override
    public Vector2D clone() {
	return new Vector2D(x, y);
    }

}
