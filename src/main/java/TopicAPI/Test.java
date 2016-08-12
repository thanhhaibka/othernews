package TopicAPI;

import java.sql.SQLException;

import org.rapidoid.config.Conf;

import org.rapidoid.setup.On;
import vcc.optimization.othernews.ConnectMySQL;
import vcc.optimization.othernews.findtopic.ConfigModel;
import vcc.optimization.othernews.findtopic.InferDocument;
import vn.vccorp.bigdata.scher.sqlconnection.ScherConnectionPool;

public class Test {
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		ConnectMySQL.getInstance();
		ConfigModel.getInstance();
		Cassandra.getInstance();
//		CassandraDAO.getInstance();
//		Cassandra.getInstance();
		VCTokenizer.getInstance();
		InferDocument.getInstance();
		
		try {
			ScherConnectionPool.initAllConnection(ScherConnectionPool.DEVELOPMENT_TYPE);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		MasterListener listener = new MasterListener();
		listener.start();
		On.port(8081);
//		On.get("/gettopic").plain(new TopicsFromNews());
//		On.get("/getnews").plain(new NewsIDFromGuidSourceNews());
		On.get("/rs").plain(new NewsRecommended());

	}
}