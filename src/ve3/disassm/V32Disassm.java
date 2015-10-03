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
		b, w, l, f, q, df, ebl, Brb, Brw 
	}
	
	enum Ope {

		DUMMY1(0x5bd0, new MetaInfo()), DUMMY2(0x5be0, new MetaInfo()),
		DUMMY3(0x599a, new MetaInfo()),		
		HALT(0x00, new MetaInfo()), SUBL2(0xc2, new MetaInfo(OT.l, OT.l)),
		MOVL(0xd0, new MetaInfo(OT.l, OT.l)), MOVAB(0x9e, new MetaInfo(OT.b, OT.l)),
		TSTL(0xd5, new MetaInfo(OT.l)), BNEQ(0x12, new MetaInfo(OT.Brb)),
		CMPL(0xd1, new MetaInfo(OT.l, OT.l)), BLSS(0x19, new MetaInfo(OT.Brb)),
		CALLS(0xfb, new MetaInfo(OT.l, OT.b)), PUSHL(0xdd, new MetaInfo(OT.l)),
		CHMK(0xbc, new MetaInfo(OT.w)), PROBER(0x0c, new MetaInfo(OT.b, OT.w, OT.b)),
		NOP(0x01, new MetaInfo()), BLEQ(0x15, new MetaInfo(OT.Brb)),
		CMPB(0x91, new MetaInfo(OT.b, OT.b)), INCL(0xd6, new MetaInfo(OT.l)),
		DECL(0xd7, new MetaInfo(OT.l)), ADDL2(0xc0, new MetaInfo(OT.l, OT.l)),
		BRB(0x11, new MetaInfo(OT.Brb)), PUSHAL(0xdf, new MetaInfo(OT.l)),
		SUBL3(0xc3, new MetaInfo(OT.l, OT.l, OT.l)), BGEQ(0x18, new MetaInfo(OT.Brb)),
		CVTLB(0xf6, new MetaInfo(OT.l, OT.b)), CVTBL(0x98, new MetaInfo(OT.b, OT.l)),
		RET(0x4, new MetaInfo()), REMQUE(0x0f, new MetaInfo(OT.b, OT.l)),
		XORW2(0xac, new MetaInfo(OT.w, OT.w)), BICW3(0xab, new MetaInfo(OT.w, OT.w, OT.w)),
		BEQL(0x13, new MetaInfo(OT.Brb)), BISB2(0x88, new MetaInfo(OT.b, OT.b)),
		BBC(0xe1, new MetaInfo(OT.l, OT.b, OT.Brb)), CLRF(0xd4, new MetaInfo(OT.f)),
		MOVAL(0xde, new MetaInfo(OT.l, OT.l)), CVTWL(0x32, new MetaInfo(OT.w, OT.l)),
		BCC(0x1e, new MetaInfo(OT.Brb)), JMP(0x17, new MetaInfo(OT.b)),
		ADDL3(0xc1, new MetaInfo(OT.l, OT.l, OT.l)), MNEGL(0xce, new MetaInfo(OT.l, OT.l)),
		REMQHI(0x5e, new MetaInfo(OT.q, OT.l)), BICL2(0xca, new MetaInfo(OT.l, OT.l)),
		MOVB(0x90, new MetaInfo(OT.b, OT.b)), BISL2(0xc8, new MetaInfo(OT.l, OT.l)),
		BLBC(0xe9, new MetaInfo(OT.l, OT.Brb)), BLBS(0xe8, new MetaInfo(OT.l, OT.Brb)),
		SUBD3(0x63, new MetaInfo(OT.df, OT.df, OT.df)), DIVD3(0x67, new MetaInfo(OT.df, OT.df, OT.df)),
		SPANC(0x2b, new MetaInfo(OT.w, OT.b, OT.b, OT.b)), BSBW(0x30, new MetaInfo(OT.Brw)),
		CVTWB(0x33, new MetaInfo(OT.w, OT.b)), CVTPL(0x36, new MetaInfo(OT.w, OT.b, OT.l)),
		ADDD3(0x61, new MetaInfo(OT.df, OT.df, OT.df)), MULD3(0x65, new MetaInfo(OT.df, OT.df, OT.df)),
		BICB3(0x8b, new MetaInfo(OT.b, OT.b, OT.b)), MOVF(0x50, new MetaInfo(OT.f, OT.f)),
		CVTWF(0x4d, new MetaInfo(OT.w, OT.f)), POPR(0xba, new MetaInfo(OT.w)),
		MOVTC(0x2e, new MetaInfo(OT.w, OT.b, OT.b, OT.w, OT.b, OT.b)),
		CLRB(0x94, new MetaInfo(OT.b)), XORW3(0xad, new MetaInfo(OT.w, OT.w, OT.w)),
		CVTDF(0x76, new MetaInfo(OT.df, OT.f)), CASEB(0x8f, new MetaInfo(OT.b, OT.b, OT.b)),
		BRW(0x31, new MetaInfo(OT.Brw)), MOVZBL(0x9a, new MetaInfo(OT.ebl, OT.l)),
		MOVC3(0x28, new MetaInfo(OT.w, OT.b, OT.b)), CVTBD(0x6c, new MetaInfo(OT.b, OT.df)),
		BBS(0xe0, new MetaInfo(OT.l, OT.b, OT.Brb)), LOCC(0x3a, new MetaInfo(OT.b, OT.w, OT.b)),
		EXTZV(0xef, new MetaInfo(OT.l, OT.b, OT.b, OT.l)), ACBL(0xf1, new MetaInfo(OT.l, OT.l, OT.l, OT.Brw)),
		SKPC(0x3b, new MetaInfo(OT.b, OT.w, OT.b)), BISW3(0xa9, new MetaInfo(OT.w, OT.w, OT.w));
		
		

		
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
	
	private long fetch(OT optype) {
		switch(optype) {
		case df:
		case q: {
			return memory.fetch8();
		}
		case f:
		case l: {
			return memory.fetch4();			
		}
		case w: {
			return memory.fetch2();
		}
		case b: {			
			return memory.fetch();
		}
		default: {
			System.out.println("unrecognised optype(OT) in fetch");
			System.exit(1);
		}
		}
		
		return 0;
		
	}
	
	private OpInfoSub resolveDispPc(OT optype, byte type) {
		switch(type) {
		case 0x8: { // Immediate
			opinfo.opsub.type = Type.Immed;
			opinfo.opsub.arg = fetch(optype);
			return opinfo.opsub;
		}
		case 0xc: { // word relative
			opinfo.opsub.type = Type.WordRel;
			opinfo.opsub.arg = memory.fetch2() + memory.getCurrentPc();
			return opinfo.opsub;
		}
		case 0xe: { // long relative
			opinfo.opsub.type = Type.LongRel;
			opinfo.opsub.arg = memory.fetch4() + memory.getCurrentPc();
			return opinfo.opsub;
		}
		default: {
			System.out.println("unsupported byte in program couter address mode in resolveDispPc");
			System.exit(1);
		}
		}		
		return null;		
	}
	
	private OpInfoSub resolveDisp(OT optype) {
		if (optype == OT.Brb) {
			opinfo.opsub.type = Type.Branch1;
			opinfo.opsub.arg = memory.fetch() + memory.getCurrentPc();
			//System.out.printf("disp = 0x%x\n", opinfo.opsub.arg);
			return opinfo.opsub;
		} else if(optype == OT.Brw) {
			opinfo.opsub.type = Type.Branch2;
			opinfo.opsub.arg = memory.fetch2() + memory.getCurrentPc();
			return opinfo.opsub;
		}
		
		byte arg = memory.fetch();
		byte type = (byte)((arg >> 4) & 0xf);
		byte value = (byte)(arg & 0xf);
		
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
		case 4: { // index
			opinfo.opsub.type = Type.Index;
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
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.AutoInc;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			return opinfo.opsub;				
		}
		case 9: {
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.AutoIncDefer;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			return opinfo.opsub;			
		}
		case 0xa: { // Byte Displacement
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.ByteDisp;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			opinfo.opsub.arg = memory.fetch();
			return opinfo.opsub;				
		}
		case 0xb: { // Byte Displacement Deferred
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.ByteDispDefer;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			opinfo.opsub.arg = memory.fetch();
			return opinfo.opsub;
		}
		case 0x0c: { // Word Displacement
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.WordDisp;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			opinfo.opsub.arg = memory.fetch2();
			return opinfo.opsub;
		}
		case 0x0d: { // Word Displacement Deferred
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.WordDispDefer;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			opinfo.opsub.arg = memory.fetch2();
			return opinfo.opsub;
		}
		case 0x0e: { // Long Displacement
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.LongDisp;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			opinfo.opsub.arg = memory.fetch4();
			return opinfo.opsub;
		}
		default: { // index
			System.out.printf("%x is not implemented yet in resolveDisp\n", type);
			System.exit(1);				
			break;
		}
		
		}			
		
		return null;
	}
	
	private void setArg1(MetaInfo minfo) {
		OpInfoSub opsub = resolveDisp(minfo.arg1);
		if (opsub.type == Type.Index) {
			opinfo.setIdx1(opsub.operand);
			opsub = resolveDisp(minfo.arg1);
		}
		opinfo.setType1(opsub.type);
		opinfo.setOpe1(opsub.operand);
		opinfo.setArg1(opsub.arg);
	}
	private void setArg2(MetaInfo minfo) {
		setArg1(minfo);
		OpInfoSub opsub = resolveDisp(minfo.arg2);
		if (opsub.type == Type.Index) {
			opinfo.setIdx2(opsub.operand);
			opsub = resolveDisp(minfo.arg2);
		}
		opinfo.setType2(opsub.type);
		opinfo.setOpe2(opsub.operand);
		opinfo.setArg2(opsub.arg);
	}
	
	private void setArg3(MetaInfo minfo) {
		setArg2(minfo);
		OpInfoSub opsub = resolveDisp(minfo.arg3);
		if (opsub.type == Type.Index) {
			opinfo.setIdx3(opsub.operand);
			opsub = resolveDisp(minfo.arg3);
		}
		opinfo.setType3(opsub.type);
		opinfo.setOpe3(opsub.operand);
		opinfo.setArg3(opsub.arg);
	}
	
	private void setArg4(MetaInfo minfo) {
		setArg3(minfo);
		OpInfoSub opsub = resolveDisp(minfo.arg4);
		if (opsub.type == Type.Index) {
			opinfo.setIdx4(opsub.operand);
			opsub = resolveDisp(minfo.arg4);
		}
		opinfo.setType4(opsub.type);
		opinfo.setOpe4(opsub.operand);
		opinfo.setArg4(opsub.arg);
	}
	
	private void setArg5(MetaInfo minfo) {
		setArg4(minfo);
		OpInfoSub opsub = resolveDisp(minfo.arg5);
		if (opsub.type == Type.Index) {
			opinfo.setIdx5(opsub.operand);
			opsub = resolveDisp(minfo.arg5);
		}
		opinfo.setType5(opsub.type);
		opinfo.setOpe5(opsub.operand);
		opinfo.setArg5(opsub.arg);		
	}
	
	private void setArg6(MetaInfo minfo) {
		setArg5(minfo);
		OpInfoSub opsub = resolveDisp(minfo.arg6);
		if (opsub.type == Type.Index) {
			opinfo.setIdx6(opsub.operand);
			opsub = resolveDisp(minfo.arg6);
		}
		opinfo.setType6(opsub.type);
		opinfo.setOpe6(opsub.operand);
		opinfo.setArg6(opsub.arg);		
	}
	
	
	
	private String format(int index, byte[] rawdata, String s) {
		return String.format("%4x:   %s    %s", index, new String(rawdata), s);
	}
	private String format(int index, byte[] rawdata) {
		return String.format("%4x:   %s", index, new String(rawdata));	
	}
	
	
	private void showlog(String s) {
		System.out.println(s);
		while(memory.remaining()) {
			System.out.println(format(memory.getPrevPc(), memory.rawdump_rem()));
		}
	}
	
	public void disassm() {
		while (true) {
			opinfo.clear();
			run();
		}
	}
	
	private void run() {
		int index = memory.savePc();
		//int b1 = (opinfo.setOpCode(memory.fetch())) & 0xff;
		int b1 = opinfo.setOpCode(memory.fetch() & 0xff);
		Ope ope = Ope.table[b1];

		if (ope == null) {
			b1 = opinfo.setOpCode(b1 << 8 | memory.fetch() & 0xff);			
			ope = Ope.table[b1];
			if (ope == null) {
				System.out.printf("0x%x unrecognised mnemonic in run1\n", b1);
				System.exit(1);
			} else {
				// temporary
				showlog(format(index, memory.rawdump(), ".word " + String.format("0x%x", b1)));
				return;
			}
		}


		opinfo.setMetaInfo(ope.minfo);
		switch(ope.minfo.size) {
		case 0: {
			showlog(format(index, memory.rawdump(), Dump.dump(opinfo, ope.opname)));
			break;
		}
		case 1: {
			setArg1(ope.minfo);
			showlog(format(index, memory.rawdump(), Dump.dump(opinfo, ope.opname)));
			break;
		}
		case 2: {			
			setArg2(ope.minfo);
			showlog(format(index, memory.rawdump(), Dump.dump(opinfo, ope.opname)));
			
			break;
		}
		case 3: {
			setArg3(ope.minfo);
			showlog(format(index, memory.rawdump(), Dump.dump(opinfo, ope.opname)));
			break;
		}
		case 4: {
			setArg4(ope.minfo);
			showlog(format(index, memory.rawdump(), Dump.dump(opinfo, ope.opname)));
			break;
		}
		case 5: {
			setArg5(ope.minfo);
			showlog(format(index, memory.rawdump(), Dump.dump(opinfo, ope.opname)));
			break;
		}
		case 6: {
			setArg6(ope.minfo);
			showlog(format(index, memory.rawdump(), Dump.dump(opinfo, ope.opname)));
			break;
		}
		default: {
			System.out.printf("0x%x unrecognised size in run\n", ope.minfo.size);
			System.exit(1);
		}
		}		
	}
		
}
