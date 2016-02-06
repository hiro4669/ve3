package ve3.os;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;

public class VFile {
	private final PrintStream out;
	private final InputStream in;
	private RandomAccessFile rfile = null;
	private final File file;
	private int count = 0;	
	
	public VFile(PrintStream out) {
		in = null;
		file = null;
		this.out = out;		
	}
	
	public VFile(InputStream in) {
		this.in = in;
		out = null;
		file = null;
	}
	
	public VFile(String path) throws FileNotFoundException {
		out = null;
		in = null;
		
		file = new File(path);
		count++;
	}
	
	public boolean exists() {
		if (file != null) {
			return file.exists();
		}
		throw new RuntimeException(); // this is never called
	}
	
	public int open(int fd, String smode) {
		if (file == null) return -1;
		try {
			rfile = new RandomAccessFile(file, smode);
			return fd;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public int creat() {
		if (!exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
				return -1;
			}			
		}		
		return 0;
	}
	
	public int read(byte[] dst, int off, int len) {
		try {
			int r;
			if (in != null) {
				r = in.read(dst, off, len);
				return r;
			}
			
			if (rfile == null) throw new RuntimeException();
			
			r = rfile.read(dst, off, len);
			return r;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public int write(byte[] src, int off, int len) {
		if (out != null) {
			out.write(src, off, len);
			return len;
		}
		
		if (rfile == null) throw new RuntimeException();
		try {
			rfile.write(src, off, len);
			return len;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}		
	}
	
	public String getPath() {
		if (file == null) throw new RuntimeException();		
		return file.getAbsolutePath();
	}
	
	
	
	public long lseek(int offset, int mode) {
		if (rfile == null) throw new RuntimeException();
		
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
			return -1;
		}		
	}
	
	public int close() {
		if (out != null) return 0;
		if (in  != null) return 0;
		
		
		if (--count == 0) {
			try {
				//System.out.println("real close");
				rfile.close();
				return 0;
			} catch (Exception e) {
				e.printStackTrace();
				return -1;
			}
		}
		return 0;
	}
	


}
