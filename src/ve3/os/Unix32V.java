package ve3.os;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ve3.hdw.Cpu;
import ve3.hdw.Memory;
import ve3.main.Context;

public class Unix32V implements Cloneable {
	
	private Context ctx;
	private Memory memory;
	private byte[] rawmem;
	private Cpu cpu;
	private int[] reg;
	private long end;
	private String vaxRoot;
	
	private long sigp;
	private Map<Integer, Long> sigmap;
	private int pid;
	
	private boolean debug;
	private VFSystem vfs;
	
	private Context childCtx;
	private boolean share_fs; // if child process share VFSystem or not
	
	public Unix32V() {
		this.debug = false;
		sigmap = new HashMap<Integer, Long>();
		vfs = new VFSystem();
		childCtx = null;
		share_fs = false;
	}
	/*
	public Unix32V(Cpu cpu, Memory memory, String vaxRoot) {
		this.cpu = cpu;
		this.vaxRoot = vaxRoot;
		this.reg = cpu.getRegister();
		this.memory = memory;
		this.rawmem = memory.getRawMemory();
		this.debug = false;
		this.end = memory.getEOH();
		
		sigmap = new HashMap<Integer, Long>();
		vfs = new VFSystem();
		//pid = 0;
		//pid = 10000; // for test
	}
	*/
	
	public void setContext(Context ctx) {
		this.ctx = ctx;
	}	
	public Context getContext() {
		return ctx;
	}
	
	public void setShareFs(boolean share_fs) {
		this.share_fs = share_fs;
	}
	
	public void setCpu(Cpu cpu) {
		this.cpu = cpu;
		this.reg = cpu.getRegister();
	}
	
	public void setMemory(Memory memory) {
		this.memory = memory;
		this.rawmem = memory.getRawMemory();
		this.end = memory.getEOH();
	}
	
	public void setVaxRoot(String vaxRoot) {
		this.vaxRoot = vaxRoot;
	}
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public void setPid(int pid) {
		this.pid = pid;
	}
	public int getPid() {
		return pid;
	}
	
	public Map<Integer, String> createSymbolTable(int offset, byte[] rawdata) {
		Map<Integer, String> symTable = new HashMap<Integer, String>();
		byte[] name = new byte[8];
		for (int i = offset; i < rawdata.length; i += 16) {
			System.arraycopy(rawdata, i, name, 0, 8);
			int type = (rawdata[i + 8] & 0xff) | (rawdata[i + 9] & 0xff) << 8 |
					(rawdata[i + 10] & 0xff) << 16 |  (rawdata[i + 11] & 0xff) << 24;
			
			int addr = (rawdata[i + 12] & 0xff) | (rawdata[i + 13] & 0xff) << 8 |
					(rawdata[i + 14] & 0xff) << 16 |  (rawdata[i + 15] & 0xff) << 24;
			if (type > 0xff) { // true?
				int len = 8;
				for (int j = 0; j < 8; ++j) {
					if (name[j] == 0) {
						len = j;
						break;
					}
				}
				symTable.put(addr, new String(name, 0, len));
			}
			
		}
		return symTable;		
	}
	
	private String convertPath(final String path) {
		if (path.startsWith("/tmp")) {			
			return path;
		}
		
		if (path.startsWith("/")) {
			return vaxRoot + path;
		} else {
			return path;
		}
	}
	
