package org.tero.ai_soccer.gui;

import javafx.scene.layout.BorderPane;
import not.my.code.GameState;

public class Gui extends BorderPane {

    private GameViewer gameViewer;

    public Gui() {
	gameViewer = new GameViewer();
	this.setCenter(gameViewer);
    }

    public void viewGame(GameState game) {
	gameViewer.stop();
	gameViewer.setGame(game);
	gameViewer.start();
    }

}
