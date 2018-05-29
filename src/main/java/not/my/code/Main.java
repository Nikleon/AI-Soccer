package not.my.code;

import org.tero.ai_soccer.ai.GeneticNeuralNet;
import org.tero.ai_soccer.util.SaveUtil;

public class Main {
    public static void main(String[] args) {
	// CreateWeights.main(args);

	int generations = 1000000;
	for (int i = 0; i < generations; i++) {
	    // System.out.println("Starting Generation " + (i + 1) + ".");
	    double[] scores = new double[Constants.POPULATION_SIZE];
	    playGames(scores);
	    reproduce(scores);
	}

    }

    public static void playGames(double[] scores) {
	Thread[] threads = new PlayGamesThread[Constants.THREADS];

	for (int i = 0; i < Constants.THREADS; i++) {
	    threads[i] = new PlayGamesThread(i, scores);
	}
	for (int i = 0; i < Constants.THREADS; i++) {
	    try {
		threads[i].join();
	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
    }

    public static void reproduce(double[] scores) {
	GeneticNeuralNet[] brainzzes = new GeneticNeuralNet[Constants.POPULATION_SIZE];

	for (int i = 0; i < Constants.POPULATION_SIZE; i++) {
	    String fileName = "Weights" + String.format("%03d", i) + ".txt";
	    brainzzes[i] = SaveUtil.load(GeneticNeuralNet.class, fileName);
	}

	double minScore = 1;
	double maxScore = -1;
	int maxIndex = 0;
	for (int i = 0; i < Constants.POPULATION_SIZE; i++) {
	    minScore = Math.min(minScore, scores[i]);

	    if (scores[i] > maxScore) {
		maxScore = scores[i];
		maxIndex = i;
	    }
	}
	minScore -= 0.01;
	// System.out.println("Minimum score: " + minScore);
	double totalScore = 0;
	double sumForAvg = 0;
	for (int i = 0; i < Constants.POPULATION_SIZE; i++) {
	    sumForAvg += scores[i];
	    scores[i] -= minScore;
	    scores[i] *= scores[i];
	    totalScore += scores[i];

	    // System.out.println("Scores:" + i + " " + scores[i]);
	}

	// System.out.println("AverageScore: " + (sumForAvg / Constants.POPULATION_SIZE
	// + minScore));
	// System.out.println("Maximum Score: " + maxScore);
	// System.out.format("%.2f\t%.2f\t%.2f\n", minScore, (sumForAvg /
	// Constants.POPULATION_SIZE + minScore), maxScore);
	System.out.format("%.2f\n", (sumForAvg / Constants.POPULATION_SIZE));

	for (int i = 0; i < Constants.POPULATION_SIZE; i++) {
	    GeneticNeuralNet newBrain = brainzzes[maxIndex].clone();
	    if (i % 2 == 0)
		newBrain.mutate(Constants.MUTATION_RATE);
	    SaveUtil.save(newBrain, "Weights" + String.format("%03d", i) + ".txt");
	}

	// for (int i = 0; i < Constants.POPULATION_SIZE - 50; i++) {
	// double random = Math.random() * totalScore;
	// int index = -1;
	// while (random >= 0) {
	// random -= scores[++index];
	// }
	//
	// brainzzes[index].mutate(Constants.MUTATION_RATE);
	// SaveUtil.save(brainzzes[index], "Weights" + String.format("%03d", i) +
	// ".txt");
	// }
    }
}
