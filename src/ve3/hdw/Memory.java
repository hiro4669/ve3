package ve3.hdw;

public interface Memory {
	public byte[] getRawMemory();
	public int load(byte[] rawdata, int offset, int size);
	public int load(byte[] rawdata, int roffset, int moffset, int size);
	public void dump();	
	public MVal fetch();
	public MVal fetch2();
	public MVal fetch4();
	public MVal fetch8();
	public int getCurrentPc();
	public int savePc();
	public void setPc(int pc);
	
	public byte readByte(int offset);
	public short readShort(int offset);
	public int readInt(int offset);
	public long readLong(int offset);
	
	public byte[] rawdump();
	public byte[] rawdump_rem();
	public boolean remaining();
	public int getPrevPc();

}
