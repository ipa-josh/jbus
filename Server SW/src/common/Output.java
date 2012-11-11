package common;

public class Output {

	public static void warning(String s) {
		System.out.println("Warning: "+s);
	}

	public static void error(String s) {
		System.out.println("Error: "+s);
	}

	public static void error(Exception e) {
		e.printStackTrace();
	}
}
