package edu.repetita.solvers.sr.cg4sr.threading;

import edu.repetita.io.RepetitaWriter;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Mathieu Jadin mathieu.jadin@uclouvain.be
 */
public class TimeoutThread extends Thread {

	static private final long TIMEOUT_ACCURACY = 100;

	static public AtomicBoolean timedOut = new AtomicBoolean(false);
	static public AtomicBoolean stopMonitoring = new AtomicBoolean(false);

	private long timeout;

	public TimeoutThread(long timeout) {
		this.timeout = timeout;
		TimeoutThread.timedOut.set(false);
	}

	public void run() {

		while (timeout > 0 && !stopMonitoring.get()) {

			long start_time  = System.currentTimeMillis();

			try {
				sleep(TIMEOUT_ACCURACY);
			} catch (InterruptedException e) {
				return;
			}

			long delta = (System.currentTimeMillis() - start_time);
			timeout -= delta;
		}

		if (!stopMonitoring.get()) {
			timedOut.set(true);
			RepetitaWriter.appendToOutput("Timeout reached !", 0);
		}
	}
}
