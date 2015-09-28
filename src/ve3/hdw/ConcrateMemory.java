package ve3.hdw;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ConcrateMemory implements Memory {
	
	private int size;
	private int maxSize;
	private byte[] memory;
	private int pc;
	private int prevPc;
	private ByteArrayOutputStream buf;
	private PrintStream rawout;
	
	
	public ConcrateMemory(int maxSize) {
		memory = new byte[maxSize];
		this.maxSize = maxSize;		
		buf = new ByteArrayOutputStream(30);		
		rawout = new PrintStream(buf);
	}
	
	public void load(byte[] rawdata, int offset, int size) {
		System.arraycopy(rawdata, offset, memory, 0, size);
		this.size = size;
		pc = 0;
	}
	
	public byte fetch() {
		return memory[pc++];
	}
	
	public short fetch2() {
		byte b1 = (byte)(memory[pc++] & 0xff);
		return (short)((memory[pc++] & 0xff) << 8 | b1);
	}
	
	public int fetch4() {
		byte b1 = (byte)(memory[pc++] & 0xff);
		byte b2 = (byte)(memory[pc++] & 0xff);
		byte b3 = (byte)(memory[pc++] & 0xff);
		byte b4 = (byte)(memory[pc++] & 0xff);
		
		return b4 << 24 | b3 << 16 | b2 << 8 | b1;
	}
	
	public int savePc() {
		return prevPc = pc;
	}
	
	public int getCurrentPc() {
		return pc;
	}
	
	public void dump() {
		for (int i = 0; i < size; ++i) {
			if (i % 16 == 0) System.out.println("");
			System.out.printf("%02x ", memory[i]);
		}
	}
	
	public byte[] rawdump() {
		int count = 0;
		for (int i = prevPc; i < pc; ++i, ++count) {
			rawout.printf("%02x ", memory[i]);
		}
		for (int i = 0; i < 4-count; ++i) {
			rawout.printf("   ");
		}		
		
		byte[] rawdata = buf.toByteArray();
		buf.reset();
		return rawdata;		
	}

}
