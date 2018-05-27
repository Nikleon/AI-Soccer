package not.my.code;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class CreateWeights {
	public static void main(String[] args){
		for (int i = 0; i < Constants.POPULATION_SIZE; i++){
			try{
				FileWriter fileWriter = new FileWriter("Weights" + String.format("%03d", i) + ".txt");
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				for (int j = 0; j < Constants.WEIGHTS; j++){
					bufferedWriter.write(String.format("%f",Math.random()*1-0.5)); // between -1 and 1.
					bufferedWriter.newLine();
				}
				bufferedWriter.close();
				fileWriter.close();
			}catch(IOException e){
				System.out.println("IOException occurred when writing to file \"Weights" + String.format("%03d", i) + ".txt\"");
				System.exit(1);
			}
		}
		System.out.println("Finished Initializing Weights!");
	}
}
