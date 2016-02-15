package ve3.main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
	private int bsize;
	private boolean debug;
	private boolean sysdebug;
	private boolean imode;
	private String vaxRoot;
	private List<String> argList;
	private Context parent;
	
	public Context() {		
	}
	
	public Context(byte[] rawdata) {
		this.rawdata = rawdata;
		imode = true;
		debug = true;
		initImode();
		cpu.setDebug(debug);
		parent = null;		
	}
		
	public Context(byte[] rawdata, List<String> argList, String vaxRoot) {
		this.rawdata = rawdata;
		this.vaxRoot = vaxRoot;
		this.argList = argList;
		init();
		parent = null;		
	}
	
	public void setRawData(byte[] rawdata) {
		this.rawdata = rawdata;
	}
	public byte[] getRawData() {
		return rawdata;
	}
	
	public void setVaxRoot(String vaxRoot) {
		this.vaxRoot = vaxRoot;
	}	
	public String getVaxRoot() {
		return vaxRoot;
	}
	public void setArgList(List<String> argList) {
		this.argList = argList;
	}
	public List<String> getArgList() {
		return argList;
	}
	
	private void initImode() {
		memory = new ConcrateMemory(0x100000);
		memory.load(rawdata, 0, rawdata.length);
		cpu = new Cpu();
		cpu.setMemory(memory);
		
	}
	
	public void startIMode() {
		System.out.println("Start Interactive Mode");
		memory.dump(0, 0x10);
		cpu.showHeader();
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String com;
		System.out.print("command >>");
		try {
			while((com = reader.readLine()) != null) {
				if (com.equals("s")) {					
					cpu.run();					
				} else if(com.equals("e")) {
					System.out.println("address >>");
					String addrs = reader.readLine();					
					long addr = Long.parseUnsignedLong(addrs, 16);
					memory.dump((int)addr, 20);					
				} else {
					System.exit(1);
				}
				System.out.print("command >>");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}		
	}
		
	public void init() {
		tsize = readInt(rawdata, 4);
		dsize = readInt(rawdata, 8);
		bsize = readInt(rawdata, 12);
				
		System.out.printf("tsize = 0x%x\n", tsize);
		System.out.printf("dsize = 0x%x\n", dsize);
		System.out.printf("bsize = 0x%x\n", bsize);
		
		memory = new ConcrateMemory(0x100000);
		int offset = 0;
		// load text
		offset = memory.load(rawdata, 0x20, offset, tsize);
		offset = (offset + 0x1ff) & ~0x1ff;
		memory.setEOH((long)(offset + dsize + bsize));
		System.out.printf("data offset = 0x%x\n", offset);
		System.out.printf("end = begin of sbrk = 0x%x\n", memory.getEOH());
		// load data
		offset = memory.load(rawdata, 0x20+tsize, offset, dsize);		
		
		cpu = new Cpu();
		cpu.setMemory(memory);
		//os = new Unix32V(cpu, memory, vaxRoot);
		os = new Unix32V();
		os.setContext(this);
		os.setCpu(cpu);
		os.setMemory(memory);
		os.setVaxRoot(vaxRoot);
		//os.setPid(new Random().nextInt(0x3fffffff) >> 16);
		os.setPid(10000); // for test
		cpu.setOs(os);
		
		//memory.dump(offset, 4);
		//cpu.setPc(2);
		
		cpu.setSp(0x100000);		
		// test for calls
		//cpu.setSp(0xffff);
		//cpu.getRegister()[cpu.r1] = 0x11;		
		// end of test for calls
		
		
		debug = false;
		//System.out.println(offset);
		//memory.dump();
		
		List<String> envList = new ArrayList<String>();
		envList.add("PATH=/usr/local/bin");
		
		os.processArgs(argList, envList); // when call test, comment out
		cpu.setPc(2);
		cpu.setSymTable(os.createSymbolTable(tsize + dsize + 32, rawdata)); // create and set
	}
	
	public void setSysDebug(boolean sysdbg) {		
		os.setDebug(this.sysdebug = sysdbg);
		//cpu.setDebug(false);
	}
	public boolean getSysDebug() {
		return sysdebug;
	}
			
	public void setDebug(boolean debug) {		
		os.setDebug(this.debug = debug);
		cpu.setDebug(debug);
	}
	public boolean getDebug() {
		return debug;
	}
	
	public void start() {
		cpu.start();
	}
	
	private int readInt(byte[] rawdata, int offset) {
		return rawdata[offset] & 0xff | (rawdata[offset+1] & 0xff) << 8
				| (rawdata[offset+2] & 0xff) << 16 | (rawdata[offset+3] & 0xff) << 24;
	}

}
