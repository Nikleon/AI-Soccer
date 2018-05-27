package org.tero.ai_soccer.gui;

import org.tero.ai_soccer.sim.GameState;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

public class GameViewer extends Canvas {
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;

    private static final double FPS = 60;
    private static final KeyCode SPEEDUP_KEY = KeyCode.SPACE;
    private static final double SPEEDUP_MULTIPLIER = Double.MAX_VALUE;

    private GameState game;

    private GraphicsContext g2d;
    private AnimationTimer timer;
    private long lastFrameTimeStamp;
    private boolean speedUp = false;

    public GameViewer(GameState game) {
	super(WIDTH, HEIGHT);

	this.game = game;

	this.setOnKeyPressed(evt -> {
	    if (evt.getCode() == SPEEDUP_KEY)
		speedUp = true;
	});
	this.setOnKeyReleased(evt -> {
	    if (evt.getCode() == SPEEDUP_KEY)
		speedUp = false;
	});

	g2d = this.getGraphicsContext2D();
	timer = new AnimationTimer() {
	    @Override
	    public void handle(long now) {
		if (!speedUp && (now - lastFrameTimeStamp < 1e9 / FPS))
		    return;
		if (speedUp && (now - lastFrameTimeStamp < 1e9 / (FPS * SPEEDUP_MULTIPLIER)))
		    return;
		lastFrameTimeStamp = now;
		tick();
	    }
	};

	this.setFocusTraversable(true);
    }

    public GameViewer() {
	this(null);
    }

    public void start() {
	if (game == null) {
	    System.err.println("WARNING: Game cannot start. No GameState to display.");
	    return;
	}

	lastFrameTimeStamp = System.nanoTime();
	timer.start();
    }

    public void stop() {
	timer.stop();
	fillBackground(g2d, Color.WHITE);
    }

    public void setGame(GameState game) {
	this.game = game;
    }

    private void tick() {
	if (game == null)
	    return;
	game.update();

	fillBackground(g2d, Color.WHITE);
	game.draw(g2d, WIDTH, HEIGHT);
    }

    private void fillBackground(GraphicsContext g, Color fill) {
	g.setFill(fill);
	g.fillRect(0, 0, WIDTH, HEIGHT);
    }

}
