package ve3.os;

import java.util.List;

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
				System.out.printf("=> %x\n", len);
			}
			reg[Cpu.r0] = len;
			cpu.clearCarry();
			
			break;
		}
		}
	}
	

}
