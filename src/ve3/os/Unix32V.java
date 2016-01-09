package ve3.os;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ve3.hdw.Cpu;
import ve3.hdw.Memory;

public class Unix32V {
	
	private Memory memory;
	private byte[] rawmem;
	private Cpu cpu;
	private int[] reg;
	
	private boolean debug;
	
	public Unix32V(Cpu cpu, Memory memory) {
		this.cpu = cpu;
		this.reg = cpu.getRegister();
		this.memory = memory;
		this.rawmem = memory.getRawMemory();
		this.debug = false;
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
				System.out.printf("<write(%x, 0x%x, %x)", dst, off, len);
			}
			
			System.out.write(rawmem, off, len);
			
			if (debug) {
				System.out.printf(" => %x\n", len);
			}
			reg[Cpu.r0] = len;
			cpu.clearCarry();
			
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
