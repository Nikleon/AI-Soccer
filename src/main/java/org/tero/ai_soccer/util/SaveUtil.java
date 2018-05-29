package org.tero.ai_soccer.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Scanner;

import org.tero.ai_soccer.ai.NeuralNet;
import org.tero.ai_soccer.ai.WeightMatrix;

public class SaveUtil {

    private static final String SAVE_DIR = "save/";
    private static final String FILE_EXT = ".txt";

    public static void save(NeuralNet network, String saveName) {
	mkdirSaveDir();

	try (FileWriter out = new FileWriter(new File(SAVE_DIR + saveName + FILE_EXT))) {
	    out.write(network.getDepth() + "\n");
	    for (int width : network.getShape())
		out.append(width + " ");
	    out.append("\n");

	    for (int i = 0; i < network.getWeights().length; i++) {
		out.append(network.getWeights()[i].toString());
	    }

	    out.close();
	} catch (IOException e) {
	    System.err.println("Could not create save file: " + SAVE_DIR + saveName + FILE_EXT);
	}
    }

    public static <N> N load(Class<N> nnType, String saveName) {
	mkdirSaveDir();

	N ret;
	Scanner in = null;
	try {
	    in = new Scanner(new File(SAVE_DIR + saveName + FILE_EXT));

	    int depth = in.nextInt();
	    in.nextLine();

	    int[] shape = new int[depth + 1];
	    for (int i = 0; i < shape.length; i++) {
		shape[i] = in.nextInt();
	    }
	    in.nextLine();

	    WeightMatrix[] weights = new WeightMatrix[depth];
	    for (int preLayer = 0; preLayer < depth; preLayer++) {
		weights[preLayer] = new WeightMatrix(shape[preLayer + 1], shape[preLayer]);
		for (int r = 0; r < weights[preLayer].numRows(); r++) {
		    for (int c = 0; c < weights[preLayer].numCols(); c++) {
			weights[preLayer].set(r, c, in.nextDouble());
		    }
		    in.nextLine();
		}
	    }

	    Constructor<N> constructor = nnType.getConstructor(shape.getClass(), weights.getClass());
	    ret = constructor.newInstance(shape, weights);

	    in.close();
	} catch (Exception e) {
	    System.err.println("Could not load save file: " + SAVE_DIR + saveName + FILE_EXT);
	    System.err.println("Generating random save...\n");
	    generateRandomSave(saveName);
	    return load(nnType, saveName);
	}

	return ret;
    }

    public static void generateRandomSave(String saveName) {
	mkSaveFile(saveName);
	save(new NeuralNet(NeuralNet.DEFAULT_SHAPE), saveName);
    }

    private static void mkdirSaveDir() {
	File saveDirectory = new File(SAVE_DIR);
	if (!saveDirectory.exists())
	    saveDirectory.mkdirs();
    }

    private static void mkSaveFile(String saveName) {
	File saveFile = new File(SAVE_DIR + saveName + FILE_EXT);
	if (!saveFile.exists())
	    try {
		saveFile.createNewFile();
	    } catch (IOException e) {
		System.err.println("Could not create file: " + SAVE_DIR + saveName + FILE_EXT);
	    }
    }

}
