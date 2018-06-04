package org.tero.evosuite.sim;

import java.util.ArrayList;
import java.util.List;

import org.tero.evosuite.Drawable;

import javafx.scene.canvas.GraphicsContext;

public abstract class Environment implements Drawable {

	protected final List<Drawable> components = new ArrayList<Drawable>();

	protected final void drawComponents(GraphicsContext g) {
		components.forEach(obj -> obj.draw(g));
	}

	protected int frame = 0;

	public void simulate(int maxFrames) {
		while (frame < maxFrames) {
			tick();
			frame++;
		}
	}

	public abstract void tick();

}
