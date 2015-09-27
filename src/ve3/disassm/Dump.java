package ve3.disassm;

import ve3.os.OpInfo;
import ve3.os.OpInfo.Type;

public class Dump {
	
	private static String createOperand(Type type, byte operand, int arg) {
		switch(type) {
		case Literal: { // 0~3
			return String.format("$0x%x", operand);
		}
		case Register: { // 5
			switch(operand) {
			case 12: 
				return "ap";			
			case 13:
				return "fp";				
			case 14:
				return "sp";
			case 15: {
				return "pc";				
			}
			default: {
				return "r" + operand;
			}
			}
		}
		case RegDefer: { // 6
			switch(operand) {
			case 12: 
				return "(ap)";			
			case 13:
				return "(fp)";				
			case 14:
				return "(sp)";
			case 15: {
				return "(pc)";				
			}
			default: {
				return String.format("(r%x)", operand);
			}
			}								
		}
		case AutoInc: { // 8
			switch(operand) {
			case 12: 
				return "(ap)+";
			case 13:
				return "(fp)+";				
			case 14:
				return "(sp)+";
			case 15: {
				return "(pc)+";				
			}
			default: {
				return String.format("(r%x)+", operand);
			}
			}						
		}		
		case ByteDisp: { // 0xa
			switch(operand) {
			case 12: {
				return String.format("0x%x(ap)", arg);
			}
			case 13: {
				return String.format("0x%x(fp)", arg);
			}
			case 14: {
				return String.format("0x%x(sp)", arg);
			}
			case 15: {
				return String.format("0x%x(pc)", arg);
			}
			default: {
				return String.format("0x%x(r%x)", arg, operand);
			}
			}			
		}
		default: {
			System.out.println("unrecognized type in Dump");
			System.exit(1);
			break;
		}
		}		
		return "";
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
