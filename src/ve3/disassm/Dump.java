package ve3.disassm;

import ve3.disassm.V32Disassm.OT;
import ve3.os.OpInfo;
import ve3.os.OpInfo.Type;

public class Dump {
	private static final String[] regs = {
			"r0", "r1", "r2", "r3", "r4", "r5", "r6", "r7", "r8",
			"r9", "r10", "r11", "ap", "fp", "sp", "pc"
	};
	
	private static String getArgStr(OT ot, long arg) {
		
		switch(ot) {
		case b:
			return String.format("0x%x", (byte)arg);
		case ebl: {
			return String.format("0x%x", (int)arg);
		}
		case w: 
			return String.format("0x%x", (short)arg);
		case l: 
		case f:{
			return String.format("0x%x", (int)arg);
		}
		case q:
		case df: {
			return String.format("0x%x", (long)arg);
		}
		default: {
			System.out.println("unrecognised optype(OT) in getARgStr");
			System.exit(1);
		}
		}
		return "";
		
	}
	
	private static String createOperand(Type type, byte operand, long arg, OT ot) {
		
		switch(type) {
		case Literal: { // 0~3
			String fmt = "$0x%x";
			switch(ot) {			
			case df: {
				fmt += " [d-floot]";
				break;
			}
			case f: {
				fmt += " [f-floot]";
				break;
			}
			}
			return String.format(fmt, operand);
		}
		case Index: {
			return "[" + regs[operand] + "]";
		}
		case Register: { // 5
			return regs[operand];
		}
		case RegDefer: { // 6
			return "(" + regs[operand] + ")";
		}
		case AutoDec: { // 7
			return "-(" + regs[operand] + ")";
		}
		case AutoInc: { // 8
			return "(" + regs[operand] + ")+";
		}		
		case AutoIncDefer: {
			return "@(" + regs[operand] + ")+";
		}
		case ByteDisp: { // 0xa
			//return String.format("0x%x(%s)", arg, regs[operand]);
			return getArgStr(ot, arg) + String.format("(%s)", regs[operand]);
		}
		case ByteDispDefer: { // 0x0b
			return "*" + getArgStr(ot, arg) + String.format("(%s)", regs[operand]);
			//return "*" + String.format("0x%x", arg) + String.format("(%s)", regs[operand]);
		}
		case WordDisp: { // 0x0c
			//return String.format("0x%x(%s)", arg, regs[operand]);
			return getArgStr(ot, arg) + String.format("(%s)", regs[operand]);
			//return String.format("0x%x", arg) + String.format("(%s)", regs[operand]);
		}
		case WordDispDefer: { // 0x0d
			//return String.format("*0x%x(%s)", arg, regs[operand]);
			return "*" + getArgStr(ot, arg) + String.format("(%s)", regs[operand]);
			//return "*" + String.format("0x%x", arg) + String.format("(%s)", regs[operand]);
		}
		case LongDisp: { // 0x0e
			return getArgStr(ot, arg) + String.format("(%s)", regs[operand]);
		}
		case Immed: {
			switch(ot) {
			case f: {
				return "$" + getArgStr(ot, arg) + " [f-float]";
			}
			case df: {
				return "$" + getArgStr(ot, arg) + " [d-float]";
			}
			default: {
				return "$" + getArgStr(ot, arg);
			}
			}
			
			/*
			String fmt = "$0x%x";
			switch(ot) {
			case b:
				fmt = "$0x%02x";
				break;
			case w: 
				fmt = "$0x%04x";
				break;
			case l: {
				fmt = "$0x%08x";
				break;
			}
			case q: {
				fmt = "$0x%016x";
				break;
			}
			case df: {
				fmt = "$0x%016x [d-floot]";
				break;
			}
			case f: {
				fmt = "$0x%08x [f-floot]";
				break;
			}
			}
			return String.format(fmt, arg);
			*/
		}
		case WordRel: { // program counter mode
			return String.format("0x%x", arg);
		}
		case LongRel: { // 0xe : program conter mode
			return String.format("0x%x", arg);
		}
		case Branch1: { // branch disp byte
			return String.format("0x%x", arg);
		}
		case Branch2: {
			return String.format("0x%x", arg);
		}
		default: {
			System.out.println("unrecognized type in Dump");
			System.exit(1);
			break;
		}
		}		
		return "";
	}
	
	
	
	public static String dump(OpInfo opinfo, String opname) {
		
		switch(opinfo.getMetaInfo().size) {
		case 0: {
			return opname;	
		}
		case 1: {
			return opname + " " + dump1(opinfo);
		}
		case 2: {
			return opname + " " + dump2(opinfo);
		}
		case 3: {
			return opname + " " + dump3(opinfo);
		}
		case 4: {
			return opname + " " + dump4(opinfo);
		}
		case 5: {
			return opname + " " + dump5(opinfo);
		}
		case 6: {
			return opname + " " + dump6(opinfo);
		}
		default: {
			System.out.println("unrecognized size in dump: " + opinfo.getMetaInfo().size);
			System.exit(1);
			return "";
			
		}
		}		
	}
	
	private static String dump1(OpInfo opinfo) {
		String s = createOperand(opinfo.getType1(), opinfo.getOpe1(), opinfo.getArg1(), opinfo.getMetaInfo().arg1);
		if (opinfo.getIdx1() != -1) {
			s += createOperand(Type.Index, opinfo.getIdx1(), 0, null);
		}
		return s;
	}
	
	private static String dump2(OpInfo opinfo) {
		return dump1(opinfo) + "," + createOperand(opinfo.getType2(), opinfo.getOpe2(), opinfo.getArg2(), opinfo.getMetaInfo().arg2);
	}
	
	private static String dump3(OpInfo opinfo) {
		return dump2(opinfo) + "," + createOperand(opinfo.getType3(), opinfo.getOpe3(), opinfo.getArg3(), opinfo.getMetaInfo().arg3);
	}
	
	private static String dump4(OpInfo opinfo) {
		return dump3(opinfo) + "," + createOperand(opinfo.getType4(), opinfo.getOpe4(), opinfo.getArg4(), opinfo.getMetaInfo().arg4);
	}
	
	private static String dump5(OpInfo opinfo) {
		return dump4(opinfo) + "," + createOperand(opinfo.getType5(), opinfo.getOpe5(), opinfo.getArg5(), opinfo.getMetaInfo().arg5);
	}
	
	private static String dump6(OpInfo opinfo) {
		return dump5(opinfo) + "," + createOperand(opinfo.getType6(), opinfo.getOpe6(), opinfo.getArg6(), opinfo.getMetaInfo().arg6);
	}
}
