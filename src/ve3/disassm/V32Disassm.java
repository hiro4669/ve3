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
	private int tsize;

	public static enum AdMode {
		Branch, General, PC
	}
	
	/* b:byte, w:word, l:long, Brb:Branch byte, Brw:Branch word */
	public static enum OT {
		b, w, l, f, q, df, ebl, Brb, Brw 
	}
	
	enum Ope {

		DUMMY1(0x5bd0, new MetaInfo()), DUMMY2(0x5be0, new MetaInfo()),
		DUMMY3(0x599a, new MetaInfo()), DUMMY4(0x5b98, new MetaInfo()),
		DUMMY5(0xffff, new MetaInfo()), DUMMY6(0xff50, new MetaInfo()),
		DUMMY7(0x5b11, new MetaInfo()), DUMMY8(0x7700, new MetaInfo()),
		
		HALT(0x00, new MetaInfo()), MOVL(0xd0, new MetaInfo(OT.l, OT.l)), 
		TSTL(0xd5, new MetaInfo(OT.l)), BNEQ(0x12, new MetaInfo(OT.Brb)),
		BLSS(0x19, new MetaInfo(OT.Brb)),
		CALLS(0xfb, new MetaInfo(OT.l, OT.b)), PUSHL(0xdd, new MetaInfo(OT.l)),
		CHMK(0xbc, new MetaInfo(OT.w)), PROBER(0x0c, new MetaInfo(OT.b, OT.w, OT.b)),
		NOP(0x01, new MetaInfo()), BLEQ(0x15, new MetaInfo(OT.Brb)),
		CMPB(0x91, new MetaInfo(OT.b, OT.b)), CMPD(0x71, new MetaInfo(OT.df, OT.df)), 
		CMPF(0x51, new MetaInfo(OT.f, OT.f)), CMPL(0xd1, new MetaInfo(OT.l, OT.l)), 
		DECL(0xd7, new MetaInfo(OT.l)), ADDL2(0xc0, new MetaInfo(OT.l, OT.l)),
		BRB(0x11, new MetaInfo(OT.Brb)), PUSHAL(0xdf, new MetaInfo(OT.l)),
		SUBL3(0xc3, new MetaInfo(OT.l, OT.l, OT.l)), BGEQ(0x18, new MetaInfo(OT.Brb)),
		CVTLB(0xf6, new MetaInfo(OT.l, OT.b)), CVTBL(0x98, new MetaInfo(OT.b, OT.l)),
		RET(0x4, new MetaInfo()), REMQUE(0x0f, new MetaInfo(OT.b, OT.l)),
		XBICW3(0xab, new MetaInfo(OT.w, OT.w, OT.w)), SUBW3(0xa3, new MetaInfo(OT.w, OT.w, OT.w)),
		BEQL(0x13, new MetaInfo(OT.Brb)), 
		BBC(0xe1, new MetaInfo(OT.l, OT.b, OT.Brb)), CLRF(0xd4, new MetaInfo(OT.f)),
		CVTWL(0x32, new MetaInfo(OT.w, OT.l)),
		BCC(0x1e, new MetaInfo(OT.Brb)), JMP(0x17, new MetaInfo(OT.b)),
		ADDL3(0xc1, new MetaInfo(OT.l, OT.l, OT.l)), MNEGL(0xce, new MetaInfo(OT.l, OT.l)),
		REMQHI(0x5e, new MetaInfo(OT.q, OT.l)), BICL2(0xca, new MetaInfo(OT.l, OT.l)),
		MOVB(0x90, new MetaInfo(OT.b, OT.b)), 
		BLBC(0xe9, new MetaInfo(OT.l, OT.Brb)), BLBS(0xe8, new MetaInfo(OT.l, OT.Brb)),
		SUBD3(0x63, new MetaInfo(OT.df, OT.df, OT.df)), DIVD3(0x67, new MetaInfo(OT.df, OT.df, OT.df)),
		SPANC(0x2b, new MetaInfo(OT.w, OT.b, OT.b, OT.b)), 
		CVTWB(0x33, new MetaInfo(OT.w, OT.b)), CVTPL(0x36, new MetaInfo(OT.w, OT.b, OT.l)),
		ADDD3(0x61, new MetaInfo(OT.df, OT.df, OT.df)), 
		BICB3(0x8b, new MetaInfo(OT.b, OT.b, OT.b)), MOVF(0x50, new MetaInfo(OT.f, OT.f)),
		CVTWF(0x4d, new MetaInfo(OT.w, OT.f)), POPR(0xba, new MetaInfo(OT.w)),
		MOVTC(0x2e, new MetaInfo(OT.w, OT.b, OT.b, OT.w, OT.b, OT.b)),
		CLRB(0x94, new MetaInfo(OT.b)), 
		CVTDF(0x76, new MetaInfo(OT.df, OT.f)), 
		BRW(0x31, new MetaInfo(OT.Brw)), MOVZBL(0x9a, new MetaInfo(OT.ebl, OT.l)),
		MOVC3(0x28, new MetaInfo(OT.w, OT.b, OT.b)), CVTBD(0x6c, new MetaInfo(OT.b, OT.df)),
		BBS(0xe0, new MetaInfo(OT.l, OT.b, OT.Brb)), LOCC(0x3a, new MetaInfo(OT.b, OT.w, OT.b)),
		EXTZV(0xef, new MetaInfo(OT.l, OT.b, OT.b, OT.l)), ACBL(0xf1, new MetaInfo(OT.l, OT.l, OT.l, OT.Brw)),
		SKPC(0x3b, new MetaInfo(OT.b, OT.w, OT.b)), 
		CVTLP(0xf9, new MetaInfo(OT.l, OT.w, OT.b)), EDITPC(0x38, new MetaInfo(OT.w, OT.b, OT.b, OT.b)),
		BVC(0x1c, new MetaInfo(OT.Brb)), BLSSU(0x1f, new MetaInfo(OT.Brb)),
		CVTLD(0x6e, new MetaInfo(OT.l, OT.df)), INDEX(0xa, new MetaInfo(OT.l, OT.l, OT.l, OT.l, OT.l, OT.l)),
		DIVF3(0x47, new MetaInfo(OT.f, OT.f, OT.f)), INSQUE(0x0e, new MetaInfo(OT.b, OT.b)),
		CVTPS(0x08, new MetaInfo(OT.w, OT.b, OT.w, OT.b)), BITB(0x93, new MetaInfo(OT.b, OT.b)),
		BICB2(0x8a, new MetaInfo(OT.b, OT.b)), TSTB(0x95, new MetaInfo(OT.b)),
		DIVL3(0xc7, new MetaInfo(OT.l, OT.l, OT.l)), ASHL(0x78, new MetaInfo(OT.b, OT.l, OT.l)),
		BICL3(0xcb, new MetaInfo(OT.l, OT.l, OT.l)), BGTR(0x14, new MetaInfo(OT.Brb)),
		DIVL2(0xc6, new MetaInfo(OT.l, OT.l)), DECW(0xb7, new MetaInfo(OT.w)),
		MOVZWL(0x3c, new MetaInfo(OT.w, OT.l)), TSTW(0xb5, new MetaInfo(OT.w)),
		CVTLW(0xf7, new MetaInfo(OT.l, OT.w)), BPT(0x3, new MetaInfo()),
		EMODD(0x74, new MetaInfo(OT.df, OT.b, OT.df, OT.l, OT.df)), MOVTUC(0x2f, new MetaInfo(OT.w, OT.b, OT.b, OT.b, OT.w, OT.b)),
		ADAWI(0x58, new MetaInfo(OT.w, OT.w)), CVTDW(0x69, new MetaInfo(OT.df, OT.w)),
		ADDP4(0x20, new MetaInfo(OT.w, OT.b, OT.w, OT.b)), MOVD(0x70, new MetaInfo(OT.df, OT.df)),
		ACBD(0x6f, new MetaInfo(OT.df, OT.df, OT.df, OT.Brw)), MNEGD(0x72, new MetaInfo(OT.df, OT.df)),
		SUBF3(0x43, new MetaInfo(OT.f, OT.f, OT.f)), TSTD(0x73, new MetaInfo(OT.df)),
		DIVD2(0x66, new MetaInfo(OT.df, OT.df)), CVTFW(0x49, new MetaInfo(OT.f, OT.w)),
		CVTBF(0x4c, new MetaInfo(OT.b, OT.f)), TSTF(0x53, new MetaInfo(OT.f)),
		CVTWD(0x6d, new MetaInfo(OT.w, OT.df)), RSB(0x5, new MetaInfo()),
		POLYF(0x55, new MetaInfo(OT.f, OT.w, OT.b)), POLYD(0x75, new MetaInfo(OT.df, OT.w, OT.b)),
		ADDF2(0x40, new MetaInfo(OT.f, OT.f)), REI(0x2, new MetaInfo()),
		CMPC3(0x29, new MetaInfo(OT.w, OT.b, OT.b)), CMPC5(0x2d, new MetaInfo(OT.w, OT.b, OT.b, OT.w, OT.b)),
		CMPP3(0x35, new MetaInfo(OT.w, OT.b, OT.b)), CMPP4(0x37, new MetaInfo(OT.w, OT.b, OT.w, OT.b)),
		LDPCTX(0x6, new MetaInfo()), SVPCTX(0x7, new MetaInfo()),
		MOVC5(0x2c, new MetaInfo(OT.w, OT.b, OT.b, OT.w, OT.b)),
		SUBB2(0x82, new MetaInfo(OT.b, OT.b)), SUBW2(0xa2, new MetaInfo(OT.w, OT.w)),
		SUBL2(0xc2, new MetaInfo(OT.l, OT.l)), SUBF2(0x42, new MetaInfo(OT.f, OT.f)),
		SUBD2(0x62, new MetaInfo(OT.df, OT.df)), MULP(0x25, new MetaInfo(OT.w, OT.b, OT.w, OT.b, OT.w, OT.b)),
		MOVAB(0x9e,new MetaInfo(OT.b, OT.l)), MOVAW(0x3e,new MetaInfo(OT.w, OT.l)),
		MOVAL(0xde,new MetaInfo(OT.l, OT.l)), MOVAQ(0x7e,new MetaInfo(OT.q, OT.l)),
		XORB2(0x8c, new MetaInfo(OT.b, OT.b)), XORW2(0xac, new MetaInfo(OT.w, OT.w)), XORL2(0xcc, new MetaInfo(OT.l, OT.l)),
		XORB3(0x8d, new MetaInfo(OT.b, OT.b, OT.b)), XORW3(0xad, new MetaInfo(OT.w, OT.w, OT.w)), XORL3(0xcd, new MetaInfo(OT.l, OT.l, OT.l)),
		SOBGEQ(0xf4, new MetaInfo(OT.l, OT.Brb)), SOBGTR(0xf5, new MetaInfo(OT.l, OT.Brb)),
		ASHP(0xf8, new MetaInfo(OT.b, OT.w, OT.b, OT.b, OT.w, OT.b)),
		CMPV(0xec, new MetaInfo(OT.l, OT.b, OT.b, OT.l)), CMPZV(0xed, new MetaInfo(OT.l, OT.b, OT.b, OT.l)),
		AOBLSS(0xf2, new MetaInfo(OT.l, OT.l, OT.Brb)), AOBLEQ(0xf3, new MetaInfo(OT.l, OT.l, OT.Brb)),
		BSBB(0x10, new MetaInfo(OT.Brb)), BSBW(0x30, new MetaInfo(OT.Brw)),		
		MULB2(0x84, new MetaInfo(OT.b, OT.b)), MULB3(0x85, new MetaInfo(OT.b, OT.b, OT.b)), 
		MULW2(0xa4, new MetaInfo(OT.w, OT.w)), MULW3(0xa5, new MetaInfo(OT.w, OT.w, OT.w)), 
		MULL2(0xc4, new MetaInfo(OT.l, OT.l)), MULL3(0xc5, new MetaInfo(OT.l, OT.l, OT.l)), 
		MULF2(0x44, new MetaInfo(OT.f, OT.f)), MULF3(0x45, new MetaInfo(OT.f, OT.f, OT.f)), 
		MULD2(0x64, new MetaInfo(OT.df, OT.df)), MULD3(0x65, new MetaInfo(OT.df, OT.df, OT.df)),
		MCOMB(0x92, new MetaInfo(OT.b, OT.b)), MCOMW(0xb2, new MetaInfo(OT.w, OT.w)), MCOML(0xd2, new MetaInfo(OT.l, OT.l)),
		BISB2(0x88, new MetaInfo(OT.b, OT.b)), BISB3(0x89, new MetaInfo(OT.b, OT.b, OT.b)),
		BISW2(0xa8, new MetaInfo(OT.w, OT.w)), BISW3(0xa9, new MetaInfo(OT.w, OT.w, OT.w)),
		BISL2(0xc8, new MetaInfo(OT.l, OT.l)), BISL3(0xc9, new MetaInfo(OT.l, OT.l, OT.l)),
		BISPSW(0xb8, new MetaInfo(OT.w)), BICPSW(0xb9, new MetaInfo(OT.w)),
		CASEB(0x8f, new MetaInfo(OT.b, OT.b, OT.b)), CASEW(0xaf, new MetaInfo(OT.w, OT.w, OT.w)),
		CASEL(0xcf, new MetaInfo(OT.l, OT.l, OT.l)), CVTRFL(0x4b, new MetaInfo(OT.f, OT.l)),
		CVTDL(0x6a, new MetaInfo(OT.df, OT.l)), INCB(0x96, new MetaInfo(OT.b)),
		INCW(0xb6, new MetaInfo(OT.w)), INCL(0xd6, new MetaInfo(OT.l)),
		MOVP(0x34, new MetaInfo(OT.w, OT.b, OT.b)),
		BBSS(0xe2, new MetaInfo(OT.l, OT.b, OT.Brb)), BBCS(0xe3, new MetaInfo(OT.l, OT.b, OT.Brb)),
		BBSC(0xe4, new MetaInfo(OT.l, OT.b, OT.Brb)), BBCC(0xe5, new MetaInfo(OT.l, OT.b, OT.Brb));
		
		
		public final int mne;		
		public final String opname;
		public final MetaInfo minfo;
		public static Ope[] table = new Ope[0xfffff];
		
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
		tsize = readInt(rawdata, 4);
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
		case 0x9: { // Absolute
			opinfo.opsub.type = Type.Abs;
			opinfo.opsub.arg = memory.fetch4();
			return opinfo.opsub;
		}
		case 0xc: { // word relative
			opinfo.opsub.type = Type.WordRel;
			opinfo.opsub.arg = memory.fetch2() + memory.getCurrentPc();
			return opinfo.opsub;
		}
		case 0xd: { // word relative deferred
			opinfo.opsub.type = Type.WordRelDefer;
			opinfo.opsub.arg = memory.fetch2() + memory.getCurrentPc();
			return opinfo.opsub;
		}
		case 0xe: { // long relative
			opinfo.opsub.type = Type.LongRel;
			opinfo.opsub.arg = memory.fetch4() + memory.getCurrentPc();
			return opinfo.opsub;
		}
		case 0xf: { // long relative deferred
			opinfo.opsub.type = Type.LongRelDefer;
			opinfo.opsub.arg = memory.fetch4() + memory.getCurrentPc();
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
		case 0x0f: { // Long Displacement Deferred
			if (value == 0xf) return resolveDispPc(optype, type);
			opinfo.opsub.type = Type.LongDispDefer;
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
		OpInfoSub opsub = null;
		while ((opsub = resolveDisp(minfo.arg1)).type == Type.Index) {
			opinfo.pushIdx1(opsub.operand);
		}		
		opinfo.setType1(opsub.type);
		opinfo.setOpe1(opsub.operand);
		opinfo.setArg1(opsub.arg);
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
	}
	
	private void setArg3(MetaInfo minfo) {
		setArg2(minfo);
		OpInfoSub opsub;
		while ((opsub = resolveDisp(minfo.arg3)).type == Type.Index) {
			opinfo.pushIdx3(opsub.operand);
		}
		
		opinfo.setType3(opsub.type);
		opinfo.setOpe3(opsub.operand);
		opinfo.setArg3(opsub.arg);
	}
	
	private void setArg4(MetaInfo minfo) {
		setArg3(minfo);
		OpInfoSub opsub = null;
		while ((opsub = resolveDisp(minfo.arg4)).type == Type.Index) {
			opinfo.pushIdx4(opsub.operand);
		}
		
		opinfo.setType4(opsub.type);
		opinfo.setOpe4(opsub.operand);
		opinfo.setArg4(opsub.arg);
	}
	
	private void setArg5(MetaInfo minfo) {
		setArg4(minfo);
		OpInfoSub opsub;		
		while((opsub = resolveDisp(minfo.arg5)).type == Type.Index) {
			opinfo.pushIdx5(opsub.operand);
		}
		
		opinfo.setType5(opsub.type);
		opinfo.setOpe5(opsub.operand);
		opinfo.setArg5(opsub.arg);		
	}
	
	private void setArg6(MetaInfo minfo) {
		setArg5(minfo);
		OpInfoSub opsub;
		while ((opsub = resolveDisp(minfo.arg6)).type == Type.Index) {
			opinfo.pushIdx6(opsub.operand);
		}
		
		opinfo.setType6(opsub.type);
		opinfo.setOpe6(opsub.operand);
		opinfo.setArg6(opsub.arg);		
	}
	
	
	
	private String format(int index, byte[] rawdata, String s) {
		//return String.format("%4x:   %s    %s", index, new String(rawdata), s);
		return String.format("%4x:\t%s\t%s", index, new String(rawdata), s);
	}
	private String format(int index, byte[] rawdata) {
		//return String.format("%4x:   %s", index, new String(rawdata));	
		return String.format("%4x:\t%s", index, new String(rawdata));	
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
			if (memory.getCurrentPc() > tsize) break;
			
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
