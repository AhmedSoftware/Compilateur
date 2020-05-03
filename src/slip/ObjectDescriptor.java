package slip;

import java.util.List;

public class ObjectDescriptor extends AbstractDescriptor
{
	private final static int SIZE = 8;
	private int fieldNumber;
	
	public ObjectDescriptor(String name, int n, int fieldNb)
	{
		setVariableName(name);
		setVarNumber(n);
		fieldNumber = fieldNb;
	}

	public void getInstruction(List l)
	{
		LmaInstruction inst1 = new LmaInstruction();
		LmaInstruction inst2 = new LmaInstruction();
		
		inst1.setOpCode("LDM");
		inst1.setArg1(1);
		inst1.setArg2(Constant.REG_FRAME);
		inst1.setOptArg(4*varNumber);
		inst1.setAddress(getStartAddress());
		l.add(inst1);
		
		inst2.setOpCode("STM");
		inst2.setArg1(0);
		inst2.setArg2(1);
		inst2.setOptArg(4*fieldNumber);
		inst2.setAddress(getStartAddress()+4);
		l.add(inst2);		
	}
	
	public int generateAddress(int startAddr)
	{
		setStartAddress(startAddr);
		return startAddr+SIZE;
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append(getVariableName());
		sb.append("#");
		sb.append(getVarNumber());
		sb.append(".");
		sb.append(fieldNumber);
		
		return sb.toString();
	}
}