	public void processArgs(List<String> argList, List<String> envList) {
		int nc = 0, na = argList.size(), ne = envList.size();
		int ucp, ucp2, ap;
		for (String s : argList) {
			nc += s.length();
			++nc;
		}
		for (String s : envList) {
			nc += s.length();
			++nc;
		}
		nc = (nc + 3) & ~3; // align
		
		//System.out.printf("nc = %d\n", nc);
		//System.out.printf("0x%x\n", reg[cpu.sp]);
		
		na += ne;
		ucp2 = ucp = reg[cpu.sp] - nc - 4;
		ap = ucp - (na * 4) - (3 * 4);
		reg[cpu.sp] = ap;
		
		//System.out.printf("0x%x\n", reg[cpu.sp]);
		
		memory.writeInt(ap, na-ne);
		ap += 4;
		for (String s : argList) {
			memory.load(s.getBytes(), 0, ucp, s.length());
			memory.writeInt(ap, ucp); // write pointer to arg
			ap += 4;
			ucp += s.length();
			memory.writeByte(ucp, (byte)0);
			++ucp;
		}
		memory.writeInt(ap, 0);
		ap += 4;
		for (String s : envList) {
			memory.load(s.getBytes(), 0, ucp, s.length());
			memory.writeInt(ap, ucp); // write pointer to env
			ap += 4;
			ucp += s.length();
			memory.writeByte(ucp, (byte)0);
			++ucp;
		}
		memory.writeInt(ap, 0);
		memory.writeInt(ucp, 0);
		//memory.dump(ucp2, 0x100000 - ucp2);
		//memory.dump(reg[cpu.sp], 0x100000 - reg[cpu.sp]); // show memory
		
	}
	
