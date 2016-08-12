package vcc.optimization.othernews.findtopic;

import TopicAPI.Cassandra;
import TopicAPI.JsonReader;
import TopicAPI.URLUnshortener;
import TopicAPI.VCTokenizer;
import cc.mallet.pipe.Pipe;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.IDSorter;
import cc.mallet.types.InstanceList;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;
import org.apache.http.client.ClientProtocolException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import vcc.optimization.othernews.ConnectMySQL;
import vcc.optimization.othernews.JSoupTest;
import vcc.optimization.othernews.Name;
import vcc.optimization.othernews.findtopic.TopicsDistribution.TopicDistribution;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

public class InferDocument {
	protected static InferDocument instance = null;
	private String inferenceModel;
	private String previousInstanceListFile;
	private static cc.mallet.topics.TopicInferencer inferencer = null;
	private static String lineRegex = "^(\\S*)[\\s,]*(.*)$";
	private static Pipe instancePipe = null;
	public static ConnectMySQL connect;
	public static Cassandra cassandra;
	public static Map<String, ArrayList<Topic>> map= null;

	public static InferDocument getInstance() throws SQLException, ClassNotFoundException {
		if (instance == null) {
			instance = new InferDocument();
			cassandra= new Cassandra();
			connect= new ConnectMySQL();
		}
		return instance;
	}

	public static InferDocument getInstance(int i) {
		if (instance == null) {
			instance = new InferDocument(i);
		}
		return instance;
	}

	public InferDocument(int i) {

	}

