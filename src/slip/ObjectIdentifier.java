package slip;

import java.util.List;

public class ObjectIdentifier extends AbstractIdentifier
{
	private final static int SIZE = 8;
	private int fieldNumber;
	
	public ObjectIdentifier(String name, int n, int field)
	{
		setOutputReg(0);
		setVariableName(name);
		setVarNumber(n);
		fieldNumber = field;
	}
	
	public void getInstruction(List l)
	{
		LmaInstruction inst1 = new LmaInstruction(),
					   inst2 = new LmaInstruction();
		
		inst1.setOpCode("LDM");
		inst1.setArg1(2);
		inst1.setArg2(Constant.REG_FRAME);
		inst1.setOptArg(4*getVarNumber());
		inst1.setAddress(getStartAddress());
		l.add(inst1);
		
		inst2.setOpCode("LDM");
		inst2.setArg1(getOutputReg());
		inst2.setArg2(2);
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
