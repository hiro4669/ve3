package ve3.hdw;

public interface Memory {
	
	public void load(byte[] rawdata, int offset, int size);	
	public void dump();	
	public byte fetch();
	public short fetch2();
	public int fetch4();
	public long fetch8();
	public int getCurrentPc();
	public int savePc();
	
	public byte[] rawdump();
	public byte[] rawdump_rem();
	public boolean remaining();
	public int getPrevPc();

}
