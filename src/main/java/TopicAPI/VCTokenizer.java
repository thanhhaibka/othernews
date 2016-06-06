package TopicAPI;

import vn.edu.vnu.uet.nlp.segmenter.TPSegmenter;

public class VCTokenizer {
	private static VCTokenizer instance = null;
	private static TPSegmenter segmenter = null;

	public static VCTokenizer getInstance() {
		if (instance == null){
			System.out.println("Setup Tokenizer!");
			instance = new VCTokenizer();
			System.out.println("OK!");
		}
			
		return instance;
	}

	private VCTokenizer() {
		segmenter = new TPSegmenter();
	}

	public VCTokenizer(TPSegmenter segmenter) {
		super();
		VCTokenizer.segmenter = segmenter;
	}

	public TPSegmenter getSegmenter() {
		return segmenter;
	}
}
