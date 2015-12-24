package ve3.hdw;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import ve3.disassm.Dump;
import ve3.disassm.V32Disassm;
import ve3.disassm.V32Disassm.OT;
import ve3.disassm.V32Disassm.Ope;
import ve3.os.MetaInfo;
import ve3.os.OpInfo;
import ve3.os.OpInfo.OpInfoSub;
import ve3.os.OpInfo.Type;
import ve3.os.Unix32V;

public class Cpu {
	
	public static final int r0  = 0; 
	public static final int r1  = 1; 
	public static final int r2  = 2; 
	public static final int r3  = 3; 
	public static final int r4  = 4; 
	public static final int r5  = 5; 
	public static final int r6  = 6; 
	public static final int r7  = 7; 
	public static final int r8  = 8; 
	public static final int r9  = 9; 
	public static final int r10 = 10; 
	public static final int r11 = 11; 
	public static final int ap  = 12; 
	public static final int fp  = 13; 
	public static final int sp  = 14; 
	public static final int pc  = 15;
	
	public static final int PSW_C = 0x1;
	public static final int PSW_V = 0x2;
	public static final int PSW_Z = 0x4;
	public static final int PSW_N = 0x8;
	
	
	private int[] reg = new int[16];
	private int psl;
	private Memory memory;
	private OpInfo opinfo;
	
	private Unix32V os;
	private final ByteArrayOutputStream logOut;
	private final PrintStream log;
	
	private boolean debug;
	private byte[] space = "  ".getBytes();
	
	// for computation (e.g., carry, ov flags)
	private long  val64;
	private int   val32;
	private short val16;
	private byte  val8;
	
	
	public static Ope[] table = new Ope[0xfffff];
	
	static {
		for (Ope ope : Ope.values()) {
			table[ope.mne] = ope;
		}
	}
	
	public Cpu() {
		logOut = new ByteArrayOutputStream();
		log = new PrintStream(logOut);
		init();
	}
	
	public Cpu(Memory memory) {
		this();
		this.memory = memory;		
	}
	
	public void init() {
		for (int i = 0; i < reg.length; ++i) {
			reg[i] = 0;
		}
		opinfo = new OpInfo();	
		psl = 0x41f0000;
		debug = false;		
	}
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public void setPc(int pc) {
		memory.setPc(this.reg[this.pc] = pc);		
	}
	
	public void setSp(int val) {
		reg[sp] = val;
	}
	
	public void setOs(Unix32V os) {
		this.os = os;
	}
	
	public int[] getRegister() {
		return reg;
	}
	
	public MVal fetch() {
		MVal mval = memory.fetch();
		reg[pc] = mval.pc;
		return mval;
	}
	public MVal fetch2() {
		MVal mval = memory.fetch2();
		reg[pc] = mval.pc;
		return mval;
	}
	public MVal fetch4() {
		MVal mval = memory.fetch4();
		reg[pc] = mval.pc;
		return mval;
	}
	public MVal fetch8() {
		MVal mval = memory.fetch8();
		reg[pc] = mval.pc;
		return mval;
	}
	
