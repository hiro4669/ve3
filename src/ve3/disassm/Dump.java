package ve3.disassm;

import ve3.os.OpInfo;
import ve3.os.OpInfo.Type;

public class Dump {
	private static final String[] regs = {
			"r0", "r1", "r2", "r3", "r4", "r5", "r6", "r7", "r8",
			"r9", "r10", "r11", "ap", "fp", "sp", "pc"
	};
	
	private static String createOperand(Type type, byte operand, int arg) {
		switch(type) {
		case Literal: { // 0~3
			return String.format("$0x%x", operand);
		}
		case Register: { // 5
			return regs[operand];
		}
		case RegDefer: { // 6
			return "(" + regs[operand] + ")";
		}
		case AutoInc: { // 8
			return "(" + regs[operand] + ")+";
		}		
		case ByteDisp: { // 0xa
			return String.format("0x%x(%s)", arg, regs[operand]);	
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
		default: {
			System.exit(1);
			return "";
			
		}
		}		
	}
	
	private static String dump1(OpInfo opinfo) {
		return createOperand(opinfo.getType1(), opinfo.getOpe1(), opinfo.getArg1());
	}
	
	private static String dump2(OpInfo opinfo) {
		return dump1(opinfo) + "," + createOperand(opinfo.getType2(), opinfo.getOpe2(), opinfo.getArg2());
	}
	
	public static String dump0Ops(OpInfo opinfo, String opname) {
		return opname;
	}
	
	public static String dump2Ops(OpInfo opinfo, String opname) {
		String s = opname + " " + createOperand(opinfo.getType1(), opinfo.getOpe1(), opinfo.getArg1());
		s += "," + createOperand(opinfo.getType2(), opinfo.getOpe2(), opinfo.getArg2());
		return s;	
	}
	
	public static String dump1Ops(OpInfo opinfo, String opname) {
		String s = opname + " " + createOperand(opinfo.getType1(), opinfo.getOpe1(), opinfo.getArg1());
		return s;
	}
	
	public static String dumpsubl2(OpInfo opinfo) {
		String s = "subl2 " + createOperand(opinfo.getType1(), opinfo.getOpe1(), opinfo.getArg1());
		s += "," + createOperand(opinfo.getType2(), opinfo.getOpe2(), opinfo.getArg2());
		return s;
	}
	
	public static String dumpmovl(OpInfo opinfo) {
		String s = "movl " + createOperand(opinfo.getType1(), opinfo.getOpe1(), opinfo.getArg1());
		s += "," + createOperand(opinfo.getType2(), opinfo.getOpe2(), opinfo.getArg2());
		return s;
	}
	
	public static String dumpmovab(OpInfo opinfo) {
		return dump2Ops(opinfo, "movab");
	}
	
	public static String dumptstl(OpInfo opinfo) {
		return dump1Ops(opinfo, "tstl");
	}
	

}
