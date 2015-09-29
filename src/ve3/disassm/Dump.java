package ve3.disassm;

import ve3.disassm.V32Disassm.OT;
import ve3.os.OpInfo;
import ve3.os.OpInfo.Type;

public class Dump {
	private static final String[] regs = {
			"r0", "r1", "r2", "r3", "r4", "r5", "r6", "r7", "r8",
			"r9", "r10", "r11", "ap", "fp", "sp", "pc"
	};
	
	private static String createOperand(Type type, byte operand, int arg, OT ot) {
		switch(type) {
		case Literal: { // 0~3
			return String.format("$0x%x", operand);
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
		case ByteDisp: { // 0xa
			return String.format("0x%x(%s)", arg, regs[operand]);	
		}
		case ByteDispDefer: { // 0x0b
			return String.format("*0x%x(%s)", arg, regs[operand]);	
		}
		case WordDisp: { // 0x0c
			return String.format("0x%x(%s)", arg, regs[operand]);
		}
		case WordDispDefer: { // 0x0d
			return String.format("*0x%x(%s)", arg, regs[operand]);	
		}
		case Immed: {
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
			case f: {
				fmt = "$0x%08x [f-floot]";
				break;
			}
			}
			return String.format(fmt, arg);
		}
		case WordRel: {
			return String.format("0x%x", arg);
		}
		case LongRel: { // 0xe : program conter mode
			return String.format("0x%x", arg);			
		}
		case Branch1: { // branch disp byte
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
}