	private long fetch(OT optype) {
		switch(optype) {
		case df:
		case q: {
			return fetch8().lval;
		}
		case f:
		case l: {
			return fetch4().ival;			
		}
		case w: {
			return fetch2().sval;
		}
		case b: {			
			return fetch().bval;
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
		case 0x9: { // Absolute
			opinfo.opsub.type = Type.Abs;
			opinfo.opsub.arg = fetch4().ival;
			return opinfo.opsub;
		}
		case 0xc: { // word relative
			opinfo.opsub.type = Type.WordRel;
			MVal mval = fetch2();
			opinfo.opsub.arg = mval.sval + mval.pc;			
			return opinfo.opsub;
		}
		case 0xd: { // word relative deferred
			opinfo.opsub.type = Type.WordRelDefer;
			MVal mval = fetch2();
			opinfo.opsub.arg = mval.sval + mval.pc;				
			return opinfo.opsub;
		}
		case 0xe: { // long relative
			opinfo.opsub.type = Type.LongRel;
			MVal mval = fetch4();
			opinfo.opsub.arg = mval.ival + mval.pc;
			return opinfo.opsub;
		}
		case 0xf: { // long relative deferred
			opinfo.opsub.type = Type.LongRelDefer;
			MVal mval = fetch4();
			opinfo.opsub.arg = mval.ival + mval.pc;			
			return opinfo.opsub;
		}
		default: {
			System.out.println("unsupported byte in program couter address mode in resolveDispPc: " + type);
			System.exit(1);
		}
		}		
		return null;		
	}

	
	

	
	
	private OpInfoSub resolveDisp(OT optype) {
		if (optype == OT.Brb) {
			MVal mval = fetch();
			opinfo.opsub.type = Type.Branch1;
			opinfo.opsub.arg = mval.bval + mval.pc;
			return opinfo.opsub;
		} else if (optype == OT.Brw) {
			MVal mval = fetch2();
			opinfo.opsub.type = Type.Branch2;
			opinfo.opsub.arg = mval.sval + mval.pc;
			return opinfo.opsub;
		}
		
		byte arg = fetch().bval;
		byte type = (byte)((arg >> 4) & 0xf);
		byte value = (byte)(arg & 0xf);
		switch (type) {
		case 0:
		case 1:
		case 2:
		case 3: { // immediate data
			opinfo.opsub.type = Type.Literal;
			opinfo.opsub.operand = (byte)(arg & 0x3f);
			opinfo.opsub.arg = opinfo.opsub.operand;
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
			opinfo.opsub.addr = opinfo.opsub.operand;
			return opinfo.opsub;				
		}
		case 6: { // Register Defered 
			opinfo.opsub.type = Type.RegDefer;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			//opinfo.opsub.addr = (long)reg[opinfo.opsub.operand = (byte)(arg & 0xf)] & 0xffffffffL;
			opinfo.opsub.addr = (long)reg[opinfo.opsub.operand] & 0xffffffffL;
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
			opinfo.opsub.arg = fetch().bval;
			opinfo.opsub.addr = (long)reg[opinfo.opsub.operand] + (long)opinfo.opsub.arg;
			return opinfo.opsub;				
		}
		case 0xb: { // Byte Displacement Deferred
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.ByteDispDefer;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			opinfo.opsub.arg = fetch().bval;
			return opinfo.opsub;
		}
		case 0x0c: { // Word Displacement
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.WordDisp;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			opinfo.opsub.arg = fetch2().sval;
			return opinfo.opsub;
		}
		case 0x0d: { // Word Displacement Deferred
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.WordDispDefer;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			opinfo.opsub.arg = fetch2().sval;
			return opinfo.opsub;
		}
		case 0x0e: { // Long Displacement
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.LongDisp;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			opinfo.opsub.arg = fetch4().ival;
			return opinfo.opsub;
		}
		case 0x0f: { // Long Displacement Deferred
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.LongDispDefer;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			opinfo.opsub.arg = fetch4().ival;
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
		OpInfoSub opsub = null;
		while ((opsub = resolveDisp(minfo.arg1)).type == Type.Index) {
			opinfo.pushIdx1(opsub.operand);
		}		
		opinfo.setType1(opsub.type);
		opinfo.setOpe1(opsub.operand);
		opinfo.setArg1(opsub.arg);
		opinfo.setAddr1(opsub.addr);
		
	}
	
	private void setArg2(MetaInfo minfo) {
		setArg1(minfo);		
		OpInfoSub opsub = null;
		while ((opsub = resolveDisp(minfo.arg2)).type == Type.Index) {
			opinfo.pushIdx2(opsub.operand);
		}		
		opinfo.setType2(opsub.type);
		opinfo.setOpe2(opsub.operand);
		opinfo.setArg2(opsub.arg);
		opinfo.setAddr2(opsub.addr);
	}
	
	private short getShort(Type type, long arg, long addr) {
		return (short)getInt(type, arg, addr);
		/*
		switch (type) {
		case Literal: {
			return (short)arg;
		}
		}
		return 0;
		*/		
	}
	
	private int getInt(Type type, long arg, long addr) {
		switch (type) {
		case Literal: 
		case Immed: {
			return (int)arg;
		}
		case Register: {
			return reg[(int)addr];
		}
		case ByteDisp: {
			//System.out.printf("%x\n", (int)addr);
			//int val = memory.readInt((int)addr);
			//System.out.println("val = " + val);
			return memory.readInt((int)addr);
		}
		default: {
			System.out.println("unrecognized type in getInt: " + type);
			System.exit(1);
		}
		}
		
		return 0;
	}
	
	private void storeInt(Type type, long addr, int value) {
		switch (type) {
		case Register: {
			reg[(int)(addr & 0xff)] = value;
			break;
		}
		case RegDefer: {
			memory.writeInt((int)addr, value);			
			break;
		}
		case ByteDisp: {
			memory.writeInt((int)addr, value);
			break;
		}
		default: {
			System.out.println("value = " + value);
			System.out.println("unrecognized type in storeInt: " + type);
			System.exit(1);
		}
		}
	}
		
	
	private void showHeader() {
		System.out.println("   r0       r1       r2       r3       r4       r5       r6       r7       r8       r9      r10    "
				+ "  r11       ap       fp       sp       pc    NZVC");
	}
	
	private void storeRegInfo() {		
		log.printf("%08x %08x %08x %08x %08x %08x %08x %08x %08x %08x %08x %08x %08x %08x %08x %08x", 
				reg[r0], reg[r1], reg[r2], reg[r3], reg[r4], reg[r5], reg[r6], reg[r7], reg[r8], reg[r9], 
				reg[r10], reg[r11], reg[ap], reg[fp], reg[sp], reg[pc]);
		
		// NZCV
		log.printf(" %s%s%s%s", isN() ? "N" : "-",
				isZ() ? "Z" : "-",
				isV() ? "V" : "-",
				isC() ? "C" : "-"
								
				//((((psl & PSW_Z) >>> 2) & 1) == 1) ? "Z" : "-",
//				((((psl & PSW_Z) >>> 2) & 1) == 1) ? "Z" : "-",
				//((((psl & PSW_V) >>> 1) & 1) == 1) ? "V" : "-",
				//((((psl & PSW_C))       & 1) == 1) ? "C" : "-"				
			);		
	}
	
	private boolean isN() {
		return (((psl & PSW_N) >>> 3) & 1) == 1; 
	}
	private boolean isZ() {
		return (((psl & PSW_Z) >>> 2) & 1) == 1;
	}
	private boolean isV() {
		return (((psl & PSW_V) >>> 1) & 1) == 1;
	}
	private boolean isC() {
		return ((psl & PSW_C) & 1) == 1;
	}
	
	private void setNZVC(boolean nf, boolean zf, boolean vf, boolean cf) {
		psl = nf ? (psl | PSW_N) : (psl & ~PSW_N);
		psl = zf ? (psl | PSW_Z) : (psl & ~PSW_Z);
		psl = vf ? (psl | PSW_V) : (psl & ~PSW_V);
		psl = cf ? (psl | PSW_C) : (psl & ~PSW_C);		
	}
	/* for OS system call*/
	public void clearCarry() {
		psl &= ~PSW_C;
	}
	
	public void pushInt(int val) {
		reg[sp] -= 4;
		memory.writeInt(reg[sp], val);
		//memory.dump(reg[sp], 4);
	}
	
	public int popInt() {
		int r = memory.readInt(reg[sp]);
		reg[sp] += 4;
		return r;
	}
	
	
	private void storeDisInfo(OpInfo opinfo, String opname) {
		try {
			//log.write("  ".getBytes());
			log.write(space);
			log.write(Dump.dump(opinfo, opname).getBytes());
		} catch (IOException e) {
			
		}
	}
			
	public void start() {
		if (debug) {
			showHeader();
		}
		for (int i = 0; i < 7; ++i) {
			run();
		}
	}
	
	private void run() {
		if (debug) {
			storeRegInfo();
		}
		
		int b1 = opinfo.setOpCode(fetch().bval & 0xff);
		Ope ope = Ope.table[b1];
		
		opinfo.minfo = ope.minfo;
		switch (ope.minfo.size) {
		case 0: {
			break;
		}
		case 1: {
			setArg1(ope.minfo);
			break;
		}
		case 2: {
			setArg2(ope.minfo);
			break;
		}
		default: {
			System.out.printf("0x%x unrecognised size in run\n", ope.minfo.size);			
			System.exit(1);
		}
		}
		storeDisInfo(opinfo, ope.opname);
		
		
		switch (ope.mne) {
		case 0: { // Halt
			break;
		}
		case 0xd0: { // movl
			int src = getInt(opinfo.getType1(), opinfo.getArg1(), opinfo.getAddr1());
			storeInt(opinfo.getType2(), opinfo.getAddr2(), src);
			setNZVC(src < 0, src == 0, false, isC());			
			break;
		}
		case 0xbc: { // chmk CHMKではフラグはいじらない(REI命令で戻されるから)．
			int src = getShort(opinfo.getType1(), opinfo.getArg1(), opinfo.getAddr1());
			if (debug) {
				System.out.println(new String(logOut.toByteArray()));
				logOut.reset();
			}
			os.syscall(src);			
			break;
		}
		case 0xdd: { // pushl
			int src = getInt(opinfo.getType1(), opinfo.getArg1(), opinfo.getAddr1());
			pushInt(src);
			setNZVC(src < 0, src == 0, false, isC());						
			break;
		}
		case 0xc2: { // subl2
			int src = getInt(opinfo.getType1(), opinfo.getArg1(), opinfo.getAddr1());
			int dst = getInt(opinfo.getType2(), opinfo.getArg2(), opinfo.getAddr2());
			val64 = (long)dst - (long)src;
			val32 = (int)val64;
			storeInt(opinfo.getType2(), opinfo.getAddr2(), val32);
			setNZVC(val32 < 0, val32 == 0, val64 != val32, (dst & 0xffffffffL) < (src & 0xffffffffL));
			break;
		}
		case 0x9e: { // movab
			//int src = getInt(opinfo.getType1(), opinfo.getArg1(), opinfo.getAddr1(), opinfo.minfo.arg1);
			int src = (int)opinfo.getAddr1();
			//System.out.printf("addr = %x\n", src);
			storeInt(opinfo.getType2(), opinfo.getAddr2(), src);
			setNZVC(src < 0, src == 0, false, isC());			
			break;
		}
		default: {
			System.out.printf("unrecognised operator[0x%x] in run\n", ope.mne);
			System.exit(1);
		}
		}
		
		if (debug && logOut.size() > 0) {
			System.out.println(new String(logOut.toByteArray()));
			logOut.reset();
		}
		
	}
	

}
