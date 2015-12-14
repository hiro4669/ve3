package ve3.hdw;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ConcrateMemory implements Memory {
	
//	private int size;
	private int end;
	private int maxSize;
	private byte[] memory;
	private int pc;
	private int prevPc;
	private ByteArrayOutputStream buf;
	private PrintStream rawout;
	
	private boolean remain;
	
	
	public ConcrateMemory(int maxSize) {
		remain = false;
		memory = new byte[maxSize];
		this.maxSize = maxSize;		
		buf = new ByteArrayOutputStream(30);		
		rawout = new PrintStream(buf);
	}
	
	public int load(byte[] rawdata, int offset, int size) {
		System.arraycopy(rawdata, offset, memory, 0, size);
		pc = 0;
		//this.size = size;
		return (end = size);
		
	}
	
	public int load(byte[] rawdata, int roffset, int moffset, int size) {
		System.arraycopy(rawdata, roffset, memory, moffset, size);		
		pc = 0;
		return (end = moffset + size);
	}
	
	public byte fetch() {
		return memory[pc++];
	}
	
	public short fetch2() {
		byte b1 = (byte)(memory[pc++] & 0xff);
		return (short)((memory[pc++] & 0xff) << 8 | b1 & 0xff);
	}
	
	public int fetch4() {
		byte b1 = (byte)(memory[pc++]);
		byte b2 = (byte)(memory[pc++]);
		byte b3 = (byte)(memory[pc++]);
		byte b4 = (byte)(memory[pc++]);			
		return (int)((b4 & 0xff) << 24 | (b3 & 0xff) << 16 | (b2 & 0xff) << 8 | b1 & 0xff);
	}
	
	public long fetch8() {
		byte b1 = (byte)(memory[pc++]);
		byte b2 = (byte)(memory[pc++]);
		byte b3 = (byte)(memory[pc++]);
		byte b4 = (byte)(memory[pc++]);		
		byte b5 = (byte)(memory[pc++]);
		byte b6 = (byte)(memory[pc++]);
		byte b7 = (byte)(memory[pc++]);
		byte b8 = (byte)(memory[pc++]);
		return (long)((b8 & 0xff) << 56 | (b7 & 0xff) << 48 | (b6 & 0xff) << 40 | (b5 & 0xff) << 32 |
				(long)((b4 & 0xff) << 24 | (b3 & 0xff) << 16 | (b2 & 0xff) << 8 | b1 & 0xff));
		
	}
	
	public int savePc() {
		remain = false;
		return prevPc = pc;
	}
	
	public int getCurrentPc() {
		return pc;
	}
	
	public void setPc(int pc) {
		this.pc = pc;
	}
	
	public int getPrevPc() {
		return prevPc;
	}
	
	public void dump() {
		for (int i = 0; i < end; ++i) {
			if (i % 16 == 0) System.out.printf("\n%04x: ", i);
			System.out.printf("%02x ", memory[i]);
			
		}
	}
	
	public byte[] rawdump() {
		int count = 0;
		int limit = (pc - prevPc) <= 4 ? pc : prevPc+4;
		
		//for (int i = ti = prevPc; i < limit; ++i, ++count, ++ti) {
//			rawout.printf("%02x ", memory[i]);
		//}
		
		for(; prevPc < limit; ++prevPc, ++count) {
			rawout.printf("%02x ", memory[prevPc]);
		}
		remain = (prevPc != pc);
		
		for (int i = 0; i < 4-count; ++i) {
			rawout.printf("   ");
		}		
		
		byte[] rawdata = buf.toByteArray();
		buf.reset();
		return rawdata;		
	}
	
	public boolean remaining() {
		return remain;
	}
	
	public byte[] rawdump_rem() {
		int limit = (pc - prevPc) <= 4 ? pc : prevPc+4;
		
		for(; prevPc < limit; ++prevPc) {
			rawout.printf("%02x ", memory[prevPc]);
		}		
		remain = (prevPc != pc);		
		byte[] rawdata = buf.toByteArray();
		buf.reset();
		return rawdata;	
	}

}
