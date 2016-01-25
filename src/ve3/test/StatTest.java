package ve3.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import ve3.os.FSystem;
import ve3.os.Stat;



public class StatTest {
	
	public static void main(String ...args) throws Exception {
		//Process p = Runtime.getRuntime().exec("stat -f dev=%d i=%i per=%p l=%l u=%u g=%g size=%z r=%r a=%a m=%m c=%c nm");
		//Process p = Runtime.getRuntime().exec("stat -f dev=%d i=%i per=%p l=%l u=%u g=%g size=%z r=%r a=%a m=%m c=%c nm");
		//Process p = Runtime.getRuntime().exec("stat -f dev=%d nm");
		Process p = Runtime.getRuntime().exec(new String[]{"stat", "-f dev=%d i=%i per=%p l=%l u=%u g=%g size=%z r=%r a=%a m=%m c=%c", "nm"});
				
		//Process p = Runtime.getRuntime().exec("stat -f dev=%d nm");
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		System.out.println(reader.readLine());
		
		p = Runtime.getRuntime().exec("stat -f i=%i nm");
		reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		System.out.println(reader.readLine());
		
		p = Runtime.getRuntime().exec("stat -f p=%p nm");
		reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String permission = reader.readLine();
		System.out.println(permission);
		String[] pairs = permission.split("=");
		System.out.println(pairs[1]);
		int sum = 0;
		System.out.println("len = " + pairs[1].length());
		for (int i = pairs[1].length() - 1, j = 0; i >= 0; --i, ++j) {
			System.out.println(pairs[1].charAt(i));
			sum += Math.pow(8, j) * (pairs[1].charAt(i) - 0x30);
			System.out.println("sum = " + sum);
		}

		
		
		p = Runtime.getRuntime().exec("stat -f l=%l nm");
		reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		System.out.println(reader.readLine());
		
		p = Runtime.getRuntime().exec("stat -f u=%u nm");
		reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		System.out.println(reader.readLine());
		
		p = Runtime.getRuntime().exec("stat -f g=%g nm");
		reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		System.out.println(reader.readLine());
		
		p = Runtime.getRuntime().exec("stat -f s=%z nm");
		reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		System.out.println(reader.readLine());
		
		p = Runtime.getRuntime().exec("stat -f r=%r nm");
		reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		System.out.println(reader.readLine());
		
		p = Runtime.getRuntime().exec("stat -f a=%a nm");
		reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		System.out.println(reader.readLine());
		
		p = Runtime.getRuntime().exec("stat -f m=%m nm");
		reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		System.out.println(reader.readLine());
		
		p = Runtime.getRuntime().exec("stat -f c=%c nm");
		reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		System.out.println(reader.readLine());
		
		Stat st = new Stat();
		FSystem.stat("nm", st);
		System.out.println(st.size);
		System.out.println(st.permission);
		
		
	}

}
