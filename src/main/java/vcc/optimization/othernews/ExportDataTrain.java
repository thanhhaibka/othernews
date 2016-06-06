package vcc.optimization.othernews;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.util.List;

import TopicAPI.VCTokenizer;
import opennlp.tools.util.InvalidFormatException;
import vn.edu.vnu.uet.nlp.segmenter.TPSegmenter;

public class ExportDataTrain {

	public void writeData(String fileName) throws InvalidFormatException, IOException {
		BufferedWriter out = null;
		long d = 1;
		try {
			System.out.println("start");
			ResultSet rs = ConnectMySQL.getInstance().getConn().createStatement()
					.executeQuery(Name.query_getDataToTrain);
			File f = new File("C:\\Users\\PC0102\\Documents\\VPProjects\\othernews\\LDA-Classify\\input\\" + fileName);
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f, true), "UTF-8"));
			System.out.println("ok run!");
			TPSegmenter segment = VCTokenizer.getInstance().getSegmenter();
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
				try {

					data = segment.segment(data);
					if (!data.contains(" ̉")) {
						System.out.println(d);
						d++;
						out.write(data);
						out.newLine();
					}
				} catch (Exception ex) {
					System.out.println("Fail!" + data);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static void main(String[] args) throws InvalidFormatException, IOException {
		// String str = "<p align=\"right\">Theo <span style=\"FONT-WEIGHT:
		// bold\">Linh Anh</span><br>";
		// Document doc = Jsoup.parse(str);
		new ExportDataTrain().writeData("data.txt");
	}
}
