package ve3.os;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import ve3.util.RuntimeUtil;

public class VFSystem {
	
	private static Map<Integer, VFile> nodeMap;

	static {				
		nodeMap = new HashMap<Integer, VFile>();
		nodeMap.put(0, new VFile(System.in));
		nodeMap.put(1, new VFile(System.out));
		nodeMap.put(2, new VFile(System.err));	
	}
	
	public static final int getAvailable() {
		int fnum = 0;
		for (;; ++fnum) {
			if (!nodeMap.containsKey(fnum)) return fnum;
		}
	}
	
	public static int open(String fname, int mode) {
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
			System.err.println("Unsupported open mode in VFSystem:" + mode);
			System.exit(1);
		}
		}
		
		int fnum = getAvailable();
		
		try {
			VFile vf = new VFile(fname);
			if (!vf.exists()) {
				return -1;
			}
			vf.open(fnum, smode);
			nodeMap.put(fnum, vf);
			return fnum;			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		}		
	}
	
	public static int creat(String fname, int mode) {
		int fd = -1;		
		try {
			VFile vf = new VFile(fname);		
			if (!vf.exists()) {
				vf.creat();
				doChmod(vf.getPath(), mode);
			}
			fd = open(fname, 1);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}				
		return fd;
	}
	
	public static int write(byte[] src, int fd, int off, int len) {
		VFile vf = nodeMap.get(fd);
		if (vf == null) {
			System.err.println("Cannot find target file " + fd);
			return -1;
		}		
		return vf.write(src, off, len);
	}
	
	public static int read(int fd, byte[] dst, int off, int len) {
		VFile vf = nodeMap.get(fd);
		if (vf == null) {
			System.err.println("Cannot find target file " + fd);
			return -1;
		}
		
		return vf.read(dst, off, len);
	}
	
	private static void doChmod(final String path, int mode) {
		String modes = "";
		for (; mode != 0; mode /= 8) {
			char c = (char)((mode % 8) + 0x30);			
			modes = c + modes;
		}
		RuntimeUtil.exec("chmod", modes, path);
	}
	
	public static int close(int fd) {
		VFile vf = nodeMap.remove(fd);
		if (vf == null) {
			System.err.println("Cannot find target file " + fd);
		}
		return vf.close();
	}
	
	public static long lseek(int fd, int offset, int mode) {
		VFile vf = nodeMap.get(fd);
		if (vf == null) {
			System.err.println("Cannot find target file " + fd);
			return -1;
		}
		
		return vf.lseek(offset, mode);
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