package ve3.main;

import java.util.ArrayList;
import java.util.List;

import ve3.hdw.ConcrateMemory;
import ve3.hdw.Cpu;
import ve3.hdw.Memory;
import ve3.os.Unix32V;

public class Context {
	
	private Cpu cpu;
	private Memory memory;
	private Unix32V os;
	private byte[] rawdata;
	private int tsize;
	private int dsize;
	private boolean debug;
	
	public Context(byte[] rawdata, List<String> argList) {
		this.rawdata = rawdata;
		init();
		List<String> envList = new ArrayList<String>();
		envList.add("PATH=/usr/local/bin");
		os.processArgs(argList, envList);
	}
		
	private void init() {
		tsize = readInt(rawdata, 4);
		dsize = readInt(rawdata, 8);
		
		System.out.println("tsize = " + tsize);
		System.out.println("dsize = " + dsize);
		
		memory = new ConcrateMemory(0x100000);
		cpu = new Cpu(memory);
		os = new Unix32V(cpu, memory);
		cpu.setOs(os);
		
		int offset = 0;
		// load text
		offset = memory.load(rawdata, 0x20, offset, tsize);
		offset = (offset + 0x1ff) & ~0x1ff;
		System.out.printf("data offset = 0x%x\n", offset);
		// load data
		offset = memory.load(rawdata, 0x20+tsize, offset, dsize);
		//memory.dump(offset, 4);
		cpu.setPc(2);
		cpu.setSp(0x100000);
		debug = false;
		//System.out.println(offset);
		//memory.dump();
	}
			
	public void setDebug(boolean debug) {		
		os.setDebug(this.debug = debug);
		cpu.setDebug(debug);
	}
	
	public void start() {
		cpu.start();
	}
	
	private int readInt(byte[] rawdata, int offset) {
		return rawdata[offset] & 0xff | (rawdata[offset+1] & 0xff) << 8
				| (rawdata[offset+2] & 0xff) << 16 | (rawdata[offset+3] & 0xff) << 24;
	}

}
