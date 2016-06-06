package vcc.optimization.othernews.findtopic;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.mallet.pipe.Pipe;

/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */

/** 
   @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
 */


import cc.mallet.types.Instance;

/**
 * This iterator, perhaps more properly called a Line Pattern Iterator, reads
 * through a file and returns one instance per line, based on a regular
 * expression.
 * <p>
 * 
 * If you have data of the form
 * 
 * <pre>
 * [name]  [label]  [data]
 * </pre>
 * 
 * and a {@link Pipe} <code>instancePipe</code>, you could read instances using
 * this code:
 * 
 * <pre>
 * InstanceList instances = new InstanceList(instancePipe);
 * 
 * instances.addThruPipe(new CsvIterator(new FileReader(dataFile), "(\\w+)\\s+(\\w+)\\s+(.*)", 3, 2, 1) // (data,
 * 																										// target,
 * 																										// name)
 * 																										// field
 * 																										// indices
 * );
 * </pre>
 *
 */
public class StringIterator implements Iterator<Instance> {;
	public Pattern lineRegex;
	public int uriGroup, targetGroup, dataGroup;
	public String currentLine;

	public StringIterator(String line, Pattern lineRegex, int dataGroup, int targetGroup, int uriGroup) {
		this.lineRegex = lineRegex;
		this.targetGroup = targetGroup;
		this.dataGroup = dataGroup;
		this.uriGroup = uriGroup;
		this.currentLine = line;
	}

	// The PipeInputIterator interface

	public Instance next() {
		String uriStr = null;
		String data = null;
		String target = null;

		Matcher matcher = lineRegex.matcher(currentLine);
		if (matcher.find()) {
			if (uriGroup > 0)
				uriStr = matcher.group(uriGroup);
			if (targetGroup > 0)
				target = matcher.group(targetGroup);
			if (dataGroup > 0)
				data = matcher.group(dataGroup);

//			System.out.println("data input " + data);
			
		} else {
			//throw new IllegalStateException(
			//		"Line # does not match regex:\n" + currentLine);
			
			data = " ";
			
		}

		String uri;
		if (uriStr == null) {
			uri = "csvline:";
		} else {
			uri = uriStr;
		}

		Instance carrier = new Instance(data, target, uri, null);
		this.currentLine = null;
		return carrier;
	}

	public boolean hasNext() {
		return currentLine != null;
	}

	public void remove() {
		throw new IllegalStateException("This Iterator<Instance> does not support remove().");
	}

}
