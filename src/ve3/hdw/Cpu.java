package ve3.hdw;

import ve3.disassm.V32Disassm.Ope;
import ve3.os.OpInfo;

public class Cpu {
	private int r0, r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11;
	private int ap, fp, sp, pc;
	private Memory memory;
	private OpInfo opinfo;
	private int PSL;
	
	public static Ope[] table = new Ope[0xfffff];
	
	static {
		for (Ope ope : Ope.values()) {
			table[ope.mne] = ope;
		}
	}
	
	
	
	public Cpu() {
		init();
	}
	
	public Cpu(Memory memory) {
		this.memory = memory;
		init();
	}
	
	public void init() {
		r0 = r1 = r2 = r3 = r4 = r5 = r6 = r7 = r8 = r9 = r10 = r11 = 0;
		ap = fp = sp = pc = 0;
		opinfo = new OpInfo();
	}
	
	public void setPc(int pc) {
		memory.setPc(this.pc = pc);		
	}
	
	public void setSp(int sp) {
		this.sp = sp;
	}
	
	public int fetch() {
		byte b = memory.fetch();
		pc = memory.getCurrentPc();
		return b;
	}
	
	public void start() {
		run();
	}
	
	private void run() {
		int b1 = opinfo.setOpCode(fetch() & 0xff);
		Ope ope = Ope.table[b1];
		System.out.println(ope.opname);
	}
	

}
