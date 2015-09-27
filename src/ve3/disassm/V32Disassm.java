package ve3.disassm;

import ve3.hdw.ConcrateMemory;
import ve3.hdw.Memory;
import ve3.os.OpInfo;
import ve3.os.OpInfo.OpInfoSub;

public class V32Disassm {
	
	private Memory memory;
	private OpInfo opinfo;

	public V32Disassm() {
		memory = new ConcrateMemory(0xfffff);
		opinfo = new OpInfo();
	}
	
	public V32Disassm(byte[] rawdata) {
		this();
		int tsize = readInt(rawdata, 4);
		System.out.printf("textsize = 0x%x\n", tsize);
		memory.load(rawdata, 0x20, tsize);
		//memory.dump();
	}
	
	private int readInt(byte[] rawdata, int offset) {
		return rawdata[offset] & 0xff | (rawdata[offset+1] & 0xff) << 8
				| (rawdata[offset+2] & 0xff) << 16 | (rawdata[offset+3] & 0xff) << 24;
	}
	
	
	
	private OpInfoSub resolveDisp() {
		byte arg = memory.fetch();
		byte type = (byte)((arg >> 4) & 0xf);
		byte value = (byte)(arg & 0xf);
		
		if (value == 0xf) { // program counter address mode
			opinfo.opsub.admode = OpInfo.AddressMode.PC;			
		} else { // normal address mode
			opinfo.opsub.admode = OpInfo.AddressMode.General;
			switch (type) {
			case 0:
			case 1:
			case 2:
			case 3: { // immediate data
				opinfo.opsub.type = OpInfo.Type.Literal;
				opinfo.opsub.operand = (byte)(arg & 0x3f);
				return opinfo.opsub;				
			}
			case 5: { // Register
				opinfo.opsub.type = OpInfo.Type.Register;
				opinfo.opsub.operand = (byte)(arg & 0xf);
				return opinfo.opsub;
				
			}
			case 6: { // Register Defered 
				opinfo.opsub.type = OpInfo.Type.RegDefer;
				opinfo.opsub.operand = (byte)(arg & 0xf);
				return opinfo.opsub;
			}
			case 8: { // Auto Increment
				opinfo.opsub.type = OpInfo.Type.AutoInc;
				opinfo.opsub.operand = (byte)(arg & 0xf);
				return opinfo.opsub;				
			}
			case 0xa: { // Byte Displacement
				opinfo.opsub.type = OpInfo.Type.ByteDisp;
				opinfo.opsub.operand = (byte)(arg & 0xf);
				opinfo.opsub.arg = memory.fetch();
				return opinfo.opsub;				
			}
			default: { // index
				System.out.printf("%x is not implemented yet in resolveDisp\n", type);
				System.exit(1);				
				break;
			}
		
			
			}			
		}
		
		return null;
	}
	
	private void setArg1() {
		OpInfoSub opsub = resolveDisp();
		opinfo.setType1(opsub.type);
		opinfo.setOpe1(opsub.operand);
		opinfo.setArg1(opsub.arg);
	}
	private void setArg2() {		
		setArg1();
		OpInfoSub opsub = resolveDisp();
		opinfo.setType2(opsub.type);
		opinfo.setOpe2(opsub.operand);
		opinfo.setArg2(opsub.arg);
	}
	
	private String format(int index, byte[] rawdata, String s) {
		return String.format("%4x:   %s    %s", index, new String(rawdata), s);
	}
	
	public void disassm() {
		while (true) {
			opinfo.clear();
			run();
		}
	}
	private void run() {
		int b1;
		int index = memory.getCurrentPc();
		switch (b1 = (opinfo.setOpCode(memory.fetch())) & 0xff) {		
		case 0x00: {
			System.out.println(format(index, memory.rawdump(), "halt"));
			break;
		}
		case 0x9e: { // movab
			setArg2();
			System.out.println(format(index, memory.rawdump(), Dump.dumpmovab(opinfo)));
			break;
		}
		case 0xc2: { // subl2			
			setArg2();			
			System.out.println(format(index, memory.rawdump(), Dump.dumpsubl2(opinfo)));
			break;			
		}		
		case 0xd0: { // movl
			setArg2();
			System.out.println(format(index, memory.rawdump(), Dump.dumpmovl(opinfo)));
			break;
		}
		case 0xd5: { // tstl
			setArg1();
			System.out.println(format(index, memory.rawdump(), Dump.dumptstl(opinfo)));			
		}
		default: {
			System.out.printf("unsuuported opecode 0x%x in run()\n", b1);
			System.exit(1);

			break;
		}
		}		
	}
}
