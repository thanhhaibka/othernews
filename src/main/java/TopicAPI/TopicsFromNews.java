package TopicAPI;

import org.rapidoid.commons.MediaType;
import org.rapidoid.http.Req;
import org.rapidoid.http.Resp;
import org.rapidoid.http.fast.ReqHandler;

import vcc.optimization.othernews.findtopic.InferDocument;

public class TopicsFromNews implements ReqHandler{
	public Object handle(Req req) throws Exception {
		Resp resp = req.response();

		// STEP 1. SERVICE MONITOR
		// set max request per second
		if (ServerMonitor.NUM_REQEUST > 100000) {
			resp.code(429);
			return resp;
		}
		// Monitor service
		ServerMonitor.NUM_REQEUST++;
		// verify input
		if (req.param("u") == null) {
			resp.code(400);
			return resp;
		}
		String guid = "";
		try {
			guid = req.param("u");
		} catch (Exception e) {
			resp.code(400);
			return resp;
		}

		// STEP 3. CONTENT BUID
		resp.contentType(MediaType.TEXT_PLAIN_UTF8);
		resp.content(InferDocument.getInstance().getTopicsJSONFromContent(guid).toString(4));
//		resp.content("k14_osite = "+new ShRtg().getJSONListNewsFromRtgAPI(NewsUpdate.apiTop20SohaNewsID).toString(4));
		return resp;
		
	}

}
