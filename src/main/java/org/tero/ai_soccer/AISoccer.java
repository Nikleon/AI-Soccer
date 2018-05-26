package org.tero.ai_soccer;

import org.tero.ai_soccer.gui.Gui;
import org.tero.ai_soccer.sim.GameState;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AISoccer extends Application {

    private static final String TITLE = "AI Soccer (Neuro-evolution implementation)";

    private Gui gui;

    @Override
    public void init() {
	gui = new Gui();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
	primaryStage.setTitle(TITLE);
	primaryStage.setScene(new Scene(gui));
	primaryStage.sizeToScene();
	primaryStage.centerOnScreen();
	primaryStage.show();

	gui.viewGame(new GameState());
    }

    public static void main(String[] args) {
	Application.launch(args);
    }

}
