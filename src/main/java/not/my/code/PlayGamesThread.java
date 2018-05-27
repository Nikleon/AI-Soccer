package not.my.code;
public class PlayGamesThread extends Thread {
		
		int index;
		double[] scores;
		
		public PlayGamesThread(int index, double[] scores){
			this.index = index;
			this.scores = scores;
			start();
		}
		
		public void run(){ // play against each other
			for (int i = index * Constants.POPULATION_SIZE / Constants.THREADS / 2; i < (index + 1) * Constants.POPULATION_SIZE / Constants.THREADS / 2; i++){
				GameState gameState = new GameState(2*i, 2*i+1);
				for (int j = 0; j < 10000; j++){
					gameState.update();
				}
				
				scores[2*i] = gameState.lScore - gameState.rScore + 0.01;
				scores[2*i+1] = gameState.rScore - gameState.lScore + 0.01;
			}
		}
		
//		public void run(){ // play against ai
//			for (int i = index * Constants.POPULATION_SIZE / Constants.THREADS; i < (index + 1) * Constants.POPULATION_SIZE / Constants.THREADS; i++){
//				GameState gameState = new GameState(i, -1);
//				for (int j = 0; j < 10000; j++){
//					gameState.update();
//				}
//				
//				scores[i] = gameState.lScore - gameState.rScore + 0.01;
//			}
//		}
	}