	public InferDocument() {
		System.out.println("Setup!");
		VCTokenizer.getInstance();
		inferenceModel = ConfigModel.getInstance().getInferenceFile();
		previousInstanceListFile = ConfigModel.getInstance().getPreviousinstancelistfile();
		inferencer = null;
		lineRegex = "^(\\S*)[\\s,]*(.*)$";
		init();
		// System.out.println(getTopicsJSONFromContent("2016061009573642"));
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

	public static InstanceList createInstanceList(String data) {

		InstanceList instances = new InstanceList(instancePipe);
		instances.addThruPipe(new StringIterator(data, Pattern.compile(lineRegex), 2, 1, 1));

		return instances;
	}

	public static TopicsDistribution infer(String data) {
		try {
			TopicsDistribution topics = new TopicsDistribution();
			InstanceList instances = createInstanceList(data);
			if (instances == null || instances.size() == 0)
				return null;
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

	public static String getContentFromNewsIDByMYSQL(String newsId) {
		String content = "";
		String sql = Name.query_getContentFromNewsID + " " + Long.parseLong(newsId);
		try {
			ResultSet rs = ConnectMySQL.getInstance().getConn().createStatement().executeQuery(sql);
			String cql = "INSERT INTO othernews.newsurl (newsid,content,sapo,title,url) VALUES (";
			while (rs.next()) {
				String title = rs.getString("title");
				String sapo = rs.getString("sapo");
				cql += rs.getLong("newsId") + ",'";

				content += title;
				content += " .";
				content += sapo;
				List<String> ar = JSoupTest.getStringsFromUrl(rs.getString("content"));
				for (String string : ar) {
					string = string.replace("\n", " ").trim();
					content += string + " ";
					content = content.replace("-", "");
					cql += string.replace("-", "");
				}
				cql += "','" + sapo + "','" + title + "','" + rs.getString("url") + "');";
			}
			try {
				Cassandra.getInstance().getSession().execute(cql);
			} catch (Exception ex) {

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;
	}

	public static String getContentFromNewsIDByAPI(String newsId) {
		String content = "";
		try {
			String api = Name.APIgetContentNews + URLUnshortener.getURLFrom(newsId);
			JSONObject array;
			try {
				array = JsonReader.readJsonObjectFromUrl(api);
				if (array == null)
					return content;
				try {
					String str = array.getString("title");
					content += str;
				} catch (Exception x) {

				}
				try {
					String str1 = array.getString("descriptions");
					content += str1;
				} catch (Exception x) {

				}
				try {
					String str2 = array.getString("content");
					content += str2;
				} catch (Exception x) {

				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		}
		return content;
	}

	public String getContentFromNewsIDByCass(String newsId) {
		String content = "";
		String cql = "SELECT * FROM othernews.newsurl WHERE newsid  =  " + newsId;
		com.datastax.driver.core.ResultSet rs = Cassandra.getInstance().getSession().execute(cql);
		Row r = rs.one();
		if (r != null) {
			content += r.getString("title") + ". " + r.getString("content");
			List<String> ar;
			try {
				ar = JSoupTest.getStringsFromUrl(r.getString("sapo"));
				for (String string : ar) {
					string = string.replace("\n", " ").trim();
					content += string + " ";
					content = content.replace("-", "");
				}
			} catch (IOException e) {
			}
		}
		return content;
	}

	public ArrayList<String> getTopicsFromContent(String newsid) {
		ArrayList<String> ar = new ArrayList<String>();
		try {
			String str = getContentFromNewsIDByMYSQL(newsid);
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

	public ArrayList<Topic> getTopicsFromContent1(String newsid) {
		ArrayList<Topic> ar = new ArrayList<Topic>();
		try {
			String str = getContentFromNewsIDByMYSQL(newsid);
//			System.out.println("content: " + str);

			String data = VCTokenizer.getInstance().getSegmenter().segment(str);
			// data = data.toLowerCase();
//			System.out.println("input : " + data);
			TopicsDistribution topic = infer(data);
			for (TopicDistribution tp : topic.getTopics()) {
//				System.out.println(tp);
				Topic t = new Topic(tp.getTopicId() + "", Name.topics[tp.getTopicId()], tp.getW() + "");
				ar.add(t);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ar;
	}

	public List<News> getNewsRecommended(String newsID, ArrayList<String> ids, ConnectMySQL connect, Cassandra cassandra) throws SQLException, ClassNotFoundException {
		News news = new News(newsID);
		long t= System.currentTimeMillis();
		news.setVectorTopics(getTopicsFromContent1(newsID));
//		System.out.println("cv time =" + (System.currentTimeMillis() - t));
		List<News> newss = new ArrayList<News>();
		if(map==null){
			map= getTopicsFromCassandra(ids, cassandra);
		}else{
			updateTopicsFromCassandra(ids, cassandra);
		}
//		System.err.println("cass time =" + (System.currentTimeMillis() - t));
		for (String key: map.keySet()) {
			News ne = new News(key);
			ne.setVectorTopics(map.get(key));
			ne.setCousinValue(ne.cosin(news));
//			System.out.print(n.cosin(news)+" ");
			newss.add(ne);
		}
//		System.out.println("set time =" +"  " + (System.currentTimeMillis() - t));
		Collections.sort(newss);
		Collections.reverse(newss);
//		System.out.println("sort time =" + (System.currentTimeMillis() - t));
		List<News> newss2 = newss.subList(1, 11);
		String sql = "SELECT newsId, title, url, publishDate FROM  `news`.`news_resource` where newsId in ( ";
		for (int i = 0; i < 9; i++) {
			sql += "' " + newss2.get(i).getNewsID() + " ', ";
		}
		sql += "' " + newss2.get(9).getNewsID() + " ');";
		ResultSet rs = connect.getInstance().getConn().createStatement().executeQuery(sql);
		while (rs.next()) {
			for (int i = 0; i < 10; i++) {
				if (rs.getString("newsId").equals(newss2.get(i).getNewsID())) {
					newss2.get(i).setTitle(rs.getString("title"));
					newss2.get(i).setUrl(rs.getString("url"));
					newss2.get(i).setDate(rs.getDate("publishDate"));
				}
			}
		}
//		for (int i = 0; i < 10; i++) {
//			System.out.println(newss2.get(i).getTitle());
//		}
		return newss2;
	}

	public ArrayList<Topic> getTopicsFromCassandra(String newsID, Cassandra cassandra) {
		String sql = "select * from  othernews.newscategoryscore where newsid =" + newsID + ";";
		ArrayList<Topic> topics = new ArrayList<Topic>();
		try {
			Row row = cassandra.getSession().execute(sql).one();
			for (int i = 0; i < 30; i++) {
				topics.add(new Topic(i + "", Name.topics[i], row.getFloat(Name.topics[i]) + ""));
			}
		} catch (Exception e) {

		}
		return topics;
	}

	public void updateTopicsFromCassandra(ArrayList<String> ids, Cassandra cassandra) {
		for(int i=0; i<ids.size(); i++){
			if(map.containsKey(ids.get(i))) ids.remove(i);
		}
		String sql = "select * from  othernews.newscategoryscore where newsid in ( "+ids.get(0);
		for(int i=1; i<ids.size()-1; i++){
			sql+=" , "+ids.get(i);
		}
		sql+=" )";
		try {
			List<Row> rows = cassandra.getSession().execute(sql).all();
			for(Row r: rows){
				ArrayList<Topic> topics= new ArrayList<>();
				for (int i = 0; i < 30; i++) {
					topics.add(new Topic(i + "", Name.topics[i], r.getFloat(Name.topics[i]) + ""));
				}
				map.put(r.getLong("newsid")+"", topics);
			}
		} catch (Exception e) {

		}
	}

	public Map<String, ArrayList<Topic>> getTopicsFromCassandra(ArrayList<String> ids, Cassandra cassandra) {
		String sql = "select * from  othernews.newscategoryscore where newsid in ( "+ids.get(0);
		for(int i=1; i<ids.size()-1; i++){
			sql+=" , "+ids.get(i);
		}
		sql+=" )";
		Map<String, ArrayList<Topic>> news = new HashMap<String, ArrayList<Topic>>();
		try {
			List<Row> rows = cassandra.getSession().execute(sql).all();
			for(Row r: rows){
				ArrayList<Topic> topics= new ArrayList<>();
				for (int i = 0; i < 30; i++) {
					topics.add(new Topic(i + "", Name.topics[i], r.getFloat(Name.topics[i]) + ""));
				}
				news.put(r.getLong("newsid")+"", topics);
			}
		} catch (Exception e) {

		}
		return news;
	}

	public JSONArray getTopicsJSONFromContent(String newsid) {
		if (newsid == null || newsid.length() == 0)
			return null;
		JSONArray array = new JSONArray();
		String cql = " SELECT * FROM othernews.newscategoryscore WHERE newsid = " + newsid;
		Row r = Cassandra.getInstance().getSession().execute(cql).one();
		if (r != null) {
			for (int i = 0; i < Name.topics.length; i++) {
				if (r.getFloat(Name.topics[i]) > 0) {
					String js = "{\"topicId\":\"" + i + "\",\"weight\":\"" + r.getFloat(Name.topics[i])
							+ "\",\"topicName\":\"" + Name.topics[i] + "\"}";
					System.out.println(js);
					JSONObject jsonObject;
					try {
						jsonObject = new JSONObject(js);
						array.put(jsonObject);
					} catch (org.codehaus.jettison.json.JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			return array;
		}

		try {
			String str = "";
			// str = getContentFromNewsIDByMYSQL(newsid);
			str = getContentFromNewsIDByCass(newsid);
			if (str == null || str.length() == 0) {
				str = getContentFromNewsIDByMYSQL(newsid);
				System.out.println("MYSQL: " + str);
			} else {
				System.out.println("API: " + str);
			}
			String data = null;
			try {
				data = VCTokenizer.getInstance().getSegmenter().segment(str);
			} catch (Exception e) {
			}
			// data = data.toLowerCase();
			if (data == null)
				data = str;
			System.out.println("input : " + data);
			TopicsDistribution topic = infer(data);
			Insert insert = QueryBuilder.insertInto("newscategoryscore").value("newsid", Long.parseLong(newsid));
			if (topic == null) {
				String js = "{\"topicId\":\"" + "\",\"weight\":\"" + "\",\"topicName\":\"" + "\"}";
				System.out.println(js);
				JSONObject jsonObject = new JSONObject(js);
				array.put(jsonObject);
			} else {
				for (TopicDistribution tp : topic.getTopics()) {

					// System.out.println(tp);
					// String out = tp.getTopicId() + "@-}" + tp.getW() + "@-}"
					// +
					// topics[tp.getTopicId()];
					// ModelTopicAPI api = new ModelTopicAPI(tp.getTopicId(),
					// tp.getW(), topics[tp.getTopicId()]);
					insert.value(Name.topics[tp.getTopicId()], tp.getW());

					String js = "{\"topicId\":\"" + tp.getTopicId() + "\",\"weight\":\"" + tp.getW()
							+ "\",\"topicName\":\"" + Name.topics[tp.getTopicId()] + "\"}";
					System.out.println(js);
					JSONObject jsonObject = new JSONObject(js);
					array.put(jsonObject);
				}
				Cassandra.getInstance().getSession().execute(insert);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return array;
	}

	public JSONArray getNewsJSONFrom(String newsid){
		if (newsid == null || newsid.length() == 0)
			return null;
		long startTime = System.currentTimeMillis();

		JSONArray array = new JSONArray();

		String cql = " SELECT * FROM othernews.newsrecommended WHERE newsid = " + newsid;
		Row r = null;

		try {
			r = cassandra.getSession().execute(cql).one();
			System.out.println("Init time =" + (System.currentTimeMillis() - startTime));
		} catch (Exception e) {

		}

		if (r != null) {
			Map<Long, String> map = r.getMap(2, Long.class, String.class);
			for (Long l : map.keySet()) {
				String js = "{\"newsid\":\"" + l + "\",\"url\":\"" + map.get(l) + "\"}";
				JSONObject jsonObject;
				try {
					jsonObject = new JSONObject(js);
					array.put(jsonObject);
				} catch (org.codehaus.jettison.json.JSONException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Query time =" + (System.currentTimeMillis() - startTime));
			return array;
		}
		try {
			News var1 = new News(newsid);
			String sql = "select title, url, publishDate from `news`.`news_resource` where newsId = " + newsid;
			ResultSet rs = connect.getConn().createStatement().executeQuery(sql);
			while (rs.next()) {
				var1.setDate(rs.getDate("publishDate"));
				var1.setUrl(rs.getString("url"));
				var1.setTitle(rs.getString("title"));
				var1.setDate(rs.getDate("publishDate"));
			}
//			long t= System.currentTimeMillis();
			ArrayList<String> ids= connect.getNewNews();
			List<News> top10 = getNewsRecommended(newsid, ids, connect, cassandra);
//			System.out.println("recommend time =" + (System.currentTimeMillis() - t));
			insert(var1, top10, cassandra);
			for (int i = 0; i < top10.size(); i++) {
				String js = "{\"newsid\":\"" + top10.get(i).getNewsID() + "\",\"url\":\"" + top10.get(i).getUrl() + "\"}";
				JSONObject jsonObject;
				try {
					jsonObject = new JSONObject(js);
					array.put(jsonObject);
				} catch (org.codehaus.jettison.json.JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Query time =" + (System.currentTimeMillis() - startTime));
		return array;
	}

	public JSONArray getTopicsJSONFromContent(String newsid, String content) {
		if (newsid == null || newsid.length() == 0)
			return null;
		JSONArray array = new JSONArray();
		try {
			String str = content;
			System.out.println(newsid);
			if (str == null || str.length() < 3)
				return null;
			System.out.println("content: " + str);
			String data = null;
			try {
				data = VCTokenizer.getInstance().getSegmenter().segment(str);
			} catch (Exception e) {
			}
			// data = data.toLowerCase();
			if (data == null)
				data = str;
			System.out.println("input : " + data);
			TopicsDistribution topic = infer(data);
			if (topic == null) {
				String js = "{\"topicId\":\"" + "\",\"weight\":\"" + "\",\"topicName\":\"" + "\"}";
				System.out.println(js);
				JSONObject jsonObject = new JSONObject(js);
				array.put(jsonObject);
			} else

				for (TopicDistribution tp : topic.getTopics()) {

					// System.out.println(tp);
					// String out = tp.getTopicId() + "@-}" + tp.getW() + "@-}"
					// +
					// topics[tp.getTopicId()];
					// ModelTopicAPI api = new ModelTopicAPI(tp.getTopicId(),
					// tp.getW(), topics[tp.getTopicId()]);
					String js = "{\"topicId\":\"" + tp.getTopicId() + "\",\"weight\":\"" + tp.getW()
							+ "\",\"topicName\":\"" + Name.topics[tp.getTopicId()] + "\"}";
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

	public static String getUrlFromMissingUrl(String newsId) {
		String out = null;
		String sourceNews[] = {"http://afamily.vn/", "http://autopro.com.vn/", "http://cafebiz.vn/",
				"http://cafef.vn/", "http://gamek.vn/", "http://genk.vn/", "http://soha.vn/", "http://kenh14.vn/"};
		for (int i = 0; i < sourceNews.length; i++) {

			String missURL = "";
			if (i == sourceNews.length - 2)
				missURL += sourceNews[i] + "lagquadi-" + newsId + ".htm";
			else
				missURL += sourceNews[i] + "lagquadi-" + newsId + ".chn";
			missURL = "http://afamily.vn/lagquadi-20160605061948564.chn";
			// Document doc =
			// Jsoup.connect("http://soha.vn/nan-2016060221352898.htm").get();
			// System.out.println(missURL);
			try {
				Document doc = Jsoup.connect(missURL).get();
				Elements elements = doc.getElementsByTag("link");
				int kt = 0;
				for (Element el : elements) {
					if (el.attr("rel").equalsIgnoreCase("canonical")) {
						String str = el.attr("href");
						System.out.println(str);
						if (str.contains(newsId) && !str.contains("lagquadi")) {
							out = str;
							kt = 1;
							break;
						}
					}

				}
				if (kt == 1)
					break;
			} catch (Exception ex) {
			}
		}
		return out;
	}

	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://192.168.3.67:3306/news";

	// Database credentials
	static final String USER = "haint";
	static final String PASS = "xkskJkIOqO1OB9Q734";

	public static void ConnectMySQL() {
		Connection conn = null;
		Statement stmt = null;
		try {
			// STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = (Connection) DriverManager.getConnection(DB_URL, USER, PASS);

			// STEP 4: Execute a query
			System.out.println("Creating statement...");
			stmt = (Statement) conn.createStatement();
			String sql;
			sql = "SELECT id, first, last, age FROM Employees";
			ResultSet rs = stmt.executeQuery(sql);

			// STEP 5: Extract data from result set
			while (rs.next()) {
				// Retrieve by column name
				int id = rs.getInt("id");
				int age = rs.getInt("age");
				String first = rs.getString("first");
				String last = rs.getString("last");

				// Display values
				System.out.print("ID: " + id);
				System.out.print(", Age: " + age);
				System.out.print(", First: " + first);
				System.out.println(", Last: " + last);
			}
			// STEP 6: Clean-up environment
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
		System.out.println("Goodbye!");
	}

	public void insert(News var1, List<News> var2, Cassandra cassandra) {
		Map<Long, String> var3 = new HashMap<>();
		for (int i = 0; i < var2.size(); i++) {
			var3.put(Long.parseLong(var2.get(i).getNewsID()), var2.get(i).getUrl());
		}

		com.datastax.driver.core.Statement exampleQuery = QueryBuilder.insertInto("othernews", "newsrecommended").value("newsid", Long.parseLong(var1.getNewsID()))
				.value("publishDate", var1.getDate()).value("url", var1.getUrl()).value("title", var1.getTitle()).value("tennews", var3).ifNotExists();
		cassandra.getSession().execute(exampleQuery);
	}

	public void update(News var1, List<News> var2, Cassandra cassandra) {
		Map<Long, String> var3 = new HashMap<>();
		for (int i = 0; i < var2.size(); i++) {
			var3.put(Long.parseLong(var2.get(i).getNewsID()), var2.get(i).getUrl());
		}

		com.datastax.driver.core.Statement exampleQuery = QueryBuilder.update("othernews", "newsrecommended")
				.with(QueryBuilder.set("tennews", var3)).where(QueryBuilder.eq("newsid", var1.getNewsID()));
		cassandra.getSession().execute(exampleQuery);
	}

	public void addToCass(Cassandra cassandra, ConnectMySQL connect) throws SQLException, ClassNotFoundException {
		News var1 = new News();
		ArrayList<String> ids = connect.getNewNews();

		for (int var = 0; var < ids.size(); var++) {
			String var2 = ids.get(var);
			var1.setNewsID(var2);
			Row r = null;
			try {
				String cql = " SELECT * FROM othernews.newsrecommended WHERE newsid = " + var2;
				r = cassandra.getSession().execute(cql).one();
			} catch (Exception e) {

			}
			if (r != null) continue;
			String sql = "select title, url, publishDate from `news`.`news_resource` where newsId = " + var2;
			ResultSet rs = connect.getConn().createStatement().executeQuery(sql);
			while (rs.next()) {
				var1.setDate(rs.getDate("publishDate"));
				var1.setUrl(rs.getString("url"));
				var1.setTitle(rs.getString("title"));
				var1.setDate(rs.getDate("publishDate"));
			}
			List<News> top10 = getNewsRecommended(var2, ids, connect, cassandra);
			insert(var1, top10, cassandra);
		}
		System.err.print("done");
	}

	public static void main(String[] args) throws SQLException, ClassNotFoundException {

//		ConnectMySQL();
		InferDocument i = new InferDocument();
		ConnectMySQL connect = new ConnectMySQL();
		Cassandra cassandra = new Cassandra();
		News var1 = new News();
		ArrayList<String> ids = connect.getNewNews();

		for (int var = 0; var < ids.size(); var++) {
			String var2 = ids.get(var);
			Row r = null;
			try {
				String cql = " SELECT * FROM othernews.newsrecommended WHERE newsid = " + var2;
				r = Cassandra.getInstance().getSession().execute(cql).one();
			} catch (Exception e) {

			}
			var1.setNewsID(var2);
			if (r == null) {

				String sql = "select title, url, publishDate from `news`.`news_resource` where newsId = " + var2;
				ResultSet rs = connect.getConn().createStatement().executeQuery(sql);
				while (rs.next()) {
					var1.setDate(rs.getDate("publishDate"));
					var1.setUrl(rs.getString("url"));
					var1.setTitle(rs.getString("title"));
					var1.setDate(rs.getDate("publishDate"));
				}
				List<News> top10 = i.getNewsRecommended(var2, ids, connect, cassandra);
				i.insert(var1, top10, cassandra);
			} else {
				List<News> top10 = i.getNewsRecommended(var2, ids, connect, cassandra);
				i.update(var1, top10, cassandra);
			}
		}
		System.err.print("done");

//		String sql = Name.query_getContentFromNewsID + " " + Long.parseLong("20160616084836466");
//		String content = "";
//		try {
//			ResultSet rs = ConnectMySQL.getInstance().getConn().createStatement().executeQuery(sql);
//			String cql = "INSERT INTO othernews.newsurl (newsid,content,sapo,title,url) VALUES (";
//			while (rs.next()) {
//				String title = rs.getString("title");
//				String sapo = rs.getString("sapo");
//				cql += rs.getLong("newsId") + ",'";
//
//				content += title;
//				content += " .";
//				content += sapo;
//				String ct = rs.getString("content");
//				cql += ct;
//				List<String> ar = JSoupTest.getStringsFromUrl(ct);
//				for (String string : ar) {
//					string = string.replace("\n", " ").trim();
//					content += string + " ";
//					content = content.replace("-", "");
//
//				}
//				cql += "','" + sapo + "','" + title + "','" + rs.getString("url") + "');";
//			}
//			System.out.println(cql);
//			Cassandra.getInstance().getSession().execute(cql);
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		Cassandra.getInstance().close();
		// try {
		// JSONObject array = JsonReader.readJsonObjectFromUrl(
		// "http://192.168.3.158:1607/url?link=http://dantri.com.vn/su-kien/ha-noi-lanh-thau-xuong-dan-buon-do-chong-ret-hot-bac-20160127130528447.htm");
		// System.out.println(array);
		// } catch (JSONException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// InferDocument document = new InferDocument();
		// System.out.println("Loaded Model!");
		// System.out.println(document.getTopicsJSONFromContent("2016061009573642"));

		// while (true) {
		// Scanner sc = new Scanner(System.in);
		// String str = sc.nextLine();
		// try {
		// System.out.println(document.getTopicsJSONFromContent(str).toString(4));
		// } catch (JSONException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// System.out.println(InferDocument.getUrlFromMissingUrl("20160605061948564"));

	}
}

// }
