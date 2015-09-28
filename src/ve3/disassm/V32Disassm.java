package ve3.disassm;

import ve3.hdw.ConcrateMemory;
import ve3.hdw.Memory;
import ve3.os.OpInfo;
import ve3.os.MetaInfo;
import ve3.os.OpInfo.OpInfoSub;
import ve3.os.OpInfo.Type;

public class V32Disassm {
	
	private Memory memory;
	private OpInfo opinfo;

	public static enum AdMode {
		Branch, General, PC
	}
	
	/* b:byte, w:word, l:long, Brb:Branch byte, Brw:Branch word */
	public static enum OT {
		b, w, l, Brb, Brw 
	}
	
	enum Ope {
		HALT(0x00, new MetaInfo()), SUBL2(0xc2, new MetaInfo(OT.l, OT.l)),
		MOVL(0xd0, new MetaInfo(OT.l, OT.l)), MOVAB(0x9e, new MetaInfo(OT.b, OT.l)),
		TSTL(0xd5, new MetaInfo(OT.l)), BNEQ(0x12, new MetaInfo(OT.Brb)),
		CMPL(0xd1, new MetaInfo(OT.l, OT.l)), BLSS(0x19, new MetaInfo(OT.Brb));

		
		public final int mne;		
		public final String opname;
		public final MetaInfo minfo;
		public static Ope[] table = new Ope[0xffff];
		
		static {
			for (Ope ope : Ope.values()) {
				table[ope.mne] = ope;
			}
		}		
		
		Ope(int mne, MetaInfo minfo) {
			this.mne = mne;
			this.minfo = minfo;
			this.opname = toString().toLowerCase();
		}
	}
	
	public V32Disassm() {
		memory = new ConcrateMemory(0xfffff);
		opinfo = new OpInfo();
	}
	
	public V32Disassm(byte[] rawdata) {
		this();
		int tsize = readInt(rawdata, 4);
		System.out.printf("textsize = 0x%x\n", tsize);
		memory.load(rawdata, 0x20, tsize);
		//memory.dump();
	}
	
		
	private int readInt(byte[] rawdata, int offset) {
		return rawdata[offset] & 0xff | (rawdata[offset+1] & 0xff) << 8
				| (rawdata[offset+2] & 0xff) << 16 | (rawdata[offset+3] & 0xff) << 24;
	}
	
	private int fetch(OT optype) {
		switch(optype) {
		case l: {
			//int i = memory.fetch4();
			//System.out.printf("i = %x\n", i);
			//return i;		
			return memory.fetch4();
			
		}
		case w: {
			return memory.fetch2();
		}
		case b: {
			return memory.fetch();
		}
		default: {
			System.exit(1);
		}
		}
		
		return 0;
		
	}
	
	private OpInfoSub resolveDisp(OT optype) {
		if (optype == OT.Brb) {
			opinfo.opsub.type = Type.Branch1;
			opinfo.opsub.arg = memory.fetch() + memory.getCurrentPc();
			//System.out.printf("disp = 0x%x\n", opinfo.opsub.arg);
			return opinfo.opsub;
		} 
		
		
		byte arg = memory.fetch();
		byte type = (byte)((arg >> 4) & 0xf);
		byte value = (byte)(arg & 0xf);
		
		if (value == 0xf) { // program counter address mode
			switch(type) {
			case 0xe: {
				opinfo.opsub.type = Type.LongRel;
				opinfo.opsub.arg = fetch(optype) + memory.getCurrentPc();
				return opinfo.opsub;
			}
			default: {
				System.exit(1);
			}
			
			}

		} else { // normal address mode
			//opinfo.opsub.admode = OpInfo.AddressMode.General;
			switch (type) {
			case 0:
			case 1:
			case 2:
			case 3: { // immediate data
				opinfo.opsub.type = Type.Literal;
				opinfo.opsub.operand = (byte)(arg & 0x3f);
				return opinfo.opsub;				
			}
			case 5: { // Register
				opinfo.opsub.type = Type.Register;
				opinfo.opsub.operand = (byte)(arg & 0xf);
				return opinfo.opsub;				
			}
			case 6: { // Register Defered 
				opinfo.opsub.type = Type.RegDefer;
				opinfo.opsub.operand = (byte)(arg & 0xf);
				return opinfo.opsub;
			}
			case 7: { // Auto Decrement
				opinfo.opsub.type = Type.AutoDec;
				opinfo.opsub.operand = (byte)(arg & 0xf);
				return opinfo.opsub;		
			}
			case 8: { // Auto Increment
				opinfo.opsub.type = Type.AutoInc;
				opinfo.opsub.operand = (byte)(arg & 0xf);
				return opinfo.opsub;				
			}
			case 0xa: { // Byte Displacement
				opinfo.opsub.type = Type.ByteDisp;
				opinfo.opsub.operand = (byte)(arg & 0xf);
				opinfo.opsub.arg = memory.fetch();
				return opinfo.opsub;				
			}
			case 0xb: { // Byte Displacement Deferred
				opinfo.opsub.type = Type.ByteDispDefer;
				opinfo.opsub.operand = (byte)(arg & 0xf);
				opinfo.opsub.arg = memory.fetch();
				return opinfo.opsub;
			}
			default: { // index
				System.out.printf("%x is not implemented yet in resolveDisp\n", type);
				System.exit(1);				
				break;
			}
			
			}			
		}
		
		return null;
	}
	
	private void setArg1(MetaInfo minfo) {
		OpInfoSub opsub = resolveDisp(minfo.arg1);
		opinfo.setType1(opsub.type);
		opinfo.setOpe1(opsub.operand);
		opinfo.setArg1(opsub.arg);
	}
	private void setArg2(MetaInfo minfo) {
		setArg1(minfo);
		OpInfoSub opsub = resolveDisp(minfo.arg2);
		opinfo.setType2(opsub.type);
		opinfo.setOpe2(opsub.operand);
		opinfo.setArg2(opsub.arg);
	}
	
	private String format(int index, byte[] rawdata, String s) {
		return String.format("%4x:   %s    %s", index, new String(rawdata), s);
	}
	
	public void disassm() {
		while (true) {
			opinfo.clear();
			run();
		}
	}
	
	private void run() {
		int index = memory.savePc();
		int b1 = (opinfo.setOpCode(memory.fetch())) & 0xff;
		Ope ope = Ope.table[b1];

		if (ope == null) {
			System.out.printf("0x%x unrecognised mnemonic in run1\n", b1);
			System.exit(1);
		}
		
		opinfo.setMetaInfo(ope.minfo);
		switch(ope.minfo.size) {
		case 0: {
			System.out.println(format(index, memory.rawdump(), Dump.dump(opinfo, ope.opname)));
			break;
		}
		case 1: {
			setArg1(ope.minfo);
			System.out.println(format(index, memory.rawdump(), Dump.dump(opinfo, ope.opname)));
			break;
		}
		case 2: {
			setArg2(ope.minfo);
			System.out.println(format(index, memory.rawdump(), Dump.dump(opinfo, ope.opname)));
			break;
		}
		default: {
			System.out.printf("0x%x unrecognised size in run\n", ope.minfo.size);
			System.exit(1);
		}
		}		
	}
		
}
