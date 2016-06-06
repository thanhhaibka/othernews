package vcc.optimization.othernews.findtopic;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import TopicAPI.VCTokenizer;
import cc.mallet.pipe.Pipe;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.IDSorter;
import cc.mallet.types.InstanceList;
import vcc.optimization.othernews.ConnectMySQL;
import vcc.optimization.othernews.JSoupTest;
import vcc.optimization.othernews.Name;
import vcc.optimization.othernews.findtopic.TopicsDistribution.TopicDistribution;

public class InferDocument {
	protected static InferDocument instance = null;
	private String inferenceModel;
	private String previousInstanceListFile;
	private cc.mallet.topics.TopicInferencer inferencer = null;
	private String lineRegex = "^(\\S*)[\\s,]*(.*)$";
	private Pipe instancePipe = null;

	public static InferDocument getInstance() {
		if (instance == null) {
			instance = new InferDocument();
		}
		return instance;
	}

	public InferDocument() {
		System.out.println("Setup!");
		inferenceModel = ConfigModel.getInstance().getInferenceFile();
		previousInstanceListFile = ConfigModel.getInstance().getPreviousinstancelistfile();
		inferencer = null;
		lineRegex = "^(\\S*)[\\s,]*(.*)$";
		init();
		System.out.println("Done!");
	}

	// public InferDocument(String modelDir) {
	// ConfigModel config = ConfigModel.getInstance(modelDir);
	// inferenceModel = config.getInferenceFile();
	// previousInstanceListFile = config.getPreviousinstancelistfile();
	// inferencer = null;
	// lineRegex = "^(\\S*)[\\s,]*(.*)$";
	// init();
	// }

	public InferDocument(String modelDir) {
		ConfigModel config = new ConfigModel(modelDir);
		inferenceModel = config.getInferenceFile();
		previousInstanceListFile = config.getPreviousinstancelistfile();
		inferencer = null;
		lineRegex = "^(\\S*)[\\s,]*(.*)$";
		init();
	}

	public void init() {
		instancePipe = InstanceList.load(new File(previousInstanceListFile)).getPipe();
		try {
			inferencer = ParallelTopicModel.read(new File(inferenceModel)).getInferencer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public InstanceList createInstanceList(String data) {

		InstanceList instances = new InstanceList(instancePipe);
		instances.addThruPipe(new StringIterator(data, Pattern.compile(lineRegex), 2, 1, 1));

		return instances;
	}

	public TopicsDistribution infer(String data) {
		try {
			TopicsDistribution topics = new TopicsDistribution();
			InstanceList instances = createInstanceList(data);
			List<IDSorter> result = inferencer.inferDocument(instances.get(0), 100, 10, 10, 0.05, 100);
			for (IDSorter idSorter : result) {
				topics.add(idSorter.getID(), idSorter.getWeight());
			}

			return topics;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;

	}

	public String getContentFromNewsID(String newsId) {
		String content = "";
		String sql = Name.query_getContentFromNewsID + " " + newsId + ";";
		try {
			ResultSet rs = ConnectMySQL.getInstance().getConn().createStatement().executeQuery(sql);

			while (rs.next()) {
				content += rs.getString("title");
				content+=" .";
				content += rs.getString("sapo");
				List<String> ar = JSoupTest.getStringsFromUrl(rs.getString("content"));
				for (String string : ar) {
					string = string.replace("\n", " ").trim();
					content += string + " ";
					content = content.replace("-", "");
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return content;
	}

	public ArrayList<String> getTopicsFromContent(String newsid) {
		ArrayList<String> ar = new ArrayList<String>();
		try {
			String str = getContentFromNewsID(newsid);
			System.out.println("content: " + str);
			
			String data = VCTokenizer.getInstance().getSegmenter().segment(str);
			// data = data.toLowerCase();
			System.out.println("input : " + data);
			TopicsDistribution topic = infer(data);
			for (TopicDistribution tp : topic.getTopics()) {
				System.out.println(tp);
				String out = tp.getTopicId() + "@-}" + tp.getW() + "@-}" + Name.topics[tp.getTopicId()];
				ar.add(out);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ar;
	}

	public JSONArray getTopicsJSONFromContent(String newsid) {
		JSONArray array = new JSONArray();
		try {
			String str = getContentFromNewsID(newsid);
			System.out.println("content: " + str);
			String data = VCTokenizer.getInstance().getSegmenter().segment(str);
			// data = data.toLowerCase();
			System.out.println("input : " + data);
			TopicsDistribution topic = infer(data);
			for (TopicDistribution tp : topic.getTopics()) {

				// System.out.println(tp);
				// String out = tp.getTopicId() + "@-}" + tp.getW() + "@-}" +
				// topics[tp.getTopicId()];
				// ModelTopicAPI api = new ModelTopicAPI(tp.getTopicId(),
				// tp.getW(), topics[tp.getTopicId()]);
				String js = "{\"topicId\":\"" + tp.getTopicId() + "\",\"weight\":\"" + tp.getW() + "\",\"topicName\":\""
						+ Name.topics[tp.getTopicId()] + "\"}";
				System.out.println(js);
				JSONObject jsonObject = new JSONObject(js);
				array.put(jsonObject);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return array;
	}

	public static void main(String[] args) {
		InferDocument document = new InferDocument();
		System.out.println("Loaded Model!");
		try {
			System.out.println(document.getTopicsJSONFromContent("20160606025217962").toString(4));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

// }
