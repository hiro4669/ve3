package ve3.main;

import ve3.hdw.ConcrateMemory;
import ve3.hdw.Cpu;
import ve3.hdw.Memory;

public class Context {
	
	private Cpu cpu;
	private Memory memory;
	private byte[] rawdata;
	private int tsize;
	private int dsize;
	
	public Context(byte[] rawdata) {
		this.rawdata = rawdata;
		init();
	}
	
	private void init() {
		tsize = readInt(rawdata, 4);
		dsize = readInt(rawdata, 8);
		
		System.out.println("tsize = " + tsize);
		System.out.println("dsize = " + dsize);
		
		memory = new ConcrateMemory(0x100000);
		cpu = new Cpu(memory);
		int offset = 0;
		// load text
		offset = memory.load(rawdata, 0x20, offset, tsize);
		offset = (offset + 0x1ff) & ~0x1ff;
		//System.out.println(offset);
		// load data
		offset = memory.load(rawdata, 0x20+tsize, offset, dsize);
		cpu.setPc(2);
		cpu.setSp(0xfffff);
		//System.out.println(offset);
		//memory.dump();
	}
	
	public void start() {
		cpu.start();
	}
	
	private int readInt(byte[] rawdata, int offset) {
		return rawdata[offset] & 0xff | (rawdata[offset+1] & 0xff) << 8
				| (rawdata[offset+2] & 0xff) << 16 | (rawdata[offset+3] & 0xff) << 24;
	}

}