	public int syscall(int sysnum) {
		switch (sysnum) {
		case 1: { // exit
			int argnum = memory.readInt(reg[Cpu.ap]);
			int exnum = memory.readInt(reg[Cpu.ap] + 4);
			if (debug) {
				System.out.printf("<exit(%d)>\n", exnum);
			}
			/*
			System.out.println("-- exit--");
			System.out.println("argnum = " + argnum);
			System.out.println("ext = " + exnum);
			*/
			if (ctx.hasParent()) return -1;
			
			System.exit(exnum);			
			break;
		}
		case 2: { // fork
			int argnum = memory.readInt(reg[Cpu.ap]);
			//System.out.printf("argnum = %d\n", argnum);
			
			// for child process
			reg[Cpu.r0] = ctx.getPid();
			reg[Cpu.r1] = 1;					
			cpu.clearCarry();
			
			//System.out.printf("parent pid = %d\n", ctx.getPid());
			
			childCtx = ctx.clone();
			int cpid = childCtx.getPid();
			
			if (debug) {
				System.out.printf("<fork() => %d>\n", cpid);
			}
			// for parent process
			reg[Cpu.r0] = cpid;
			reg[Cpu.r1] = 0;
			
			//System.exit(1);
			break;
		}
		case 3: { // read
			int argnum = memory.readInt(reg[Cpu.ap]);
			int fd = memory.readInt(reg[Cpu.ap] + 4);
			int addr = memory.readInt(reg[Cpu.ap] + 8);
			int len = memory.readInt(reg[Cpu.ap] + 12);
			
			/*
			System.out.println("argnum = " + argnum);
			System.out.println("fnum = " + fd);
			System.out.printf("addr = %x\n",  addr);
			System.out.println("len = " + len);
			*/
			
			//memory.dump(addr, 10);			
			//int rlen = FSystem.read(fd, rawmem, addr, len);			
			int rlen = vfs.read(fd, rawmem, addr, len);			
			//memory.dump(addr, 10);
			
			if (debug) {
				System.out.printf("<read(%d, 0x%x, %d) => %d>\n", fd, addr, len, rlen);
			}
			
			if (rlen != -1) {
				reg[Cpu.r0] = rlen;
				cpu.clearCarry();
			} else {
				reg[Cpu.r0] = 1; // should be fix soon as ERROR
				cpu.setCarry();
			}
						
//			System.exit(1);
			break;
		}
		case 4: { // write
			int argnum = memory.readInt(reg[Cpu.ap]);
			int dst = memory.readInt(reg[Cpu.ap] + 4);
			int off = memory.readInt(reg[Cpu.ap] + 8);
			int len = memory.readInt(reg[Cpu.ap] + 12);

			/*
			System.out.println("argnum = " + argnum);
			System.out.println("dst = " + dst);
			System.out.println("off = " + off);
			System.out.println("len = " + len);
			*/
			if (debug) {
				System.out.printf("<write(%x, 0x%x, %d)", dst, off, len);
			}
			
			//System.out.write(rawmem, off, len);
			//int rlen = FSystem.write(rawmem, dst, off, len);
			int rlen = vfs.write(rawmem, dst, off, len);
			
			if (debug) {
				System.out.printf(" => %d>\n", rlen);
			}
			
			if (rlen != -1) {
				reg[Cpu.r0] = len;
				cpu.clearCarry();
			} else {
				reg[Cpu.r0] = 1; // should be fix soon as ERROR
				cpu.setCarry();
			}
			
			break;
		}
		case 5: { // open
			int argnum = memory.readInt(reg[Cpu.ap]);
			int filep = memory.readInt(reg[Cpu.ap] + 4);
			int mode = memory.readInt(reg[Cpu.ap] + 8);
			
			//System.out.printf("argnum = %x, filep = %x, mode = %x\n", argnum, filep, mode);
			int pos = memory.seekZero(filep);
			//System.out.printf("pos = %x, len = %d\n", pos, pos - filep);
			String fileName = new String(memory.rawRead(filep, (pos - filep)));
			String newPath = convertPath(fileName);
			

			
			
			//int fd = FSystem.open(newPath, mode);
			int fd = vfs.open(newPath, mode);
			//System.out.println("fnum = " + fnum);
			
			if (fd == -1) {
				reg[Cpu.r0] = fd;
				cpu.setCarry();
			} else {			
				reg[Cpu.r0] = fd;
				cpu.clearCarry();
			}
			
			if (debug) {
				System.out.printf("<open(0x%x, %d) => %d>\n", filep, mode, fd);
				System.out.println("fileName = " + fileName);
				System.out.println("convert path = " + newPath);
			}
			
						
			break;
		}
		case 6: { // close
			int argnum = memory.readInt(reg[Cpu.ap]);
			int fd = memory.readInt(reg[Cpu.ap] + 4);
			
			
			//System.out.println("argnum = " + argnum);
			//System.out.println("fd     = " + fd);
			//int r = FSystem.close(fd);
			
			int r = 0;
			if (!share_fs) {
				r = vfs.close(fd);	
				
				//System.out.println("real close");
				//System.exit(1);
			}
						
			
			if (debug) {
				System.out.printf("<close(%d) => %d>\n", fd, r);				
			}
			//System.exit(1);
			reg[Cpu.r0] = 0;
			cpu.clearCarry();		
			
			break;
		}
		case 7: { // wait
			int argnum = memory.readInt(reg[Cpu.ap]);
			int saddr = memory.readInt(reg[Cpu.ap] + 4); // status address
			//System.out.printf("argnum = %d, saddr = %x\n", argnum, saddr);
			
			if (debug) {
				System.out.println("<wait()>");
			}
			
			if (childCtx != null) {
				int cpid = childCtx.getPid();
				//System.out.println("child pid = " + cpid);
				childCtx.start();
				//memory.dump(saddr, 4);
				
				if (saddr != 0) {
					memory.writeInt(saddr, 0); // write status
				}
				
				if (debug) {
					System.out.printf("<wait() => %d, 0x%x>\n", cpid, 0);
				}
				reg[Cpu.r0] = cpid;
				cpu.clearCarry();
			}
			
			cpu.clearCarry();
			
			//System.exit(1);
			break;
		}
		case 8: { // creat
			int argnum = memory.readInt(reg[Cpu.ap]);
			int filep = memory.readInt(reg[Cpu.ap] + 4);
			int mode = memory.readInt(reg[Cpu.ap] + 8);
			//System.out.printf("argnum = %d, filep = %x, mode = %o\n", argnum, filep, mode);
			int pos = memory.seekZero(filep);
			String fileName = new String(memory.rawRead(filep, (pos - filep)));
			String newPath = convertPath(fileName);
			//System.out.println("fileName = " + fileName);
			
			//int fd = FSystem.creat(fileName, mode);
			int fd = vfs.creat(newPath, mode);

			if (fd == -1) {
				reg[Cpu.r0] = fd;
				cpu.setCarry();
			} else {			
				reg[Cpu.r0] = fd;
				cpu.clearCarry();
			}
			
			if (debug) {
				System.out.printf("<creat(0x%x, %04o) => %d>\n", filep, mode, fd);
				System.out.println("fileName = " + newPath);
				System.out.println("mode = " + mode);
			}
			
			break;
		}
		case 0x9: { // link
			int argnum = memory.readInt(reg[Cpu.ap]);
			int fp1 = memory.readInt(reg[Cpu.ap] + 4);
			int fp2 = memory.readInt(reg[Cpu.ap] + 8);
			int pos = memory.seekZero(fp1);
			String orgFile = new String(memory.rawRead(fp1, (pos - fp1)));
			pos = memory.seekZero(fp2);
			String newFile = new String(memory.rawRead(fp2, (pos - fp2)));
			
			File of = new File(convertPath(orgFile));
			File nf = new File(convertPath(newFile));
			
			int r = vfs.link(of, nf);
			if (debug) {
				System.out.printf("<link(0x%x, 0x%x) => %d>\n", fp1, fp2, r);
				System.out.println("orgFile = " + orgFile);
				System.out.println("newFile = " + newFile);
			}						
			//System.exit(1);
			break;
		}
		case 0xa: { // unlink
			int argnum = memory.readInt(reg[Cpu.ap]);
			int filep = memory.readInt(reg[Cpu.ap] + 4);
			int pos = memory.seekZero(filep);
			String fileName = new String(memory.rawRead(filep, (pos - filep)));			
			String newPath = convertPath(fileName);
			int r = 0;
			
			if (Files.exists(Paths.get(newPath)) == true) {
				try {
					Files.delete(Paths.get(newPath));
				} catch (Exception e) {
					r = -1;
					//System.out.println("hogehoge");
					//System.out.println("file = " + fileName);
					//System.out.println("newPath = " + newPath);
					//e.printStackTrace();
					//System.exit(1);
				}
			}
			
			reg[Cpu.r0] = r;
			if (r == 0) {
				cpu.clearCarry();
			} else {
				cpu.setCarry();
			}
			
			if (debug) {
				System.out.printf("<unlink(0x%x) => %d>\n", filep, r);
				System.out.println("fileName = " + newPath);
			}
						
			break;
		}
		case 0xf: { // chmod
			int argnum = memory.readInt(reg[Cpu.ap]);			
			int filep = memory.readInt(reg[Cpu.ap] + 4);
			int mode = memory.readInt(reg[Cpu.ap] + 8);
			//System.out.printf("argnum = %d, filep = %x, mode = %o\n", argnum, filep, mode);
			int pos = memory.seekZero(filep);
			String fileName = new String(memory.rawRead(filep, (pos - filep)));
			String newPath = convertPath(fileName);
			int r = vfs.chmod(newPath, mode);
			
			if (debug) {
				System.out.printf("<creat(0x%x, %04o) => %d>\n", filep, mode, r);
				System.out.println("fileName = " + newPath);
				System.out.println("mode = " + mode);				
			}
			
			reg[Cpu.r0] = r;
			if (r == 0) {
				cpu.clearCarry();
			} else {
				cpu.setCarry();
			}
						
			break;
			
		}
		case 0x11: { // sbrk
			int argnum = memory.readInt(reg[Cpu.ap]);
			int limit = memory.readInt(reg[Cpu.ap] + 4);
			int size = (int)(limit - end);
			
			//System.out.printf("limit = %x, end = %x\n", limit, end);
			
			end = limit;
			
			if (debug) {
				System.out.printf("<sbrk(%x) => 0x%x>\n", size, size);
			
			}
			reg[Cpu.r0] = (int)size;
			cpu.clearCarry();
			//System.exit(1);
			break;
		}
		case 0x12: { // stat
			int argnum = memory.readInt(reg[Cpu.ap]);
			int filep = memory.readInt(reg[Cpu.ap] + 4);
			int addr = memory.readInt(reg[Cpu.ap] + 8);
			
			//System.out.printf("argnum = %x, filep = %x, addr = %x\n", argnum, filep, addr);
			int pos = memory.seekZero(filep);
			String fileName = new String(memory.rawRead(filep, (pos - filep)));
			//System.out.println("fileName = " + fileName);			
			Stat st = new Stat();
			//FSystem.stat(fileName, st);
			vfs.stat(fileName, st);
			
			/*
			System.out.printf("dev    = %x\n", (short)(st.dev >> 16));
			System.out.printf("inode  = %x\n", (short)(st.inode >> 16));
			System.out.printf("permit = %x\n", (short)st.permission);
			System.out.printf("link   = %x\n", (short)st.link);
			System.out.printf("uid    = %x\n", (short)st.uid);
			System.out.printf("gid    = %x\n", (short)st.gid);
			System.out.printf("rdev   = %x\n", (short)st.rdev);
			System.out.printf("size   = %x\n", st.size);
			System.out.printf("atime  = %x\n", st.atime);
			System.out.printf("mtime  = %x\n", st.mtime);
			System.out.printf("ctime  = %x\n", st.ctime);
			*/
			//memory.dump(addr, 4);
			memory.writeShort(addr, (short)(st.dev >> 16));
			addr += 2;
			memory.writeShort(addr, (short)(st.inode >> 16));
			addr += 2;
			memory.writeShort(addr, (short)(st.permission));
			addr += 2;
			memory.writeShort(addr, (short)(st.link));
			addr += 2;
			memory.writeShort(addr, (short)(st.uid));
			addr += 2;
			memory.writeShort(addr, (short)(st.gid));
			addr += 2;
			memory.writeShort(addr, (short)(st.rdev));
			addr += 2;
			addr = (addr + 3) & ~3;
			memory.writeInt(addr, st.size);
			addr += 4;
			memory.writeInt(addr, st.atime);
			addr += 4;
			memory.writeInt(addr, st.mtime);
			addr += 4;
			memory.writeInt(addr, st.ctime);			
			//memory.dump(addr-26, 30);			
			
			reg[Cpu.r0] = 0;
			cpu.clearCarry();			
			break;
		}
		case 0x13: { // lseek
			int argnum = memory.readInt(reg[Cpu.ap]);
			int fd = memory.readInt(reg[Cpu.ap] + 4);
			int off = memory.readInt(reg[Cpu.ap] + 8);
			int mode = memory.readInt(reg[Cpu.ap] + 12);
			
			//long noff = FSystem.lseek(fd, off, mode);
			long noff = vfs.lseek(fd, off, mode);
			
			if (debug) {
				System.out.printf("<lseek(%x, 0x%x, %d) = %x>\n", fd, off, mode, noff);
			}
			reg[Cpu.r0] = (int)noff;
			cpu.clearCarry();			
			break;
			
		}
		case 0x14: { // getpid
			int argnum = memory.readInt(reg[Cpu.ap]);			
			if (pid == 0) {
				pid = (new Random().nextInt(0x3fffffff)) >> 16;
				//System.out.println("pid = " + pid);				
			}
			
			if (debug) {
				System.out.printf("<getpid() = %d>\n", pid);
			}
			
			reg[Cpu.r0] = pid;
			cpu.clearCarry();			
			break;
		}
		case 0x21: { // access
			int argnum = memory.readInt(reg[Cpu.ap]);
			int filep = memory.readInt(reg[Cpu.ap] + 4);
			int mode = memory.readInt(reg[Cpu.ap] + 8);
						
			int pos = memory.seekZero(filep);
			String fileName = new String(memory.rawRead(filep, (pos - filep)));
			String newPath = convertPath(fileName);
			
			int r;						
			switch (mode) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7: {
				r = Files.exists(Paths.get(newPath)) == true ? 0 : -1;
				break;
			}				
			default: { // 
				System.err.println("unsupported mode in access");
				throw new RuntimeException();				
			}				
			}
			
			if (debug) {
				System.out.printf("<access(0x%x, %d) = %d>\n", filep, mode, r);
			}
						
			if ((reg[Cpu.r0] = r) == 0) {
				cpu.clearCarry();
			} else {
				cpu.setCarry();
			}					
			break;
		}
		case 0x30: { // signal
			int argnum = memory.readInt(reg[Cpu.ap]);
			int type = memory.readInt(reg[Cpu.ap] + 4);
			int addr = memory.readInt(reg[Cpu.ap] + 8);
			long raddr = 0;
			//System.out.printf("argnum = %x, type = %x, addr = %x\n", argnum, type, addr);
			if (sigmap.containsKey(type)) {
				raddr = sigmap.get(type);			
			} 
			sigmap.put(type, addr & 0xffffffffL);
			
			if (debug) {
				System.out.printf("<signal(%x, 0x%x) = 0x%x>\n", type, addr, raddr);
			}
			
			reg[Cpu.r0] = (int)raddr;
			cpu.clearCarry();			
			//System.exit(1);
			break;
		}
		case 0x36: { // ioctl
			int argnum = memory.readInt(reg[Cpu.ap]);
			int fd = memory.readInt(reg[Cpu.ap] + 4);
			int req = memory.readInt(reg[Cpu.ap] + 8);
			int addr = memory.readInt(reg[Cpu.ap] + 12);
			
			if (debug) {
				System.out.printf("<ioctl(%d, 0x%x, 0x%x)>\n", fd, req, addr);
			}
			/*
			System.out.println("argnum = " + argnum);
			System.out.printf("fd   = %x\n", fd);
			System.out.printf("req  = %x\n", req);
			System.out.printf("addr = %x\n", addr);
			*/
			cpu.clearCarry(); // meaning success
			reg[Cpu.r0] = 0;
			
			//System.exit(1);
			break;
		}
		case 0x3b: { // exece
			int argnum = memory.readInt(reg[Cpu.ap]);
			int filep = memory.readInt(reg[Cpu.ap] + 4);
			int argaddr = memory.readInt(reg[Cpu.ap] + 8);
			int envaddr = memory.readInt(reg[Cpu.ap] + 12);
			
			System.out.printf("argnum = %x\n", argnum);
			System.out.printf("argaddr = %x\n", argaddr);
			System.out.printf("envaddr = %x\n", envaddr);
			int pos = memory.seekZero(filep);
			String fileName = new String(memory.rawRead(filep, (pos - filep)));
			System.out.println("fileName = " + fileName);
			
			for (int i = argaddr;; i += 4) {
				int argp = memory.readInt(i);
				System.out.printf("argp = %x\n", argp);
				if (argp == 0) break;				
				pos = memory.seekZero(argp);
				String argName = new String(memory.rawRead(argp, (pos - argp)));
				System.out.println("argName = " + argName);				
			}
			
			for (int i = envaddr;; i += 4) {
				int envp = memory.readInt(i);
				System.out.printf("envp = %x\n", envp);
				if (envp == 0) break;
				pos = memory.seekZero(envp);
				String envName = new String(memory.rawRead(envp, (pos - envp)));
				System.out.println("envName = " + envName);
			}
			
			
			System.exit(1);
			break;
		}
		default: {
			System.out.println("unsupported syscam call number :" + sysnum);
			System.exit(1);
		}
		}
		
		return 0;
	}
	

	@Override
	public Unix32V clone() {
		Unix32V v32 = null;
		
		try {
			v32 = (Unix32V)super.clone();
			v32.cpu = null;
			v32.memory = null;
			v32.reg = null;
			v32.rawmem = null;
			v32.sigmap = new HashMap<Integer, Long>();
			for (Map.Entry<Integer, Long> e : this.sigmap.entrySet()) {
				sigmap.put(e.getKey(), e.getValue());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		
		return v32;
	}
}
