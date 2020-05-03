package slip;

import java.util.List;

public class VarIdentifier extends AbstractIdentifier
{
	private final static int SIZE = 4;
	
	public VarIdentifier(String name, int n)
	{
		setOutputReg(0);
		setVariableName(name);
		setVarNumber(n);
	}
	
	public void getInstruction(List l)
	{
		LmaInstruction inst = new LmaInstruction();
		
		inst.setOpCode("LDM");
		inst.setArg1(getOutputReg());
		inst.setArg2(Constant.REG_FRAME);
		inst.setOptArg(4*getVarNumber());
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
		return getVariableName()+"#"+getVarNumber();
	}
}
