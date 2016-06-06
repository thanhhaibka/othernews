package TopicAPI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorMng implements Runnable {
	Logger logger = LoggerFactory.getLogger("ServiceINFO");

	public void run() {
		if (ServerMonitor.NUM_REQEUST > 0)
			System.out.print("Request per second : " + ServerMonitor.NUM_REQEUST + " with "
					+ ServerMonitor.NUM_EMPTY_ERROR_REQUEST + " ("
					+ (ServerMonitor.NUM_EMPTY_ERROR_REQUEST * 100 / ServerMonitor.NUM_REQEUST) + "%)");
		else
			System.out.print("Request per second : " + ServerMonitor.NUM_REQEUST);

		System.out.println(". Num thread = " + java.lang.Thread.activeCount());

		ServerMonitor.NUM_REQEUST = 0;
		ServerMonitor.NUM_EMPTY_ERROR_REQUEST = 0;

	}

}
