package vcc.optimization.othernews.traindata;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.types.InstanceList;

/**
 * @author huutuan
 * @description Import data to train LDA model
 */
public class ImportData {
	public static final String INPUT_DIR = "LDA-Classify/input/0.0";
	public static final String OUTPUT_DIR = "LDA-Classify/output/13.0/";
	public static File outFile;

	public static void main(String[] args) throws IllegalArgumentException, IOException {
		try {
			importData(INPUT_DIR, OUTPUT_DIR);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void importData(String inputDir, String outputDir)
			throws UnsupportedEncodingException, FileNotFoundException {
		System.out.println("Data Importing ...");

		File directory = new File(inputDir);
		File[] files = directory.listFiles();

		outFile = new File(outputDir);

		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		pipeList.add(new CharSequenceLowercase());
		pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
		pipeList.add(new TokenSequenceRemoveStopwords(new File("LDA-Classify/resource/VCStopWords.txt"), "UTF-8", false, false,
				false));
		pipeList.add(new TokenSequence2FeatureSequence());

		InstanceList instances = new InstanceList(new SerialPipes(pipeList));

		for (File file : files) {
			Reader fileReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
			instances.addThruPipe(new CsvIteratorV1(fileReader, Pattern.compile("^(\\S*)[\\s,]*(.*)$"), 2, 1, 0));
		}

		instances.save(outFile);
		System.out.println("Data imported");
	}

	public static void importDataV1(String inputFile, String outputDir)
			throws UnsupportedEncodingException, FileNotFoundException {
		System.out.println("Data importing...");

		File file = new File(inputFile);

		outFile = new File(outputDir);

		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		pipeList.add(new CharSequenceLowercase());
		pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
		pipeList.add(new TokenSequenceRemoveStopwords(new File("LDA-Classify/resource/VCStopWords.txt"), "UTF-8", false,
				false, false));
		pipeList.add(new TokenSequence2FeatureSequence());

		InstanceList instances = new InstanceList(new SerialPipes(pipeList));

		Reader fileReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
		instances.addThruPipe(new CsvIteratorV1(fileReader, Pattern.compile("^(\\S*)[\\s,]*(.*)$"), 2, 1, 0));

		instances.save(outFile);
		System.out.println("Data imported!");
	}
}
