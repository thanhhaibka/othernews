package vcc.optimization.othernews.traindata;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.mallet.types.Instance;

/**
 * @author huutuan
 *
 */
public class CsvIteratorV1 implements Iterator<Instance> {
	LineNumberReader reader;
	Pattern lineRegex;
	int uriGroup;
	int targetGroup;
	int dataGroup;
	String currentLine;

	/**
	 * @param input
	 * @param lineRegex
	 * @param dataGroup
	 * @param targetGroup
	 * @param uriGroup
	 */
	public CsvIteratorV1(Reader input, Pattern lineRegex, int dataGroup, int targetGroup, int uriGroup) {
		this.reader = new LineNumberReader(input);
		this.lineRegex = lineRegex;
		this.targetGroup = targetGroup;
		this.dataGroup = dataGroup;
		this.uriGroup = uriGroup;
		if (dataGroup <= 0)
			throw new IllegalStateException("You must extract a data field.");
		try {
			this.currentLine = this.reader.readLine();
		} catch (IOException e) {
			throw new IllegalStateException();
		}
	}

	public CsvIteratorV1(Reader input, String lineRegex, int dataGroup, int targetGroup, int uriGroup) {
		this(input, Pattern.compile(lineRegex), dataGroup, targetGroup, uriGroup);
	}

	public CsvIteratorV1(String filename, String lineRegex, int dataGroup, int targetGroup, int uriGroup)
			throws FileNotFoundException {
		this(new FileReader(new File(filename)), Pattern.compile(lineRegex), dataGroup, targetGroup, uriGroup);
	}

	public CsvIteratorV1(String line, Pattern lineRegex, int dataGroup, int targetGroup, int uriGroup) {
		this.lineRegex = lineRegex;
		this.targetGroup = targetGroup;
		this.dataGroup = dataGroup;
		this.uriGroup = uriGroup;
		this.currentLine = line;
	}

	public Instance next() {
		String uriStr = null;
		String data = null;
		String target = null;

		Matcher matcher = this.lineRegex.matcher(this.currentLine);

		if (matcher.find()) {
			if (this.uriGroup > 0)
				uriStr = matcher.group(this.uriGroup);
			if (this.targetGroup > 0)
				target = matcher.group(this.targetGroup);
			if (this.dataGroup > 0) {
				data = matcher.group(this.dataGroup);
			}
		} else {
			data = " ";
			target = " ";
		}

		String uri;
		if (uriStr == null) {
			uri = "csvline:";
		} else {
			uri = uriStr;
		}
		assert (data != null);
		Instance carrier = new Instance(data, target, uri, null);
		try {
			this.currentLine = this.reader.readLine();
		} catch (IOException e) {
			throw new IllegalStateException();
		}
		return carrier;
	}

	public boolean hasNext() {
		return this.currentLine != null;
	}

	public void remove() {
		throw new IllegalStateException("This Iterator<Instance> does not support remove().");
	}
}
