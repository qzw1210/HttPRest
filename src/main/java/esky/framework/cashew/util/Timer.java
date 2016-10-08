package esky.framework.cashew.util;


public class Timer {
	
	private static long startTime;
	
	public Timer() {
		startTime = System.currentTimeMillis();
	}
	
	public void reStart() {
		startTime = System.currentTimeMillis();
	}
	
	public long end () {
		return System.currentTimeMillis() - startTime;
	}
}
