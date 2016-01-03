package ve3.main;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ve3.disassm.V32Disassm;

public class VE3 {
	
	public static void main(String ...args) {
		
		boolean disflg = false;
		boolean debug = false;
		boolean imode = false;
		
		String fileName = null;
		
		List<String> argList = new ArrayList<String>();
		for (int i = 0; i < args.length; ++i) {
			if (args[i].equals("-d")) {
				disflg = true;
			} else if (args[i].equals("-m")) {
				debug = true;
			} else if (args[i].equals("-i")) {
				imode = true;
			} else {
				argList.add(args[i]);
			}
		}
		if (argList.size() == 0) {
			throw new RuntimeException();
		}
		fileName = argList.get(0);
		/*
		if (args.length > 0 && !(args[args.length-1].startsWith("-"))) {
			fileName = args[args.length-1];
		}
		*/
		
//		if (args.length > 0) {
			//fileName = args[0];
		//}
		
		try {
			BufferedInputStream bin = new BufferedInputStream(new FileInputStream(fileName));
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			
			int count;
			byte[] buffer = new byte[1024];
			while ((count = bin.read(buffer, 0, buffer.length)) != -1) {
//				System.out.println("count = " + count);
				bout.write(buffer, 0, count);
			}		
			byte[] rawdata = bout.toByteArray();
			bout.close();
			bin.close();
			System.out.println("totalsize = " + rawdata.length);
			
			if (disflg) {
				V32Disassm disassm = new V32Disassm(rawdata);
				String log = disassm.disassm();
				return;
			}
				
			if (imode) {
				Context ctx = new Context(rawdata);
				ctx.startIMode();
			} else {
				Context ctx = new Context(rawdata, argList);
				ctx.setDebug(debug);
				ctx.start();
			}
			
		//	System.out.println("--------");
//			System.out.println(log);
		
		
		
		} catch (FileNotFoundException fe) {
			fe.printStackTrace();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
		
	}
	
	

}
