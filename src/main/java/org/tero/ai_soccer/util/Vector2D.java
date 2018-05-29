package org.tero.ai_soccer.util;

public class Vector2D {

    public static Vector2D of(double x, double y) {
	return new Vector2D(x, y);
    }

    public static Vector2D[] arrayOf(double... coords) {
	Vector2D[] arr = new Vector2D[coords.length / 2];
	for (int vecIx = 0; vecIx < arr.length; vecIx++)
	    arr[vecIx] = new Vector2D(coords[2 * vecIx], coords[2 * vecIx + 1]);
	return arr;
    }

    public final double x;
    public final double y;

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

    public Vector2D times(double factor) {
	return new Vector2D(factor * x, factor * y);
    }

    public double magnitude() {
	return Math.sqrt(x * x + y * y);
    }

    @Override
    public Vector2D clone() {
	return new Vector2D(x, y);
    }

    @Override
    public String toString() {
	return String.format("<%.3f, %.3f>", x, y);
    }

}
