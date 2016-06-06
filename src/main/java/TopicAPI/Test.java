package TopicAPI;

import java.sql.SQLException;

import org.rapidoid.config.Conf;
import org.rapidoid.http.fast.On;

import vcc.optimization.othernews.findtopic.InferDocument;
import vn.vccorp.bigdata.scher.sqlconnection.ScherConnectionPool;

public class Test {
	public static void main(String[] args) {
		VCTokenizer.getInstance();
		InferDocument.getInstance();

		try {
			ScherConnectionPool.initAllConnection(ScherConnectionPool.DEVELOPMENT_TYPE);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		MasterListener listener = new MasterListener();
		listener.start();
		Conf.set("port", 8888);
		On.page("/hi").gui("<b>Tùng</b> óc heo! </br> <b>Service dễ vờ lờ</b>!");
		On.get("/gettopic").plain(new TopicsFromNews());
	}
}