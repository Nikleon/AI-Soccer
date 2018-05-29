package not.my.code;

public class Constants {
	
	public static final int THREADS = 40;

	public static final int INPUTS = 16; //x, ballx
	public static final int HIDDEN_LAYERS = 3;
	public static final int NODES_PER_LAYER = 16;
	public static final int OUTPUT = 3; // delta x
	public static final double CROSSOVER_RATE = 0.1;
	public static final double MUTATION_RATE = 0.01;
	
										// BIAS--------------------------------|
	public static final int WEIGHTS =  (HIDDEN_LAYERS)*NODES_PER_LAYER + OUTPUT + NODES_PER_LAYER*NODES_PER_LAYER*(HIDDEN_LAYERS-1)+NODES_PER_LAYER*(INPUTS+OUTPUT);
	public static final int POPULATION_SIZE = 100;
	
	public static Action takeAction(double[] weights, double[] inputs){
		int count = 0;
		
		double[][] nodes = new double[Constants.HIDDEN_LAYERS][Constants.NODES_PER_LAYER];
		for (int i = 0; i < nodes.length; i++){
			for (int j = 0; j < nodes[i].length; j++){
				
				int inputsPerNode = Constants.NODES_PER_LAYER;
				
				if (i == 0) inputsPerNode = Constants.INPUTS;
				
				
				// BIAS
				nodes[i][j] += weights[count];
				count++;
				
				for (int k = 0; k < inputsPerNode; k++){
					if (i == 0){
						nodes[i][j] += weights[count]*inputs[k];
					}
					else{
						nodes[i][j] += weights[count]*nodes[i-1][k];
					}
					count++;
				}
				nodes[i][j] = 2/(1+Math.pow(Math.E, -nodes[i][j])) - 1;
			}
		}
		
		double endx = weights[count];
		count++;
		double endy = weights[count];
		count++;
		double endHit = weights[count];
		count++;
		
		for (int i = 0; i < Constants.NODES_PER_LAYER; i++){
			endx += nodes[nodes.length-1][i]*weights[count];
			count++;
		}
		
		for (int i = 0; i < Constants.NODES_PER_LAYER; i++){
			endy += nodes[nodes.length-1][i]*weights[count];
			count++;
		}
		
		for (int i = 0; i < Constants.NODES_PER_LAYER; i++){
			endHit += nodes[nodes.length-1][i]*weights[count];
			count++;
		}
		
		endx = 5 * Math.min(1, Math.max(-1,0.5*endx+0.5));
		endy = 5 * Math.min(1, Math.max(-1,0.5*endy+0.5));
		return new Action(endx, endy, endHit > 0);
	}
}
