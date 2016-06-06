package vcc.optimization.othernews;

import java.io.File;
import java.io.IOException;

import cc.mallet.topics.LDA;
import cc.mallet.types.InstanceList;
import cc.mallet.util.Randoms;

@SuppressWarnings("deprecation")
public class Test {
	public static void main(String[] args) {
		InstanceList ilist = InstanceList.load(new File("C:/Users/PC0102/Desktop/trainning/input/input.mallet"));
		int numIterations = args.length > 1 ? Integer.parseInt(args[1]) : 50;
		int numTopWords = args.length > 2 ? Integer.parseInt(args[2]) : 20;
		System.out.println("Data loaded.");
		LDA lda = new LDA(1);
		lda.estimate(ilist, numIterations, 50, 0, null, new Randoms()); // should
																		// be
																		// 1100
		lda.printTopWords(numTopWords, true);
		try {
			lda.printDocumentTopics(new File("C:/Users/PC0102/Desktop/trainning/input/chienout.lda"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
