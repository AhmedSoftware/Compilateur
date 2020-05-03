package slip;

import java.util.List;

public abstract class AbstractNode
{
	private int startAddress;
	
	public abstract void getInstruction(List l);
	
	public abstract int generateAddress(int a);
	
	public int getStartAddress() { return startAddress; }
	public void setStartAddress(int a) { startAddress = a; }
}
