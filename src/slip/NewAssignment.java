package slip;

import java.util.List;

public class NewAssignment extends AbstractAssignment
{
	private final static int SIZE = 32;
	private int objectOrder;
	
	public NewAssignment(AbstractDescriptor ae, int oo)
	{
		setDescriptor(ae);
		objectOrder = oo;
	}
	
	public void getInstruction(List l)
	{
		LmaInstruction inst1 = new LmaInstruction();
		LmaInstruction inst2 = new LmaInstruction();
		LmaInstruction inst3 = new LmaInstruction();
		LmaInstruction inst4 = new LmaInstruction();
		LmaInstruction inst5 = new LmaInstruction();
		LmaInstruction inst6 = new LmaInstruction();
		LmaInstruction inst7 = new LmaInstruction();
		LmaInstruction inst8 = new LmaInstruction();
		LmaInstruction inst9 = new LmaInstruction();
		
		inst1.setOpCode("SUBA");
		inst1.setArg1(Constant.REG_HEAP);
		inst1.setOptArg(4*(objectOrder+1));
		inst1.setAddress(getStartAddress());
		l.add(inst1);
		
		// Assez de place pour un nouvel objet ?
		inst2.setOpCode("COMP");
		inst2.setArg1(Constant.REG_HEAP);
		inst2.setArg2(Constant.REG_FRAME);
		inst2.setAddress(getStartAddress()+4);
		l.add(inst2);
		
		// oui -> on alloue l'objet
		inst3.setOpCode("JGE");
		inst3.setArg1(0);
		inst3.setArg2(0);
		inst3.setOptArg(getStartAddress()+20);
		inst3.setAddress(getStartAddress()+6);
		l.add(inst3);
		
		// Non -> on s'arrete
		inst4.setOpCode("LDM");
		inst4.setArg1(Constant.REG_MSG);
		inst4.setArg2(LiteralFactory.addLiteral(l, "ERR:"));
		inst4.setAddress(getStartAddress()+10);
		l.add(inst4);
		
		inst5.setOpCode("LDA");
		inst5.setArg1(Constant.REG_INOUT);
		inst5.setArg2(LiteralFactory.addLiteral(l, "OVFL"));
		inst5.setAddress(getStartAddress()+14);
		l.add(inst5);
		
		inst6.setOpCode("HALT");
		inst6.setArg1(0);
		inst6.setArg2(0);
		inst6.setAddress(getStartAddress()+18);
		l.add(inst6);
		
		inst7.setOpCode("LDA");
		inst7.setArg1(0);
		inst7.setOptArg(objectOrder);
		inst7.setAddress(getStartAddress()+20);
		l.add(inst7);

		inst8.setOpCode("STM");
		inst8.setArg1(0);
		inst8.setArg2(Constant.REG_HEAP);
		inst8.setOptArg(0);
		inst8.setAddress(getStartAddress()+24);
		l.add(inst8);

		inst9.setOpCode("STM");
		inst9.setArg1(Constant.REG_HEAP);
		inst9.setArg2(Constant.REG_FRAME);
		inst9.setOptArg(4*getDescriptor().getVarNumber());
		inst9.setAddress(getStartAddress()+28);
		l.add(inst9);
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
		sb.append(" ");
		sb.append(des.toString());
		sb.append(" := new/");
		sb.append(objectOrder);
		sb.append(" -> @");
		sb.append(getNextLabel());
		
		return sb.toString();
	}
}
