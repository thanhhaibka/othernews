package vcc.optimization.othernews;

public class ModelTopicAPI {
	private int topicId;
	private double weight;
	private String topicName;
	public ModelTopicAPI(int topicId, double weight, String topicName) {
		super();
		this.topicId = topicId;
		this.weight = weight;
		this.topicName = topicName;
	}
	public int getTopicId() {
		return topicId;
	}
	public void setTopicId(int topicId) {
		this.topicId = topicId;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public String getTopicName() {
		return topicName;
	}
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	
}
