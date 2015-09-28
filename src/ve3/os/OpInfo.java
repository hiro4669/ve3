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
	
	private byte opcode;
	private Type type1;
	private byte ope1;
	private int arg1;
	
	private Type type2;
	private byte ope2;
	private int arg2;
	
	private Type type3;
	private byte ope3;
	private int arg3;
	
	private MetaInfo minfo;
		
	public OpInfoSub opsub;
		
	public OpInfo() {
		opsub = new OpInfoSub();
		clear();
	}
	
	public void clear() {
		opcode = -1;
		minfo = null;
		type1 = type2 = type3 = Type.NoType;
		ope1 = ope2 = ope3 = 0;
	}
	
	public byte setOpCode(byte opcode) {
		return (this.opcode = opcode);
	}
	public byte getOpCode() {
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
	public void setArg1(int arg1) {
		this.arg1 = arg1;
	}
	public int getArg1() {
		return arg1;
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
	public void setArg2(int arg2) {
		this.arg2 = arg2;
	}
	public int getArg2() {
		return arg2;
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
	public void setArg3(int arg3) {
		this.arg3 = arg3;
	}
	public int getArg3() {
		return arg3;
	}
	
	
	public class OpInfoSub {
		public Type type;
		public byte operand;
		public int arg;
		
		public OpInfoSub() {
			type = Type.NoType;
			operand = 0;
			arg = 0;
		}
	}
	
	

}
