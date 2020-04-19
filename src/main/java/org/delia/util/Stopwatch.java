package org.delia.util;

import java.util.concurrent.TimeUnit;

public class Stopwatch {
	private long startTime = 0L;
	private long endTime = 0L;
	private long adjustment = 0L;
	
	public void start() {
		if (endTime != 0L && startTime != 0L) {
			adjustment += duration();
		}
//		startTime = new Date().getTime();
		startTime = System.nanoTime();
	}
	public void stop() {
//		this.endTime = new Date().getTime();
		this.endTime = System.nanoTime();
	}
	public long duration() {
		long tmp = adjustment + (endTime - startTime);
		return TimeUnit.NANOSECONDS.toMillis(tmp);
	}
	public long nanoDuration() {
		long tmp = adjustment + (endTime - startTime);
		return tmp;
	}

}
