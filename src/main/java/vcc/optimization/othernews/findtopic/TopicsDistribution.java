package vcc.optimization.othernews.findtopic;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author datluyen
 *
 */
public class TopicsDistribution{

	public static class TopicDistribution implements Comparable<TopicDistribution> {

		private int topicId;
		private double w;

		public TopicDistribution() {
		}

		public TopicDistribution(TopicDistribution obj) {
			this.topicId = obj.topicId;
			this.w = obj.w;
		}

		public TopicDistribution(int topicId, double w) {
			super();
			this.topicId = topicId;
			this.w = w;
		}

		public int getTopicId() {
			return topicId;
		}

		public void setTopicId(int topicId) {
			this.topicId = topicId;
		}

		public double getW() {
			return w;
		}

		public void setW(double w) {
			this.w = w;
		}

		public void readFields(DataInput in) throws IOException {

			this.topicId = in.readInt();
			this.w = in.readDouble();

		}

		public void write(DataOutput out) throws IOException {

			out.writeInt(topicId);
			out.writeDouble(w);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Item [topicId=");
			builder.append(topicId);
			builder.append(", w=");
			builder.append(w);
			builder.append("]");
			return builder.toString();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(TopicDistribution o) {

			if (o.w > w) {
				return 1;
			} else if (o.w < w) {
				return -1;
			}

			return 0;
		}

	}

	private List<TopicDistribution> topics = new ArrayList<TopicDistribution>();

	public TopicsDistribution() {

	}

	public TopicsDistribution(TopicsDistribution obj) {

		this.topics = new ArrayList<TopicDistribution>();
		for (TopicDistribution tp : obj.topics) {
			topics.add(new TopicDistribution(tp));
		}

	}

	public void clone(TopicsDistribution topicsDistribution) {
		this.topics.addAll(topicsDistribution.topics);
	}

	public List<TopicDistribution> getTopics() {
		return topics;
	}

	public void setTopics(List<TopicDistribution> topics) {
		this.topics = topics;
	}

	public void add(int topic, double w) {
		topics.add(new TopicDistribution(topic, w));
	}

	public void add(TopicDistribution item) {
		topics.add(item);
	}

	public void readFields(DataInput in) throws IOException {

		topics.clear();

		int size = in.readInt();
		for (int i = 0; i < size; i++) {
			TopicDistribution topic = new TopicDistribution(in.readInt(), in.readDouble());
			topics.add(topic);
		}

	}

	public void write(DataOutput out) throws IOException {

		out.writeInt(topics.size());
		for (TopicDistribution topic : topics) {
			topic.write(out);
		}

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (TopicDistribution tp : topics) {
			if (tp.w > 0) {
				builder.append(tp.topicId);
				builder.append(":");
				builder.append(tp.getW());
				builder.append(" ");
			}
		}
		return builder.toString();
	}

	public void sort() {
		Collections.sort(topics);
	}
}
