package org.tero.ai_soccer.gui;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import not.my.code.GameState;

public class GameViewer extends Canvas {
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;

    private static final double FPS = 60;
    private static final KeyCode SPEEDUP_KEY = KeyCode.SPACE;
    private static final double SPEEDUP_MULTIPLIER = 3;

    private GameState game;

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

	timer = new AnimationTimer() {
	    @Override
	    public void handle(long now) {
		if (!speedUp && !(lastFrameTimeStamp - now > 1_000 / FPS))
		    return;
		if (speedUp && !(lastFrameTimeStamp - now > 1_000 / (FPS * SPEEDUP_MULTIPLIER)))
		    return;
		lastFrameTimeStamp = now;
		tick();
	    }
	};
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
    }

    public void setGame(GameState game) {
	this.game = game;
    }

    private void tick() {
	if (game == null)
	    return;

    }

}
