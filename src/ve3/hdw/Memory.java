package ve3.hdw;

public interface Memory {
	
	public void load(byte[] rawdata, int offset, int size);	
	public void dump();
	public byte[] rawdump();
	public byte fetch();
	public short fetch2();
	public int fetch4();
	public int getCurrentPc();
	public int savePc();

}
