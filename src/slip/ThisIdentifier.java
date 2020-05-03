package slip;

import java.util.List;

public class ThisIdentifier extends AbstractIdentifier
{
	private final static int SIZE = 4;
	
	public ThisIdentifier()
	{
		setOutputReg(0);
		setVariableName("this");
		setVarNumber(0);
	}
	public void getInstruction(List l)
	{
		LmaInstruction inst = new LmaInstruction();
		
		inst.setOpCode("LDM");
		inst.setArg1(getOutputReg());
		inst.setArg2(Constant.REG_FRAME);
		inst.setOptArg(0);
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
		return "this";
	}
}
