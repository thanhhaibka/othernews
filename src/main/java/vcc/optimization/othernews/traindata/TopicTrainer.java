package vcc.optimization.othernews.traindata;


import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.logging.Logger;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicModelDiagnostics;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

/**
 * @author huutuan*-
 *
 */
public class TopicTrainer {
	private static Logger logger = cc.mallet.util.MalletLogger.getLogger(TopicTrainer.class.getName());

	private static final String outputDir = "LDA-Classify/output/";

	private static final String inputDir = "LDA-Classify/input/";

	private static final String inputFile = outputDir + "input.mallet";

	private static final int numIterations = 1100;

	private static final int optimizeInterval = 0;

	private static final int optimizeBurnIn = 200;

	private static final boolean useSymmetricAlpha = false;

	private static final int numThreads = 20;

	private static final String topicKeysFile = outputDir + "topic-keys.txt";


	private static final int topWords = 100;

	private static final String outputModelFilename = outputDir+"topics.model";

	private static final String inferencerFilename = outputDir + "inferencer.mallet";

	private static final String inputModelFilename = null;

	private static final int numTopics = 30;

	private static final double alpha = 0.5D; // Choice alpha =
	// 50:numTopics and
	// beta=0.01

	private static final double beta = 0.01D;

//	private static final int randomSeed = 0;

	private static final String inputStateFilename = null;

	private static final int showTopicsInterval = 50;

//	private static final int outputStateInterval = 0;

	private static final String stateFile = null;

//	private static final int outputModelInterval = 0;

//	private static final int numMaximizationIterations = 0;

	private static final String diagnosticsFile = null;

	private static final String topicReportXMLFile = null;

	private static final String topicPhraseReportXMLFile = null;

	private static final String topicDocsFile = outputDir+"topcDocs.txt";

//	private static final int numTopDocs = 30;

	private static final String docTopicsFile = null;

//	private static final double docTopicsThreshold = 0.0D;

//	private static final int docTopicsMax = -1;

	private static final String topicWordWeightsFile = null;

	private static final String wordTopicCountsFile = outputDir + "word-topic-count.txt";

	private static final String evaluatorFilename = null;

	public static void main(String[] args) throws IOException {
		
		try {
			ImportData.importData(inputDir, inputFile);
		} catch (Exception e) {
			e.printStackTrace();
		}

		ParallelTopicModel topicModel = null;

		if (inputModelFilename != null) {
			try {
				topicModel = ParallelTopicModel.read(new File(inputModelFilename));
			} catch (Exception e) {
				logger.warning("Unable to restore saved topic model " + inputModelFilename + ": " + e);
				System.exit(1);
			}
		} else {
			topicModel = new ParallelTopicModel(numTopics, alpha, beta);
		}

		if (inputFile != null) {
			InstanceList training = null;
			try {
				training = InstanceList.load(new File(inputFile));
			} catch (Exception e) {
				logger.warning("Unable to restore instance list " + inputFile + ":" + e);
				System.exit(1);
			}

			logger.info("Data loaded.");

			if ((training.size() > 0) && (training.get(0) != null)) {
				Object data = ((Instance) training.get(0)).getData();
				if (!(data instanceof FeatureSequence)) {
					logger.warning(
							"Topic modeling currently only supports feature sequences: use --keep-sequence option when importing data.");
					System.exit(1);
				}
			}

			topicModel.addInstances(training);
		}

		if (inputStateFilename != null) {
			logger.info("Initializing from saved state.");
			topicModel.initializeFromState(new File(inputStateFilename));
		}

		topicModel.setTopicDisplay(showTopicsInterval, topWords);

		topicModel.setNumIterations(numIterations);
		topicModel.setOptimizeInterval(optimizeInterval);
		topicModel.setBurninPeriod(optimizeBurnIn);
		topicModel.setSymmetricAlpha(useSymmetricAlpha);

		topicModel.setNumThreads(numThreads);

		topicModel.estimate();
		topicModel.printTopicWordWeights(new File("topwordweight.txt"));
		topicModel.printTopWords(new File(outputDir+"topWord.txt"), 100, false);

		if (topicKeysFile != null) {
			topicModel.printTopWords(new File(topicKeysFile), topWords, false);
		}

//		if (topicKeysWeightFile != null) {
//			topicModel.printTopWordsWeight(new File(topicKeysWeightFile), topWords, false);
//		}

		if (diagnosticsFile != null) {
			PrintWriter out = new PrintWriter(diagnosticsFile);
			TopicModelDiagnostics diagnostics = new TopicModelDiagnostics(topicModel, topWords);
			out.println(diagnostics.toXML());
			out.close();
		}

		if (topicReportXMLFile != null) {
			PrintWriter out = new PrintWriter(topicReportXMLFile);
			topicModel.topicXMLReport(out, topWords);
			out.close();
		}

		if (topicPhraseReportXMLFile != null) {
			PrintWriter out = new PrintWriter(topicPhraseReportXMLFile);
			topicModel.topicPhraseXMLReport(out, topWords);
			out.close();
		}

		if (stateFile != null) {
			topicModel.printState(new File(stateFile));
		}

		if (topicDocsFile != null) {
			PrintWriter out = new PrintWriter(new FileWriter(new File(topicDocsFile)));
//			topicModel.printTopicDocuments(out, numTopDocs);
			out.close();
		}

		if (docTopicsFile != null) {
			PrintWriter out = new PrintWriter(new FileWriter(new File(docTopicsFile)));

//			topicModel.printDenseDocumentTopics(out);

			out.close();
		}

		if (topicWordWeightsFile != null) {
			topicModel.printTopicWordWeights(new File(topicWordWeightsFile));
		}

		if (wordTopicCountsFile != null) {
			topicModel.printTypeTopicCounts(new File(wordTopicCountsFile));
		}

		if (outputModelFilename != null) {
			assert (topicModel != null);
			try {
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputModelFilename));
				oos.writeObject(topicModel);
				oos.close();
			} catch (Exception e) {
				logger.warning("Couldn't write topic model to filename " + outputModelFilename);
			}
		}

		if (inferencerFilename != null) {
			try {
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(inferencerFilename));
				oos.writeObject(topicModel.getInferencer());
				oos.close();
			} catch (Exception e) {
				logger.warning("Couldn't create inferencer: " + e.getMessage());
			}
		}

		if (evaluatorFilename != null) {
			try {
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(evaluatorFilename));
				oos.writeObject(topicModel.getProbEstimator());
				oos.close();
			} catch (Exception e) {
				logger.warning("Couldn't create evaluator: " + e.getMessage());
			}
		}
	}
}
