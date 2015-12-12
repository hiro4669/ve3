package ve3.os;

import java.util.EmptyStackException;
import java.util.Stack;

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
	private Stack<Byte> idxs1 = new Stack<Byte>();
	
	private Type type2;
	private byte ope2;
	private long arg2;
	private byte idx2;
	private Stack<Byte> idxs2 = new Stack<Byte>();
	
	private Type type3;
	private byte ope3;
	private long arg3;
	private byte idx3;
	private Stack<Byte> idxs3 = new Stack<Byte>();
	
	private Type type4;
	private byte ope4;
	private long arg4;
	private byte idx4;
	private Stack<Byte> idxs4 = new Stack<Byte>();
	
	private Type type5;
	private byte ope5;
	private long arg5;
	private byte idx5;
	private Stack<Byte> idxs5 = new Stack<Byte>();
	
	private Type type6;
	private byte ope6;
	private long arg6;
	private byte idx6;
	private Stack<Byte> idxs6 = new Stack<Byte>();
	
	
	
	
	private MetaInfo minfo;
		
	public OpInfoSub opsub;
		
	public OpInfo() {
		opsub = new OpInfoSub();
		clear();
	}
	
	public void clear() {
		opcode = -1;
		idx1 = idx2 = idx3 = idx4 = idx5 = idx6 = -1;
		idxs1.clear();
		idxs2.clear();
		idxs3.clear();
		idxs4.clear();
		idxs5.clear();
		idxs6.clear();		
		minfo = null;
		type1 = type2 = type3 = type4 = type5 = type6 =Type.NoType;
		ope1 = ope2 = ope3 = ope4 = ope5 = ope6 = 0;
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
	/* for operand1 */
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
	public void pushIdx1(byte id) {
		idxs1.push(id);
	}
	public byte popIdx1() {
		try {
			return idxs1.pop();
		} catch (EmptyStackException e) {
			return -1;
		}
	}
	public boolean hasIdx1() {
		return !idxs1.isEmpty();
	}
	
	
	/* for operand2 */
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
	public void pushIdx2(byte id) {
		idxs2.push(id);
	}
	public byte popIdx2() {
		try {
			return idxs2.pop();
		} catch (EmptyStackException e) {
			return -1;
		}
	}
	public boolean hasIdx2() {
		return !idxs2.isEmpty();
	}
	
	/* for operand3 */
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
	public void pushIdx3(byte id) {
		idxs3.push(id);
	}
	public byte popIdx3() {
		try {
			return idxs3.pop();
		} catch (EmptyStackException e) {
			return -1;
		}
	}
	public boolean hasIdx3() {
		return !idxs3.isEmpty();
	}
	
	/* for operand4 */
	public void setType4(Type type4) {
		this.type4 = type4;
	}
	public Type getType4() {
		return type4;
	}
	public void setOpe4(byte ope4) {
		this.ope4 = ope4;
	}
	public byte getOpe4() {
		return ope4;
	}
	public void setArg4(long arg4) {
		this.arg4 = arg4;
	}
	public long getArg4() {
		return arg4;
	}
	public void setIdx4(byte idx4) {
		this.idx4 = idx4;
	}
	public byte getIdx4() {
		return idx4;
	}
	public void pushIdx4(byte id) {
		idxs4.push(id);
	}
	public byte popIdx4() {
		try {
			return idxs4.pop();
		} catch (EmptyStackException e) {
			return -1;
		}
	}
	public boolean hasIdx4() {
		return !idxs4.isEmpty();
	}
	
	/* for operand5 */
	public void setType5(Type type5) {
		this.type5 = type5;
	}
	public Type getType5() {
		return type5;
	}
	public void setOpe5(byte ope5) {
		this.ope5 = ope5;
	}
	public byte getOpe5() {
		return ope5;
	}
	public void setArg5(long arg5) {
		this.arg5 = arg5;
	}
	public long getArg5() {
		return arg5;
	}
	public void setIdx5(byte idx5) {
		this.idx4 = idx5;
	}
	public byte getIdx5() {
		return idx5;
	}
	public void pushIdx5(byte id) {
		idxs5.push(id);
	}
	public byte popIdx5() {
		try {
			return idxs5.pop();
		} catch (EmptyStackException e) {
			return -1;
		}
	}
	public boolean hasIdx5() {
		return !idxs5.isEmpty();
	}
	
	/* for operand6 */
	public void setType6(Type type6) {
		this.type6 = type6;
	}
	public Type getType6() {
		return type6;
	}
	public void setOpe6(byte ope6) {
		this.ope6 = ope6;
	}
	public byte getOpe6() {
		return ope6;
	}
	public void setArg6(long arg6) {
		this.arg6 = arg6;
	}
	public long getArg6() {
		return arg6;
	}
	public void setIdx6(byte idx6) {
		this.idx6 = idx6;
	}
	public byte getIdx6() {
		return idx6;
	}
	public void pushIdx6(byte id) {
		idxs6.push(id);
	}
	public byte popIdx6() {
		try {
			return idxs6.pop();
		} catch (EmptyStackException e) {
			return -1;
		}
	}
	public boolean hasIdx6() {
		return !idxs6.isEmpty();
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
