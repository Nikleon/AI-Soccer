package org.tero.ai_soccer;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.tero.ai_soccer.ai.GeneticNeuralNet;
import org.tero.ai_soccer.ai.NeuralNet;
import org.tero.ai_soccer.gui.Gui;
import org.tero.ai_soccer.sim.GameState;
import org.tero.ai_soccer.util.SaveUtil;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AISoccer extends Application {
	private static final String TITLE = "AI Soccer (Neuro-evolution implementation)";

	private static final boolean VIEW = true;
	private static final String VIEW_FILE_1 = "shallow0";
	private static final String VIEW_FILE_2 = "shallow1";

	private static boolean vs_Andrew = true;
	public static final double ANDREW_MAX_SPEED = 3;
	private static final Class<GeneticNeuralNet> AI_TYPE = GeneticNeuralNet.class;
	private static final String SAVE_PREFIX = "shallow";

	private static final int CYCLE = 100_000_000;

	private static final int POPULATION_SIZE = 50;
	private static final int ELITES = 5;
	private static final double MUTATION_RATE = 5.0 / NeuralNet.NUM_WEIGHTS;

	private static final int MAX_GEN = 100_000;
	private static final int FRAMES_PER_BACKUP = 20;
	private static final int FRAMES_PER_MATCH = 20_000;

	private Gui gui;

	@Override
	public void init() {
		gui = new Gui();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		if (VIEW) {
			primaryStage.setTitle(TITLE);
			primaryStage.setScene(new Scene(gui));
			primaryStage.sizeToScene();
			primaryStage.centerOnScreen();
			primaryStage.show();
			primaryStage.requestFocus();

			NeuralNet ai1 = SaveUtil.load(AI_TYPE, VIEW_FILE_1);
			NeuralNet ai2 = (vs_Andrew) ? null : SaveUtil.load(AI_TYPE, VIEW_FILE_2);

			System.out.format("Showing game of %s vs %s...%n", VIEW_FILE_1, (vs_Andrew) ? "AndrewBot" : VIEW_FILE_2);
			gui.viewGame(new GameState(ai1, ai2));
		} else {
			List<GeneticNeuralNet> population = initPopulation();

			for (int gen = 0; gen < MAX_GEN; gen++) {
				System.out.format("Starting generation %d...%n", gen);

				// cycle
				if (gen % CYCLE == 0)
					vs_Andrew = !vs_Andrew;

				// decide match pairings
				Collections.shuffle(population);
				List<GameState> matches = new ArrayList<>(POPULATION_SIZE);
				if (vs_Andrew)
					for (int i = 0; i < POPULATION_SIZE; i++)
						matches.add(new GameState(population.get(i), null));
				else
					for (int i = 0; i < POPULATION_SIZE; i += 2)
						matches.add(new GameState(population.get(i), population.get(i + 1)));

				// play matches
				matches.parallelStream().forEach(match -> {
					for (int frame = 0; frame < FRAMES_PER_MATCH; frame++)
						match.update();
				});

				// record scores
				if (vs_Andrew)
					for (int i = 0; i < POPULATION_SIZE; i++) {
						GameState match = matches.get(i);
						population.get(i).cacheScore(
								(double) match.getLeftScore() / (match.getLeftScore() + match.getRightScore()));
					}
				else {
					for (int i = 0; i < POPULATION_SIZE; i += 2) {
						GameState match = matches.get(i / 2);
						population.get(i).cacheScore(Math.pow(match.getLeftScore(), 2)
								+ Math.max(match.getLeftScore() - match.getRightScore(), 0));
						population.get(i + 1).cacheScore(Math.pow(match.getRightScore(), 2)
								+ Math.max(match.getRightScore() - match.getLeftScore(), 0));
					}
				}

				List<GeneticNeuralNet> nextPopulation = new ArrayList<>(POPULATION_SIZE);

				// keep elites
				Collections.sort(population);
				for (int i = 0; i < ELITES; i++)
					nextPopulation.add(population.get(i).clone());

				// roulette survival
				double totalScore = population.stream().mapToDouble(individual -> individual.getScoreCache()).sum();
				for (int i = 0; i < POPULATION_SIZE - ELITES; i++) {
					double random = Math.random() * totalScore;
					int index = -1;
					do {
						random -= population.get(++index).getScoreCache();
					} while (random > 0);

					GeneticNeuralNet offspring = population.get(i).clone();
					offspring.mutate(MUTATION_RATE);
					nextPopulation.add(offspring);
				}

				// print stats
				if (vs_Andrew) {
					double minScore = Collections.max(population).getScoreCache();
					double avgScore = (double) totalScore / POPULATION_SIZE;
					double maxScore = Collections.min(population).getScoreCache();
					System.out.format("Min: %.2f, Max: %.2f, Avg: %.2f%n%n", minScore, maxScore, avgScore);

					// TODO: temp (plot running avg)
					if (gen % 10 == 0) {
						FileWriter out = new FileWriter(new File("stats/avg.txt"), true);
						out.append(avgScore + "\n");
						out.close();
					}
				} else {
					double minScore = Collections.max(population).getScoreCache();
					double maxScore = Collections.min(population).getScoreCache();
					System.out.format("Min: %.2f, Max: %.2f%n%n", minScore, maxScore);

					// TODO: temp (plot running avg)
					if (gen % 10 == 0) {
						FileWriter out = new FileWriter(new File("stats/max  .txt"), true);
						out.append(maxScore + "\n");
						out.close();
					}
				}

				// save a backup
				if (gen % FRAMES_PER_BACKUP == 0) {
					System.out.println("Saving...\n");
					for (int i = 0; i < POPULATION_SIZE; i++)
						SaveUtil.save(population.get(i), SAVE_PREFIX + i);
				}

				population = nextPopulation;
			}
		}
	}

	private static List<GeneticNeuralNet> initPopulation() {
		List<GeneticNeuralNet> population = new ArrayList<>(POPULATION_SIZE);
		for (int i = 0; i < POPULATION_SIZE; i++)
			population.add(SaveUtil.load(AI_TYPE, SAVE_PREFIX + i));
		return population;
	}

	public static void main(String[] args) {
		Application.launch(args);
	}

	public static void exit() {
		// TODO: save progress and quit (probably got here because something broke)
		System.exit(1);
	}

}
