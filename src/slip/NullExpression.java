package slip;

import java.util.List;

public class NullExpression extends AbstractSimpleExpression
{
	private final static int SIZE = 4;
	public NullExpression()
	{
		setOutputReg(0);
		setVariableName("null");
	}
	
	public void getInstruction(List l)
	{
		LmaInstruction inst = new LmaInstruction();
		inst.setOpCode("LDA");
		inst.setArg1(outputReg);
		inst.setArg2(0);
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
		return "null";
	}
}
