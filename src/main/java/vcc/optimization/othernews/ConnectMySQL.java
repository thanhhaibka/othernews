package vcc.optimization.othernews;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.util.InvalidFormatException;
import vn.edu.vnu.uet.nlp.segmenter.TPSegmenter;

public class ConnectMySQL {
	private static ConnectMySQL instance;
	private Connection conn;
	// private static String eol = System.getProperty("line.separator");
	private ArrayList<String> listNews;

	public ArrayList<String> getList() {
		return listNews;
	}

	public void setList(ArrayList<String> list) {
		this.listNews = list;
	}

	protected ConnectMySQL() throws ClassNotFoundException, SQLException {
		conn = getMySQLConnection(Name.hostName, Name.dbName, Name.userName, Name.password);
//		getAllNews();
	}

	public ConnectMySQL(String s) throws ClassNotFoundException, SQLException {
		conn = getMySQLConnection(Name.hostName, Name.dbName, Name.userName, Name.password);
//		getAllNews();
	}

	public Connection getConn() {
		return conn;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}

	public static ConnectMySQL getInstance() throws ClassNotFoundException, SQLException {
		if (instance == null) {
			instance = new ConnectMySQL();
		}
		return instance;
	}

	public static ConnectMySQL getInstance(String s) throws ClassNotFoundException, SQLException {
		if (instance == null) {
			instance = new ConnectMySQL(s);
		}
		return instance;
	}

	public static java.sql.Connection getMySQLConnection(String hostName, String dbName, String userName,
			String password) throws SQLException, ClassNotFoundException {
		// Khai báo class Driver cho DB MySQL
		// Việc này cần thiết với Java 5
		// Java6 tự động tìm kiếm Driver thích hợp.
		// Nếu bạn dùng Java6, thì ko cần dòng này cũng được.
		Class.forName("com.mysql.jdbc.Driver");

		// Cấu trúc URL Connection dành cho Oracle
		// Ví dụ: jdbc:mysql://localhost:3306/simplehr
		String connectionURL = "jdbc:mysql://" + hostName + ":3306/" + dbName;

		java.sql.Connection conn = DriverManager.getConnection(connectionURL, userName, password);
		System.out.println("connected!");
		return conn;
	}

	public void getAllNews() {
		try {
			listNews = new ArrayList<String>();
			long d = 0;
			ResultSet rs = conn.createStatement()
					.executeQuery(Name.query_getDataToTrain);
			while (rs.next()) {
				String data = "-" + rs.getLong("newsId") + "\t" + rs.getString("title") + ". " + rs.getString("sapo");
				List<String> ar = JSoupTest.getStringsFromUrl(rs.getString("content"));
				String content = "";
				for (String string : ar) {
					string = string.replace("\n", " ").trim();
					content += string + " ";
					content = content.replace("-", "");
				}
				data += content;
				data = new TPSegmenter("models").segment(data);
				d++;
				listNews.add(data);
				System.out.println(d);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException, InvalidFormatException, IOException {
//		VietTokenizer tokenizer = new VietTokenizer();
//		System.out.println(tokenizer.tokenize("hôm nay trời thật đẹp.")[0]);;
//		ConnectMySQL.getInstance();
		ConnectMySQL.getInstance().getAllNews();
	}
}
