package TopicAPI;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import userretarget.VTsimilar.UserVertor;

public class MonitorMng implements Runnable {
	Logger logger = LoggerFactory.getLogger("ServiceINFO");
	private boolean checkHours = false;

	public void run() {

//		if (new Date().getHours() % 4 == 0 && checkHours == false) {
//			UserVertor.getInstance().getListNewsFromSQL();
//			System.out.println("Update List News");
//			checkHours = true;
//		}

		if (new Date().getHours() % 4 != 0 )
			checkHours = false;
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
