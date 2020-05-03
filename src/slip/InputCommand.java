package slip;

import java.util.List;

public class InputCommand extends AbstractCommand
{
	private final static int SIZE = 10;
	private AbstractDescriptor des;
	
	public InputCommand(AbstractDescriptor d)
	{
		des = d;
	}
	
	public void getInstruction(List l)
	{	
		LmaInstruction inst1 = new LmaInstruction();
		LmaInstruction inst2 = new LmaInstruction();
		LmaInstruction inst3 = new LmaInstruction();
		
		int litAddr = LiteralFactory.addLiteral(l, " IN:");
		
		inst1.setOpCode("LDM");
		inst1.setArg1(Constant.REG_MSG);
		inst1.setArg2(litAddr);
		inst1.setAddress(getStartAddress());
		l.add(inst1);
		
		inst2.setOpCode("HALT");
		inst2.setArg1(0);
		inst2.setArg2(0);
		inst2.setAddress(getStartAddress()+4);
		l.add(inst2);
		
		inst3.setOpCode("STM");
		inst3.setArg1(Constant.REG_INOUT);
		inst3.setArg2(Constant.REG_FRAME);
		inst3.setOptArg(4*des.getVarNumber());
		inst3.setAddress(getStartAddress()+6);
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
		sb.append(" read ");
		sb.append(des.toString());
		sb.append(" -> @");
		sb.append(getNextLabel());
		
		return sb.toString();
	}
}