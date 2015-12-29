package ve3.hdw;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

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
	
	// for debug
	private Map<Integer, String> symTable;
	
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
	
	public void setSymTable(Map<Integer, String> symTable) {
		this.symTable = symTable;
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
		case 0x8: { // Immediate OK
			opinfo.opsub.type = Type.Immed;
			opinfo.opsub.arg = fetch(optype);			
			return opinfo.opsub;
		}
		case 0x9: { // Absolute
			opinfo.opsub.type = Type.Abs;
			opinfo.opsub.arg = fetch4().ival;
			System.out.println(type + " not implemented yet in resolveDispPc");
			System.exit(1);
			return opinfo.opsub;
		}
		case 0xc: { // word relative
			opinfo.opsub.type = Type.WordRel;
			MVal mval = fetch2();
			opinfo.opsub.arg = mval.sval + mval.pc;
			System.out.println(type + " not implemented yet in resolveDispPc");
			System.exit(1);
			return opinfo.opsub;
		}
		case 0xd: { // word relative deferred
			opinfo.opsub.type = Type.WordRelDefer;
			MVal mval = fetch2();
			opinfo.opsub.arg = mval.sval + mval.pc;
			System.out.println(type + " not implemented yet in resolveDispPc");
			System.exit(1);
			return opinfo.opsub;
		}
		case 0xe: { // long relative
			opinfo.opsub.type = Type.LongRel;
			MVal mval = fetch4();
			opinfo.opsub.addr = opinfo.opsub.arg = mval.ival + mval.pc;			
			return opinfo.opsub;
		}
		case 0xf: { // long relative deferred
			opinfo.opsub.type = Type.LongRelDefer;
			MVal mval = fetch4();
			opinfo.opsub.arg = mval.ival + mval.pc;		
			System.out.println(type + " not implemented yet in resolveDispPc");
			System.exit(1);
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
			opinfo.opsub.addr = opinfo.opsub.arg;
			return opinfo.opsub;
		} else if (optype == OT.Brw) {
			MVal mval = fetch2();
			opinfo.opsub.type = Type.Branch2;
			opinfo.opsub.arg = mval.sval + mval.pc;
			opinfo.opsub.addr = opinfo.opsub.arg;
			return opinfo.opsub;
		}
		
		byte arg = fetch().bval;
		byte type = (byte)((arg >> 4) & 0xf);
		byte value = (byte)(arg & 0xf);
		switch (type) {
		case 0:
		case 1:
		case 2:
		case 3: { // Literal
			opinfo.opsub.type = Type.Literal;
			opinfo.opsub.operand = (byte)(arg & 0x3f);
			opinfo.opsub.arg = opinfo.opsub.operand;
			return opinfo.opsub;				
		}
		case 4: { // Index
			opinfo.opsub.type = Type.Index;
			opinfo.opsub.operand = (byte)(arg & 0x3f);
			System.out.println(type + " not implemented yet in resolveDisp");
			System.exit(1);
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
			
			switch (optype) {
			case b: {
				reg[opinfo.opsub.operand] -= 1;
				break;				
			}
			case w: {
				reg[opinfo.opsub.operand] -= 2;
				break;
			}
			case l: { 
				reg[opinfo.opsub.operand] -= 4;
				break;
			}			
			default: {
				System.out.println("unsupported optype in AutoDecrement in resolveDisp");
				System.exit(1);
			}
			}
			opinfo.opsub.addr = reg[opinfo.opsub.operand];				
			return opinfo.opsub;		
		}
		case 8: { // Auto Increment
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.AutoInc;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			opinfo.opsub.addr = reg[opinfo.opsub.operand];
			switch (optype) {
			case b: {
				reg[opinfo.opsub.operand] += 1;
				break;
			}
			case w: {
				reg[opinfo.opsub.operand] += 2;
				break;
			}
			case l: {
				reg[opinfo.opsub.operand] += 4;
				break;
			}
			default: {
				System.out.println("unsupported optype in AutoIncrement in resolveDisp");
				System.exit(1);
			}
			}
			return opinfo.opsub;				
		}
		case 9: { // AutoIncDefer
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.AutoIncDefer;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			System.out.println(type + " not implemented yet in resolveDisp");
			System.exit(1);
			return opinfo.opsub;			
		}
		case 0xa: { // Byte Displacement
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.ByteDisp;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			opinfo.opsub.arg = fetch().bval;
			opinfo.opsub.addr = (long)reg[opinfo.opsub.operand] + (long)opinfo.opsub.arg; // minus OK
			/*
			System.out.printf("arg = %x, %d\n", opinfo.opsub.arg, opinfo.opsub.arg);
			System.out.printf("reg = %x, %d\n", reg[opinfo.opsub.operand], reg[opinfo.opsub.operand]); 						
			System.out.printf("addr = 0x%08x\n", opinfo.opsub.addr);
			*/
			return opinfo.opsub;				
		}
		case 0xb: { // Byte Displacement Deferred
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.ByteDispDefer;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			opinfo.opsub.arg = fetch().bval;			
			long tmp = (long)reg[opinfo.opsub.operand] + (long)opinfo.opsub.arg; 
			//System.out.printf("tmp = %x\n", tmp);
			opinfo.opsub.addr = memory.readInt((int)tmp);
			//System.out.printf("0x%x\n", opinfo.opsub.addr);
			//System.out.println(type + " not implemented yet in resolveDisp");
			//System.exit(1);
			return opinfo.opsub;
		}
		case 0x0c: { // Word Displacement
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.WordDisp;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			opinfo.opsub.arg = fetch2().sval;
			System.out.println(type + " not implemented yet in resolveDisp");
			System.exit(1);
			return opinfo.opsub;
		}
		case 0x0d: { // Word Displacement Deferred
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.WordDispDefer;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			opinfo.opsub.arg = fetch2().sval;
			System.out.println(type + " not implemented yet in resolveDisp");
			System.exit(1);
			return opinfo.opsub;
		}
		case 0x0e: { // Long Displacement
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.LongDisp;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			opinfo.opsub.arg = fetch4().ival;
			System.out.println(type + " not implemented yet in resolveDisp");
			System.exit(1);
			return opinfo.opsub;
		}
		case 0x0f: { // Long Displacement Deferred
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.LongDispDefer;
			opinfo.opsub.operand = (byte)(arg & 0xf);
			opinfo.opsub.arg = fetch4().ival;
			System.out.println(type + " not implemented yet in resolveDisp");
			System.exit(1);
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
	
	private void setArg3(MetaInfo minfo) {
		setArg2(minfo);
		OpInfoSub opsub = null;
		while ((opsub = resolveDisp(minfo.arg3)).type == Type.Index) {
			opinfo.pushIdx3(opsub.operand);
		}
		
		opinfo.setType3(opsub.type);
		opinfo.setOpe3(opsub.operand);
		opinfo.setArg3(opsub.arg);
		opinfo.setAddr3(opsub.addr);
		
	}
	
	private byte getByte(Type type, long arg, long addr) {
		return (byte)getInt(type, arg, addr);		
	}
	private short getShort(Type type, long arg, long addr) {
		return (short)getInt(type, arg, addr);		
	}
	
	private int getInt(Type type, long arg, long addr) {
		switch (type) {
		case Branch1: {
			return (int)addr;
		}
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
		case ByteDispDefer: {
			return memory.readInt((int)addr);
		}
		case AutoInc: {
			//System.out.printf("%x\n", (int)addr);
			return memory.readInt((int)addr);			
		}
		case LongRel: {
			return memory.readInt((int)addr);
		}
		default: {
			System.out.println("unrecognized type in getInt: " + type);
			System.exit(1);
		}
		}
		
		return 0;
	}
	
	private void storeByte(Type type, long addr, byte value) {
		switch (type) {
		case ByteDisp: {
			memory.writeByte((int)addr, value);
			break;
		}
		default: {
			System.out.printf("addr = 0x%x, value = 0x%x\n", addr, value);
			System.out.println("unrecognized type in storeByte: " + type);
			System.exit(1);
		}
		
		}
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
		case LongRel: {
			memory.writeInt((int)addr, value);			
			break;
		}
		case AutoDec: {
			memory.writeInt((int)addr, value);
			break;
		}
		default: {
			System.out.printf("addr = 0x%x, value = 0x%x\n", addr, value);
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
		
		for (int i = 0; i < 433; ++i) {
			run();			
			//memory.dump(0x611, 1);
		}
		System.out.println("end of loop");
		
		//memory.dump(reg[sp], 0x100000 - reg[sp]); // show memory		
	
	}
	
	private void run() {
		if (debug) {
			//log.printf("%x:", reg[pc]);
			if (symTable.containsKey(reg[pc] - 2)) {
				log.println(symTable.get(reg[pc] - 2));
			}
			storeRegInfo();
		}
		
		int b1 = opinfo.setOpCode(fetch().bval & 0xff);
		Ope ope = Ope.table[b1];
		//System.out.printf("b1 = %x\n", b1);
		
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
		case 3: {
			setArg3(ope.minfo);
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
		case 0xd1: { // cmpl
			int src1 = getInt(opinfo.getType1(), opinfo.getArg1(), opinfo.getAddr1());
			int src2 = getInt(opinfo.getType2(), opinfo.getArg2(), opinfo.getAddr2());
			val64 = (long)src1 - (long)src2;
			val32 = (int)val64;
			/*
			System.out.println("type2 = " + opinfo.getType2());
			memory.dump((int)opinfo.getAddr2(), 4);
			System.out.printf("src1 = %x, src2 = %x\n", src1, src2);
			*/
			setNZVC(val32 < 0, val32 == 0, false, (src1 & 0xffffffffL) < (src2 & 0xffffffffL));
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
		case 0xde: { // moval
			int src = (int)opinfo.getAddr1();
			//System.out.printf("addr = %x\n", src);
			storeInt(opinfo.getType2(), opinfo.getAddr2(), src);
			setNZVC(src < 0, src == 0, false, isC());
			
			break;
		}
		case 0xd5: { // tstl
			int src = getInt(opinfo.getType1(), opinfo.getArg1(), opinfo.getAddr1());
			val64 = (long)src - 0;
			val32 = (int)val64;
			setNZVC(val32 < 0, val32 == 0, false, false);
			break;
		}
		case 0x12: { // bneq			
			//System.out.printf("addr = %x\n", opinfo.getAddr1());
			//System.out.println(opinfo.getType1());			
			if (!isZ()) {				
				int src = getInt(opinfo.getType1(), opinfo.getArg1(), opinfo.getAddr1());
				//System.out.printf("src = %x, addr = %x\n", src, opinfo.getAddr1());
				setPc(src);
			}
			break;
		}
		case 0x13: { // beql
			if (isZ()) {				
				int src = getInt(opinfo.getType1(), opinfo.getArg1(), opinfo.getAddr1());
				//System.out.printf("src = %x, addr = %x\n", src, opinfo.getAddr1());
				setPc(src);
			}
			break;
		}
		case 0x18: { // bgeq
			if (!isN()) {
				int src = getInt(opinfo.getType1(), opinfo.getArg1(), opinfo.getAddr1());
				//System.out.printf("pc = %x\n", src);
				setPc(src);				
			}			
			break;
		}
		case 0x19: { // blss
			if (isN()) {
				int src = getInt(opinfo.getType1(), opinfo.getArg1(), opinfo.getAddr1());
//				System.out.printf("pc = %x\n", src);
				setPc(src);
			}
			break;
		}
		case 0xfb: { // calls
			int arg = getInt(opinfo.getType1(), opinfo.getArg1(), opinfo.getAddr1());
			//int next = getInt(opinfo.getType2(), opinfo.getArg2(), opinfo.getAddr2());
			int next = (int)opinfo.getAddr2();
			//System.out.printf("arg = %x, next = %x\n", arg, next);
			
			pushInt(arg); // push argument
			int tmpSp = reg[sp]; // keep current sp
			//System.out.printf("tmpSp = 0x%x\n", tmpSp);
			
			byte last2bit = (byte)(reg[sp] & 0x3);
			reg[sp] &= 0xfffffffc;
			//System.out.printf("last2bit = %x\n", last2bit);
			//System.out.printf("alined Sp = 0x%x\n", reg[sp]);
			
			
			int nextPc = memory.getCurrentPc(); // keep next pc
			//System.out.printf("nextPc = 0x%x\n", nextPc);
			setPc(next);                        // set pc to callee
			short entryMask = fetch2().sval;    // get entry mask 2bytes
			//System.out.printf("entry mask = %x\n", entryMask);
			short regMask = (short)(entryMask << 4);
			for (int i = 11; i >= 0; --i, regMask <<= 1) {
				if ((regMask & 0x8000) != 0) {
					//System.out.printf("push reg[%d]\n", i);
					pushInt(reg[i]); // push corresponding register to the stack
				}
			}
			//System.out.printf("mask = 0x%x\n", entryMask);
			
			// push each register value to the stack
			pushInt(nextPc);
			pushInt(reg[fp]);
			pushInt(reg[ap]);
			reg[ap] = tmpSp;			
			
			setNZVC(false, false, false, false);
		
			// create mask info
			int maskinfo = 0x20000000 | (last2bit << 30); // add last2bit of sp. 29,28 are fixed
			//System.out.printf("maskinfo1 = %x\n", maskinfo);
			maskinfo |= (entryMask << 16) & 0x0fff0000;
			//System.out.printf("maskinfo2 = %x\n", maskinfo);
			maskinfo |= psl & 0xffff;
			//System.out.printf("maskinfo3 = %x\n", maskinfo);
			
			pushInt(maskinfo);
			pushInt(0);
			reg[fp] = reg[sp];
			
			// new psl
			psl |= (entryMask >> 9) & 0x20; //IV bit
			psl |= (entryMask >> 8) & 0x80; //DV bit
			
			/**  //for debug
			logOut.reset();
			storeRegInfo();
			System.out.println(new String(logOut.toByteArray()));
			logOut.reset();
			memory.dump(reg[sp], 0xffff - reg[sp] + 1);
			**/ // end of debug
				
			break;
		}
		case 0x4: { // ret			
			reg[sp] = reg[fp] + 4; // restore stack pointer
			int maskinfo = popInt();
			//System.out.printf("sp = %x\n", reg[sp]);
			//System.out.printf("maskinfo = %x\n", maskinfo);
			
			byte last2bit = (byte)((maskinfo >> 30) & 3); // keep last 2bit
			//System.out.printf("last2bit = %x\n", last2bit);
			boolean callsFlg = ((maskinfo >> 29) & 1) == 1; // otherwise, callg
			short regMask = (short)((maskinfo >> 16) & 0xfff);
			//System.out.printf("regMask = %x\n", regMask);
			psl |= (maskinfo & 0xffff); // restore psw
			
			// restore register
			reg[ap] = popInt();
			reg[fp] = popInt();
			setPc(popInt());
			
			//System.out.printf("nextPc = %x\n", reg[pc]);
			for (int i = 0; i <= 11; ++i, regMask >>= 1) {
				if ((regMask & 1) == 1) {
					reg[i] = popInt();            // restore r0-r11 based on the entry mask
					//System.out.printf("push reg[%d] = 0x%x\n", i, reg[i]);
				}				
			}
			//System.out.printf("sp =  %x\n", reg[sp]);
			reg[sp] += last2bit; // restore alignment
			//System.out.printf("sp =  %x\n", reg[sp]);
			if (callsFlg) { // if comming from calls
				int argnum = popInt();            // restore arg number
				//System.out.printf("sp =  %x\n", reg[sp]);
				reg[sp] += (argnum & 0xff) << 2;  // remove arguments
				//System.out.printf("sp =  %x\n", reg[sp]);
			}
			break;
			//System.exit(1);
		}
		case 0x11: { // brb
			int nextPc = getInt(opinfo.getType1(), opinfo.getArg1(), opinfo.getAddr1());
			setPc(nextPc);			
			break;
		}
		case 0x1e: { // bcc
			if (!isC()) {
				int nextPc = getInt(opinfo.getType1(), opinfo.getArg1(), opinfo.getAddr1());
				setPc(nextPc);
			}
			break;
		}
		case 0xd4: { // clrl(f)
			storeInt(opinfo.getType1(), opinfo.getAddr1(), 0);
			setNZVC(false, true, false, isC());			
			break;
			
		}
		case 0xd7: { // decl
			int src = getInt(opinfo.getType1(), opinfo.getAddr1(), opinfo.getAddr1());
			val64 = (long)src - 1;
			val32 = (int)val64;
			setNZVC(val32 < 0, val32 == 0, val64 != val32, (src & 0xffffffffL) < (1 & 0xffffffffL));
			/*			
			System.out.printf("addr = 0x%x\n", opinfo.getAddr1());
			System.out.printf("src = 0x%x\n", src);			
			logOut.reset();
			storeRegInfo();
			System.out.println(new String(logOut.toByteArray()));
			System.exit(1);
			*/
			break;
		}
		case 0xdf: { // pushal
			int val32 = (int)opinfo.getAddr1();
			//System.out.printf("val32 in pushal = %x\n", val32);
			pushInt(val32);
			setNZVC(val32 < 0, val32 == 0, false, isC());
			/*
			logOut.reset();
			storeRegInfo();
			System.out.println(new String(logOut.toByteArray()));
			memory.dump(reg[sp], 4);			
			System.exit(1);
			*/
			break;
		}
		case 0xe0: { // bbs
			int pos = getInt(opinfo.getType1(), opinfo.getArg1(), opinfo.getAddr1());
			int base = getInt(opinfo.getType2(), opinfo.getArg2(), opinfo.getAddr2());
			int addr = (int)opinfo.getAddr3();
			/*
			System.out.printf("0xc(r11) = ");
			memory.dump(reg[r11] + 0xc, 4);
			System.out.println();
			System.out.printf("pos = %d, base = %x, addr = %x\n", pos, base, addr);
			*/			
			if (((base >>= pos) & 1) == 1) {
				setPc(addr);
			}
			//System.out.printf("pos = %d, base = %x, addr = %x\n", pos, base, addr);
			break;
		}
		case 0xe1: { // bbc
			int pos = getInt(opinfo.getType1(), opinfo.getArg1(), opinfo.getAddr1());
			int base = getInt(opinfo.getType2(), opinfo.getArg2(), opinfo.getAddr2());
			int addr = (int)opinfo.getAddr3();
			
			if (((base >>= pos) & 1) == 0) {
				setPc(addr);
			}			
			break;
		}		
		case 0x98: { //cvtbl
			byte src = getByte(opinfo.getType1(), opinfo.getArg1(), opinfo.getAddr1());
			//System.out.printf("addr1 = %x\n", opinfo.getAddr1());
			val32 = src;	// signed convert		
			storeInt(opinfo.getType2(), opinfo.getAddr2(), val32);
			
			setNZVC(val32 < 0, val32 == 0, false, false); // V is always false beccause
			//memory.dump((int)opinfo.getAddr2(), 4);     // small will be converted to large
			
			break;
		}
		case 0xf6: { // cvtlb
			int src = getInt(opinfo.getType1(), opinfo.getAddr1(), opinfo.getAddr1());
			byte dst = (byte)src;
			//System.out.printf("addr1 = %x, src = %x\n", opinfo.getAddr1(), src);
			//memory.dump((int)opinfo.getAddr1(), 4);
			//System.out.printf("addr2 = %x\n", opinfo.getAddr2());			
			//memory.dump((int)opinfo.getAddr2(), 4);
			
			storeByte(opinfo.getType2(), opinfo.getAddr2(), dst);
			
			//memory.dump((int)opinfo.getAddr2(), 4);
			setNZVC(dst < 0, dst == 0, src != dst, false);
			
			//System.exit(1);
			break;
		}
		case 0x88: { //bisb2
			int laddr = 0;
			Type ltype;
			byte mask = getByte(opinfo.getType1(), opinfo.getArg1(), opinfo.getAddr1());
			byte dst = getByte(ltype = opinfo.getType2(), opinfo.getArg2(), laddr = (int)opinfo.getAddr2());
			//System.out.printf("addr2 = %x\n", laddr);
			//System.out.printf("mask = %x, dst = %x\n", mask, dst);
			//memory.dump(0x620, 4);
			val32 = (dst |= mask);
			//System.out.printf("mask = %x, dst = %x\n", mask, dst);
			storeByte(ltype, laddr, dst);
			//memory.dump(0x620, 4);
			setNZVC(val32 < 0, val32 == 0, false, isC());			
			break;			
		}
		case 0xce: { // mnegl
			int src = getInt(opinfo.getType1(), opinfo.getArg1(), opinfo.getAddr1());
			//System.out.printf("src = %x\n", src);
			//src = 0x80000000;
			//src = 0x7fffffff;
			//System.out.printf("src = %d\n", src);
			val64 = (long)src * -1;
			val32 = (int)val64;
			//System.out.printf("val64 = %d, val32 = %d\n", val64, val32);
			storeInt(opinfo.getType2(), opinfo.getAddr2(), val32);
			setNZVC(val32 < 0, val32 == 0, val64 != val32, val32 != 0);			
			break;
		}
		case 0x93: { // bitb
			byte mask = getByte(opinfo.getType1(), opinfo.getArg1(), opinfo.getAddr1());
			byte src  = getByte(opinfo.getType2(), opinfo.getArg2(), opinfo.getAddr2());
			//System.out.printf("mask = %x, src = %x\n", mask, src);
			//System.out.printf("addr2 = %x\n", opinfo.getAddr2());
			//memory.dump((int)opinfo.getAddr2(), 4);
			val32 = val8 = (byte)(mask & src);
			setNZVC(val32 < 0, val32 == 0, false, isC());			
			break;
		}		
		case 0xca: { // bicl2
			int mask = getInt(opinfo.getType1(), opinfo.getArg1(), opinfo.getAddr1());
			int dst  = getInt(opinfo.getType2(), opinfo.getArg2(), opinfo.getAddr2());						
//			System.out.printf("mask = %x, dst = %x\n", mask, dst);
			val32 = dst & ~mask;
			setNZVC(val32 < 0, val32 == 0, false, isC());					
			break;
		}
		case 0x8a: { // bicb2
			byte mask = getByte(opinfo.getType1(), opinfo.getArg1(), opinfo.getAddr1());
			byte dst  = getByte(opinfo.getType2(), opinfo.getArg2(), opinfo.getAddr2());
			//System.out.printf("mask = %x, dst = %x\n", mask, dst);
			//System.out.printf("addr2 = %x\n", opinfo.getAddr2());
			//memory.dump(0x610, 4);
			val32 = val8 = (byte)(dst & ~mask);
			setNZVC(val32 < 0, val32 == 0, false, isC());
			break;
		}
		case 0xc0: { // addl2
			long laddr;
			int src = getInt(opinfo.getType1(), opinfo.getArg1(), opinfo.getAddr1());
			int dst = getInt(opinfo.getType2(), opinfo.getArg2(), laddr = opinfo.getAddr2());
			//System.out.printf("src = %x, dst = %x\n", src, dst);			
			val64 = (long)src + (long)dst;
			val32 = (int)val64;
			storeInt(opinfo.getType2(), (int)laddr, val32);
			setNZVC(val32 < 0, val32 == 0, val64 != val32, (src & 0xffffffffL) + (dst & 0xffffffffL) >= 0x100000000L);			
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
