package ve3.os;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ve3.hdw.Cpu;
import ve3.hdw.Memory;

public class Unix32V {
	
	private final Memory memory;
	private byte[] rawmem;
	private final Cpu cpu;
	private int[] reg;
	private long end;
	private final String vaxRoot;
	
	private boolean debug;
	
	public Unix32V(final Cpu cpu, final Memory memory, final String vaxRoot) {
		this.cpu = cpu;
		this.vaxRoot = vaxRoot;
		this.reg = cpu.getRegister();
		this.memory = memory;
		this.rawmem = memory.getRawMemory();
		this.debug = false;
		this.end = memory.getEOH();
	}
	
	public void setDebug(boolean debug) {
		this.debug = debug;
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
	
	public void syscall(int sysnum) {
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
			System.exit(exnum);
			
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
			int rlen = FSystem.read(fd, rawmem, addr, len);			
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
			int rlen = FSystem.write(rawmem, dst, off, len);
			
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
			

			
			
			int fd = FSystem.open(newPath, mode);
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
				//System.out.println("fileName = " + fileName);
				//System.out.println("convert path = " + newPath);
			}
			
						
			break;
		}
		case 6: { // close
			int argnum = memory.readInt(reg[Cpu.ap]);
			int fd = memory.readInt(reg[Cpu.ap] + 4);
			
			
			//System.out.println("argnum = " + argnum);
			//System.out.println("fd     = " + fd);
			int r = FSystem.close(fd);
			
			
			if (debug) {
				System.out.printf("<close(%d) => %d>\n", fd, r);				
			}
			//System.exit(1);
			reg[Cpu.r0] = 0;
			cpu.clearCarry();		
			
			break;
		}
		case 8: { // creat
			int argnum = memory.readInt(reg[Cpu.ap]);
			int filep = memory.readInt(reg[Cpu.ap] + 4);
			int mode = memory.readInt(reg[Cpu.ap] + 8);
			//System.out.printf("argnum = %d, filep = %x, mode = %o\n", argnum, filep, mode);
			int pos = memory.seekZero(filep);
			String fileName = new String(memory.rawRead(filep, (pos - filep)));
			//System.out.println("fileName = " + fileName);
			
			int fd = FSystem.creat(fileName, mode);

			if (fd == -1) {
				reg[Cpu.r0] = fd;
				cpu.setCarry();
			} else {			
				reg[Cpu.r0] = fd;
				cpu.clearCarry();
			}
			
			if (debug) {
				System.out.printf("<creat(0x%x, %04o) => %d>\n", filep, mode, fd);
			}
			
			break;
		}
		case 0x11: { // sbrk
			int argnum = memory.readInt(reg[Cpu.ap]);
			int limit = memory.readInt(reg[Cpu.ap] + 4);
			int size = (int)(limit - end);
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
			FSystem.stat(fileName, st);
			
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
			
			long noff = FSystem.lseek(fd, off, mode);
			
			if (debug) {
				System.out.printf("<lseek(%x, 0x%x, %d) = %x>\n", fd, off, mode, noff);
			}
			reg[Cpu.r0] = (int)noff;
			cpu.clearCarry();			
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
		default: {
			System.out.println("unsupported syscam call number :" + sysnum);
			System.exit(1);
		}
		}
	}
	

}
