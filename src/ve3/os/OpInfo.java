package ve3.os;

public class OpInfo {
		
	
	public static enum Type {
		Literal, Index, Register, RegDefer, AutoDec, AutoInc,
		AutoIncDefer, ByteDisp, ByteDispDefer, WordDisp, WordDispDefer,
		LongDisp, LongDispDefer,
		Immed, Abs, ByteRel, ByteRelDefer, WordRel, WordRelDefer,
		LongRel, LongRelDefer,
		Branch1, Branch2,
		NoType
	}
	
	private int opcode;
	
	private Type type1;
	private byte ope1;
	private long arg1;
	private byte idx1;
	
	private Type type2;
	private byte ope2;
	private long arg2;
	private byte idx2;
	
	private Type type3;
	private byte ope3;
	private long arg3;
	private byte idx3;
	
	private MetaInfo minfo;
		
	public OpInfoSub opsub;
		
	public OpInfo() {
		opsub = new OpInfoSub();
		clear();
	}
	
	public void clear() {
		opcode = -1;
		idx1 = idx2 = idx3 = -1;
		minfo = null;
		type1 = type2 = type3 = Type.NoType;
		ope1 = ope2 = ope3 = 0;
	}
	
	public int setOpCode(int opcode) {
		return (this.opcode = opcode);
	}
	public int getOpCode() {
		return opcode;		
	}
	
	public void setMetaInfo(MetaInfo minfo) {
		this.minfo = minfo;
	}
	public MetaInfo getMetaInfo() {
		return minfo;
	}
	
	public void setType1(Type type1) {
		this.type1 = type1;
	}
	public Type getType1() {
		return type1;
	}
	public void setOpe1(byte ope1) {
		this.ope1 = ope1;
	}
	public byte getOpe1() {
		return ope1;
	}	
	public void setArg1(long arg1) {
		this.arg1 = arg1;
	}
	public long getArg1() {
		return arg1;
	}
	public void setIdx1(byte idx1) {
		this.idx1 = idx1;
	}
	public byte getIdx1() {
		return idx1;
	}
	
	public void setType2(Type type2) {
		this.type2 = type2;
	}
	public Type getType2() {
		return type2;
	}
	public void setOpe2(byte ope2) {
		this.ope2 = ope2;
	}
	public byte getOpe2() {
		return ope2;
	}
	public void setArg2(long arg2) {
		this.arg2 = arg2;
	}
	public long getArg2() {
		return arg2;
	}
	public void setIdx2(byte idx2) {
		this.idx2 = idx2;
	}
	public byte getIdx2() {
		return idx2;
	}
	
	public void setType3(Type type3) {
		this.type3 = type3;
	}
	public Type getType3() {
		return type3;
	}
	public void setOpe3(byte ope3) {
		this.ope3 = ope3;
	}
	public byte getOpe3() {
		return ope3;
	}
	public void setArg3(long arg3) {
		this.arg3 = arg3;
	}
	public long getArg3() {
		return arg3;
	}
	public void setIdx3(byte idx3) {
		this.idx3 = idx3;
	}
	public byte getIdx3() {
		return idx3;
	}
	
	
	public class OpInfoSub {
		public Type type;
		public byte operand;
		public long arg;
		
		public OpInfoSub() {
			type = Type.NoType;
			operand = 0;
			arg = 0;
		}
	}
	
	

}
