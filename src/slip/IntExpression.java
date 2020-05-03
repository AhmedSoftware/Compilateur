package slip;

import java.util.List;

public class IntExpression extends AbstractSimpleExpression
{
	private final static int SIZE = 4;
	private int value;
	
	public IntExpression(int i)
	{
		setVariableName(""+i);
		setOutputReg(0);
		value = i;
	}
	
	public void getInstruction(List l)
	{
		int addr = LiteralFactory.addLiteral(l, value);
		
		LmaInstruction inst = new LmaInstruction();
		
		inst.setOpCode("LDM");
		inst.setArg1(outputReg);
		inst.setArg2(addr);
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
		return Integer.toString(value);
	}
}
