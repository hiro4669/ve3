package ve3.os;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import ve3.util.RuntimeUtil;

public class VFSystem {
	
	private Map<Integer, VFile> nodeMap;
	
	static final int readmask  = 0x124;
	static final int writemask = 0x92;
	
	private final static int REGULAR     = 0x8000;
	private final static int DIRECTORY   = 0x4000;
	private final static int OWNER_READ  = 0x100;
	private final static int OWNER_WRITE = 0x80;
	private final static int OWNER_EXEC  = 0x40;
	private final static int GROUP_READ  = 0x20;
	private final static int GROUP_WRITE = 0x10;
	private final static int GROUP_EXEC  = 0x8;
	private final static int OTHER_READ  = 0x4;
	private final static int OTHER_WRITE = 0x2;
	private final static int OTHER_EXEC  = 0x1;
	
	public VFSystem() {
		nodeMap = new HashMap<Integer, VFile>();
		nodeMap.put(0, new VFile(System.in));
		nodeMap.put(1, new VFile(System.out));
		nodeMap.put(2, new VFile(System.err));	
	}
	
	public final int getAvailable() {
		int fnum = 0;
		for (;; ++fnum) {
			if (!nodeMap.containsKey(fnum)) return fnum;
		}
	}
	
	public int open(String fname, int mode) {
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
	
	public int creat(String fname, int mode) {
		int fd = -1;		
		try {
			VFile vf = new VFile(fname);		
			if (!vf.exists()) {
				vf.creat();
				doChmod(vf.getPath(), mode);
			}
			int fmode = -1;
			if ((mode & readmask) != 0) ++fmode;
			if ((mode & writemask) != 0) ++fmode;				
			fd = open(fname, fmode);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}				
		return fd;
	}
	
	public int chmod(String fileName, int mode) {
		File f = new File(fileName);
		if (f.exists()) {
			doChmod(f.getAbsolutePath(), mode);
			return 0;
		} 
		return -1;		
	}
	
	public int write(byte[] src, int fd, int off, int len) {
		VFile vf = nodeMap.get(fd);
		if (vf == null) {
			System.err.println("Cannot find target file " + fd);
			return -1;
		}		
		return vf.write(src, off, len);
	}
	
	public int read(int fd, byte[] dst, int off, int len) {
		VFile vf = nodeMap.get(fd);
		if (vf == null) {
			System.err.println("Cannot find target file " + fd);
			return -1;
		}
		
		return vf.read(dst, off, len);
	}
	
	private void doChmod(final String path, int mode) {
		String modes = "";
		for (; mode != 0; mode /= 8) {
			char c = (char)((mode % 8) + 0x30);			
			modes = c + modes;
		}
		RuntimeUtil.exec("chmod", modes, path);
	}
	
	public int close(int fd) {
		//System.out.println("call close");
		VFile vf = nodeMap.remove(fd);
		if (vf == null) {
			System.err.println("Cannot find target file " + fd);
			//System.exit(1);
			return 0;
		}
		return vf.close();
	}
	
	public long lseek(int fd, int offset, int mode) {
		VFile vf = nodeMap.get(fd);
		if (vf == null) {
			System.err.println("Cannot find target file " + fd);
			return -1;
		}
		
		return vf.lseek(offset, mode);
	}
	
	public int link(File of, File nf) {
		try {
			Files.createLink(nf.toPath(), of.toPath());
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}
	
	private static int createPermission(Path path) throws IOException {
		
		Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path);
		Iterator<PosixFilePermission> ite = permissions.iterator();
		int permission = 0;
		while (ite.hasNext()) {
			PosixFilePermission p = ite.next();
			switch (p) {
			case OWNER_READ: {
				permission += OWNER_READ;
				break;			
			}
			case OWNER_WRITE: {
				permission += OWNER_WRITE;		
				break;
			}
			case OWNER_EXECUTE : {
				permission += OWNER_EXEC;
				break;
			}
			case GROUP_READ: {
				permission += GROUP_READ;
				break;
			}
			case GROUP_WRITE: {
				permission += GROUP_WRITE;
				break;
			}
			case GROUP_EXECUTE: {
				permission += GROUP_EXEC;
				break;
			}
			case OTHERS_READ: {
				permission += OTHER_READ;
				break;
			}
			case OTHERS_WRITE: {
				permission += OTHER_WRITE;
				break;
			}
			case OTHERS_EXECUTE: {
				permission += OTHER_EXEC;
				break;
			}
			default: {
				break;
			}				
			}
		}
		
		
		if (Files.isRegularFile(path)) {
			permission += REGULAR;
			
		}
		if (Files.isDirectory(path)) {
			permission += DIRECTORY;
		}
		
		return permission;
	}
	
	public int dstat(String fileName, Stat st) {
		FileSystem fs = FileSystems.getDefault();
		Path path = fs.getPath(fileName);
		
		
		try {
			FileTime ftime = Files.getLastModifiedTime(path);
			
			st.dev = new Random().nextInt(30000000);
			st.inode = new Random().nextInt(30000000);
			st.permission = createPermission(path);
			st.link = new Random().nextInt(5);
			st.uid = 501;
			st.gid = 20;
			st.size = (int)Files.size(path);
			st.rdev = 0;
			st.atime = (int)(ftime.toMillis() / 1000);
			st.mtime = st.atime;
			st.ctime = st.atime;
			
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		
		return 0;
	}
	
	public int stat(String fileName, Stat st) {
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
