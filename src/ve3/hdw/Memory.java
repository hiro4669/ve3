package ve3.hdw;

public interface Memory {
	
	public int load(byte[] rawdata, int offset, int size);
	public int load(byte[] rawdata, int roffset, int moffset, int size);
	public void dump();	
	public byte fetch();
	public short fetch2();
	public int fetch4();
	public long fetch8();
	public int getCurrentPc();
	public int savePc();
	public void setPc(int pc);
	
	public byte[] rawdump();
	public byte[] rawdump_rem();
	public boolean remaining();
	public int getPrevPc();

}
