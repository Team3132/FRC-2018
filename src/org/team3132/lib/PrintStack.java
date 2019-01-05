package org.team3132.lib;

public class PrintStack {
	
	public static void trace() {
		try {
			throw new Exception();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
