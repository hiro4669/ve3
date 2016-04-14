package ve3.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class RuntimeUtil {
	
	private static String process(String[] commands) {
		
		try {
			Process p = Runtime.getRuntime().exec(commands);
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			return reader.readLine();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}			
	}

	public static String exec(String command, String op) {
		String[] commands = new String[] {
				command,
				op				
		};
		
		return process(commands);
	}
	
	public static String exec(String command, String op1, String op2) {
		
		String[] commands = new String[] {
			command,
			op1,
			op2
		};		
		return process(commands);	
	}
}
