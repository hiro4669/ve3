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
		System.out.println(reader.readLine());
		
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
		
		
	}

}
