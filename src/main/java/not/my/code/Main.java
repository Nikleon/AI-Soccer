package not.my.code;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
	public static void main(String[] args){
		//CreateWeights.main(args);
		
		int generations = 1000000;
		for (int i = 0; i < generations; i++){
			System.out.println("Starting Generation " + (i+1) + ".");
			double[] scores = new double[Constants.POPULATION_SIZE];
			playGames(scores);
			reproduce(scores);
		}
		
	}
	
	public static void playGames(double[] scores){
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
	
	public static void reproduce(double[] scores){
		double[][] weights = new double[Constants.POPULATION_SIZE][Constants.WEIGHTS];
		
		for (int i = 0; i < Constants.POPULATION_SIZE; i++){
			String file;
			file = "Weights" + String.format("%03d", i) + ".txt";
			try {
				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				for (int j = 0; j < Constants.WEIGHTS; j++) {
					String s = bufferedReader.readLine();
					weights[i][j] = Double.parseDouble(s);
				}
				bufferedReader.close();

			} catch (IOException e) {
				System.out.println("Unable to open file '" + file + "'.");
			}
		}
		
		double minScore = 1;
		double maxScore = -1;
		for (int i = 0; i < Constants.POPULATION_SIZE; i++){
			minScore = Math.min(minScore, scores[i]);
			maxScore = Math.max(maxScore, scores[i]);
		}
		minScore -= 0.01;
		System.out.println("Minimum score: " + minScore);
		double totalScore = 0;
		double sumForAvg = 0;
		for (int i = 0; i < Constants.POPULATION_SIZE; i++){
			sumForAvg += scores[i];
			scores[i] -= minScore;
			scores[i] *= scores[i];
			totalScore += scores[i];
			//System.out.println("Scores:" + i + " " + scores[i]);
		}
		
		System.out.println("AverageScore: " + (sumForAvg/Constants.POPULATION_SIZE + minScore));
		System.out.println("Maximum Score: " + maxScore);
		
		for (int i = 0; i < Constants.POPULATION_SIZE; i++){
			try{
				double random = Math.random() * totalScore;
				int index = -1;
				while (random >= 0){
					random -= scores[++index];
				}
				double random2 = Math.random() * totalScore;
				int index2 = -1;
				while (random2 >= 0){
					random2 -= scores[++index2];
				}
				FileWriter fileWriter = new FileWriter("Weights" + String.format("%03d", i) + ".txt");
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				for (int j = 0; j < Constants.WEIGHTS; j++){ //First "3" is the number of layers of hidden nodes
					double weight = weights[index][j];
					boolean use1 = true;
					if (Math.random() < Constants.CROSSOVER_RATE){
						use1 = !use1;
					}
					if (!use1){
						weight = weights[index2][j];
					}
					double randomMutation = Math.random();
					if (randomMutation < Constants.MUTATION_RATE){
						weight = weight + Math.random() * 0.2 - 0.1; //Math.max(-1, Math.min(1, weight + Math.random() * 0.2 - 0.1));
					}
					bufferedWriter.write(String.format("%f",weight));
					bufferedWriter.newLine();
				}
				bufferedWriter.close();
				fileWriter.close();
			}catch(IOException e){
				System.out.println("Error writing to file " + i + ".");
			}
		}
	}
}
