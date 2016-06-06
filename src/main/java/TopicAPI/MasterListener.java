package TopicAPI;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MasterListener {
	private ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(2);

	public void start() {
		System.out.println("Master update service started...");
		stpe.scheduleWithFixedDelay(new MonitorMng(), 0, 1, TimeUnit.SECONDS);
	}

	public void stop() {
		stpe.shutdown();
	}

}
