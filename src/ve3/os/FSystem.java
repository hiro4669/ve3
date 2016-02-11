package ve3.os;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ve3.util.RuntimeUtil;

public class FSystem {
	
	private static final int MIN_NUM = 3;
	private static int current = MIN_NUM;
	private static Map<Integer, RandomAccessFile> nodeMap = new HashMap<Integer, RandomAccessFile>();
	private static boolean[] reserved = {true, true, true};
	private final static int stdin  = 0;
	private final static int stdout = 1;
	private final static int stderr = 2;

	
	private static final int getAvailable() {
		// for stdin/stdout/stderr
		for (int i = 0; i < reserved.length; ++i) {
			if (!reserved[i]) return i;
		}
		
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
			smode = "rw";
			break;
		}
		default: {
			System.err.println("Unsupported open mode in FSystem:" + mode);
			System.exit(1);
		}
		}				
		int fnum = getAvailable();
					
		try {			
			File f = new File(file);
			if (!f.exists()) {				
				return -1;
			}			
			
			nodeMap.put(fnum, new RandomAccessFile(file, smode));
			return fnum;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public static int close(int fnum) {	
		
		switch (fnum) {
		case FSystem.stdin:
		case FSystem.stdout:
		case FSystem.stderr: {
			if (reserved[fnum]) {
				reserved[fnum] = false;
				return 0;
			} else {
				RandomAccessFile rfile = nodeMap.remove(fnum);
				try {
					if (rfile != null) { 
						rfile.close();
						reserved[fnum] = true;
						return 0;
					}		
				} catch (Exception e) {					
					return -1;
				}
			}			
		}
		}
		
		RandomAccessFile rfile = nodeMap.remove(fnum);		
		try {
			if (rfile != null) { rfile.close(); }		
		} catch (Exception e) {
			return -1;
		}
		return 0;
	}
	
	private static int doWrite(byte[] src, int fnum, int off, int len) {
		RandomAccessFile rfile = nodeMap.get(fnum);
		if (rfile == null) {
			System.err.println("Cannot find target file " + fnum);				
			return -1;
		}			
		try {
			rfile.write(src, off, len);
			return len;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public static int write(byte[] src, int fnum, int off, int len) {
		switch (fnum) {
		case FSystem.stdin: {
			if (reserved[fnum]) {
				throw new RuntimeException();
			} else {
				return doWrite(src, fnum, off, len);				
			}			
		}
		case 1: {
			if (reserved[fnum]) {
				System.out.write(src, off, len);
				return len;
			} else {
				return doWrite(src, fnum, off, len);
			}			
		}
		case 2: {
			if (reserved[fnum]) {							
				System.err.write(src, off, len);
				return len;		
			} else {
				return doWrite(src, fnum, off, len);				
			}
		}
		default: {
			return doWrite(src, fnum, off, len);
		}
		}
				
	}
	
	private static int doRead(int fnum, byte[] dst, int off, int len) {
		RandomAccessFile rfile = nodeMap.get(fnum);
		if (rfile == null) {
			System.err.println("Cannot find target file " + fnum);
			System.exit(1);
		}
		try {
			int num = rfile.read(dst, off, len);
			return num;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}	
	}
	
	
	public static int read(int fnum, byte[] dst, int off, int len) {
		switch (fnum) {
		case FSystem.stdin: {
			if (reserved[fnum]) {
				try {
					int r = System.in.read(dst, off, len);
					return r;
				} catch (IOException e) {
					e.printStackTrace();
					return -1;
				}
			} else {
				return doRead(fnum, dst, off, len);
			}
		}
		case FSystem.stdout:
		case FSystem.stderr: {
			if (reserved[fnum]) {
				System.err.println("cannot read from stdout/stderr");
				return -1;			
			} else {				
				return doRead(fnum, dst, off, len);
			}
		}
		default: {
			return doRead(fnum, dst, off, len);
		}			
		}
	}
	
	public static int creat(String fileName, int mode) {
		File f = new File(fileName);
		int fd = -1;
		if (!f.exists()) {
			try {
				f.createNewFile();
				doChmod(f.getAbsolutePath(), mode);															
			} catch (Exception e) {
				e.printStackTrace();
				return fd;
			}						
		}	
		fd = open(fileName, 1);
		return fd;
	}
	
	public static int chmod(String fileName, int mode) {
		File f = new File(fileName);
		if (f.exists()) {
			doChmod(f.getAbsolutePath(), mode);
			return 0;
		} 
		return -1;		
	}
	
	private static void doChmod(final String path, int mode) {
		String modes = "";
		for (; mode != 0; mode /= 8) {
			char c = (char)((mode % 8) + 0x30);			
			modes = c + modes;
		}
		RuntimeUtil.exec("chmod", modes, path);
	}
	
	public static long lseek(int fd, int offset, int mode) {
		switch (fd) {
		case FSystem.stdin:
		case FSystem.stdout:
		case FSystem.stderr: {
			if (reserved[fd]) throw new RuntimeException();					
		}
		}
		
		
		
		RandomAccessFile rfile = nodeMap.get(fd);
		try {
			long noffset = 0;
			switch(mode) {
			case 0:
				noffset = offset;
				break;
			case 1:
				noffset = rfile.getFilePointer() + offset;								
				break;
			case 2:
				noffset = rfile.length() + offset;
				break;
			default: {
				System.err.println("unsupported lseek mode in FSystem" + mode);
				System.exit(1);				
			}		
			}
			rfile.seek(noffset);
			
			return noffset;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}		
	}
	
	public static int stat(String fileName, Stat st) {
		String ret = RuntimeUtil.exec("stat", "-f d=%d i=%i p=%p l=%l u=%u g=%g s=%z r=%r a=%a m=%m c=%c", fileName).trim();
		String[] rets = ret.split(" ");
		for (String s : rets) {
			String[] pairs = s.split("=");
			int val = Integer.parseInt(pairs[1]);
			switch (pairs[0].charAt(0)) {
			case 'd': {
				st.dev = val;
				break;
			}
			case 'i': {
				st.inode = val;
				break;
			}
			case 'p': {
				int sum = 0;
				for (int i = pairs[1].length() - 1, j = 0; i >= 0; --i, ++j) {
					sum += Math.pow(8, j) * (pairs[1].charAt(i) - 0x30);
				}
				st.permission = sum;
				break;
			}
			case 'l': {
				st.link = val;
				break;
			}
			case 'u': {
				st.uid = val;
				break;
			}
			case 'g': {
				st.gid = val;
				break;
			}
			case 'r': {
				st.rdev = val;
				break;
			}
			case 's': {
				st.size = val;
				break;
			}
			case 'a': {
				st.atime = val;
				break;				
			}
			case 'm': {
				st.mtime = val;
				break;				
			}
			case 'c': {
				st.ctime = val;
				break;				
			}
			}			
		}
		return 0;
	}
	


}
