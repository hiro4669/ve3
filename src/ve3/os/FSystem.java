package ve3.os;

import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FSystem {
	
	private static final int MIN_NUM = 3;
	private static int current = MIN_NUM;
	private static Map<Integer, RandomAccessFile> nodeMap = new HashMap<Integer, RandomAccessFile>();
	
	
	private static final int getAvailable() {
		int fnum = MIN_NUM;
		for (;;++fnum)
			if (!nodeMap.containsKey(fnum)) break;
		return fnum;
	}
	
	public static int open(String file, int mode) {
		String smode = "r";
		switch (mode) {
		case 0: {
			smode = "r";
			break;
		}
		case 1: {
			smode = "w";
			break;
		}
		default: {
			System.err.println("Unsupported open mode in FSystem:" + mode);
			System.exit(1);
		}
		}				
		int fnum = getAvailable();
		
		try {
			nodeMap.put(fnum, new RandomAccessFile(file, smode));
			return fnum;
		} catch (Exception e) {
			return -1;
		}
	}
	
	public static int close(int fnum) {
		RandomAccessFile rfile = nodeMap.remove(fnum);
		try {
			if (rfile != null) { rfile.close(); }		
		} catch (Exception e) {
			return -1;
		}
		return 0;
	}


}
