package org.delia.util;

public class Sleep {

    public static void safeSleep(int msecs) {
        try {
			Thread.sleep(msecs);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
}
