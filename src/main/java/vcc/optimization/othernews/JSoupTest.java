package vcc.optimization.othernews;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;

public class JSoupTest {
	/*
	 * Outputs: Members can login for access to exclusive content, event
	 * booking, shop discounts and more... Your Login Details Your Membership
	 * Number Enter your membership number Password Enter your password Remember
	 * Me Forgot your password? Haven't registered yet?
	 */
	public static void main(String[] args) throws IOException {
		String url = "http://afamily.vn/vu-chim-tau-o-da-nang-nguoi-than-khoc-ngat-khi-ca-3-thi-the-duoc-vot-len-20160605042250294.chn";
//		String url = "<p align=\"right\">Theo <span style=\"FONT-WEIGHT: bold\">Linh Anh</span><br>";
		List<String> strings = getStringsFromUrl(url);
		for (String string : strings) {
			System.out.println(string);
		}
	}

	public static List<String> getStringsFromUrl(String html) throws IOException {
		Document document = Jsoup.parse(html);
		Elements elements = StringUtil.isBlank(null) ? document.getElementsByTag("body")
				: document.select(null);

		List<String> strings = new ArrayList<String>();
		elements.traverse(new TextNodeExtractor(strings));
		return strings;
	}

	private static class TextNodeExtractor implements NodeVisitor {
		private final List<String> strings;

		public TextNodeExtractor(List<String> strings) {
			this.strings = strings;
		}

		public void head(Node node, int depth) {
			if (node instanceof TextNode) {
				TextNode textNode = ((TextNode) node);
				String text = textNode.getWholeText();
				if (!StringUtil.isBlank(text)) {
					strings.add(text);
				}
			}
		}

		public void tail(Node arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

	}
}