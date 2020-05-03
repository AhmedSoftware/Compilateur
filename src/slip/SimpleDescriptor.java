package slip;

import java.util.List;

public class SimpleDescriptor extends AbstractDescriptor
{
	private final static int SIZE = 4;
	public SimpleDescriptor(String name, int n)
	{
		setVariableName(name);
		setVarNumber(n);
	}
	
	public void getInstruction(List l)
	{
		LmaInstruction inst = new LmaInstruction();
		
		inst.setOpCode("STM");
		inst.setArg1(0);
		inst.setArg2(Constant.REG_FRAME);
		inst.setOptArg(4*varNumber);
		inst.setAddress(getStartAddress());
		l.add(inst);
	}
	
	public int generateAddress(int startAddr)
	{
		setStartAddress(startAddr);
		return startAddr+SIZE;
	}
	
	public String toString()
	{
		return ""+getVariableName()+"#"+getVarNumber();
	}
}
