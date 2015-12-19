package ve3.os;

import ve3.hdw.Cpu;
import ve3.hdw.Memory;

public class Unix32V {
	
	private Memory memory;
	private byte[] rawmem;
	private Cpu cpu;
	private int[] reg;
	
	public Unix32V(Cpu cpu, Memory memory) {
		this.cpu = cpu;
		this.reg = cpu.getRegister();
		this.memory = memory;
		this.rawmem = memory.getRawMemory();		
	}
	
	public void syscall(int sysnum) {
		switch (sysnum) {
		case 1: { // exit
			int argnum = memory.readInt(reg[Cpu.ap]);
			int exnum = memory.readInt(reg[Cpu.ap] + 4);
			System.out.printf("<exit(%d)>\n", exnum);
			
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
			System.out.printf("<write(%x, 0x%x, %x)", dst, off, len);
			System.out.write(rawmem, off, len);
			System.out.printf("=> %x\n", len);
			reg[Cpu.r0] = len;
			
			break;
		}
		}
	}
	

}
