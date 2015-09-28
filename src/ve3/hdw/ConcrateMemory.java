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
		return (short)((memory[pc++] & 0xff) << 8 | b1 & 0xff);
	}
	
	public int fetch4() {
		byte b1 = (byte)(memory[pc++]);
		byte b2 = (byte)(memory[pc++]);
		byte b3 = (byte)(memory[pc++]);
		byte b4 = (byte)(memory[pc++]);			
		return (int)((b4 & 0xff) << 24 | (b3 & 0xff) << 16 | (b2 & 0xff) << 8 | b1 & 0xff);
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
		int ti;
		for (int i = ti = prevPc; i < pc; ++i, ++count, ++ti) {
			rawout.printf("%02x ", memory[i]);
		}
		/*
		if (ti == pc) {
			System.out.println("end");
		} else {

			System.out.println("continue");
		}
		*/		
		
		for (int i = 0; i < 4-count; ++i) {
			rawout.printf("   ");
		}		
		
		byte[] rawdata = buf.toByteArray();
		buf.reset();
		return rawdata;		
	}

}
