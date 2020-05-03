package slip;

import java.util.List;

public class OutputCommand extends AbstractCommand
{
	private static final int SIZE = 10;
	private AbstractIdentifier id;
	
	public OutputCommand(AbstractIdentifier i)
	{
		id = i;
	}
	
	public void getInstruction(List l)
	{
		LmaInstruction inst1 = new LmaInstruction();
		LmaInstruction inst2 = new LmaInstruction();
		LmaInstruction inst3 = new LmaInstruction();
		
		inst1.setOpCode("LDM");
		inst1.setArg1(Constant.REG_INOUT);
		inst1.setArg2(Constant.REG_FRAME);
		inst1.setOptArg(4*id.getVarNumber());
		inst1.setAddress(getStartAddress());
		l.add(inst1);
		
		inst2.setOpCode("LDM");
		inst2.setArg1(Constant.REG_MSG);
		inst2.setArg2(LiteralFactory.addLiteral(l, "OUT:"));
		inst2.setAddress(getStartAddress()+4);
		l.add(inst2);
		
		inst3.setOpCode("HALT");
		inst3.setArg1(0);
		inst3.setArg2(0);
		inst3.setAddress(getStartAddress()+8);
		l.add(inst3);
	}
	
	public int generateAddress(int startAddr)
	{
		setStartAddress(startAddr);
		return startAddr+SIZE;
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer("@");
		
		sb.append(getLabel());
		sb.append(" write ");
		sb.append(id.toString());
		sb.append(" -> @");
		sb.append(getNextLabel());
		
		return sb.toString();
	}
}
