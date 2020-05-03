package slip;

import java.util.List;

public class IfStatement extends AbstractCommand
{
	/** Taille de l'instruction */
	private final static int SIZE = 10;
	
	/** Identifiant pour l'opérateur */
	public final static int LESS = 1;
	public final static int EQUAL = 2;
	
	private AbstractSimpleExpression left;
	private AbstractSimpleExpression right;
	private int cop;
	
	private int thenLabel;
	private int elseLabel;
	
	private int thenAddr;
	private int elseAddr;
	
	private int testAddr;
	
	public IfStatement(AbstractSimpleExpression l, int op, AbstractSimpleExpression r)
	{
		left = l;
		right = r;
		cop = op;
		
		thenLabel = Constant.INVALID;
		elseLabel = Constant.INVALID;
		
		thenAddr = Constant.INVALID;
		elseAddr = Constant.INVALID;
		
		testAddr = Constant.INVALID;
	}
	
	public void getInstruction(List l)
	{
		LmaInstruction inst1 = new LmaInstruction();
		LmaInstruction inst2 = new LmaInstruction();
		LmaInstruction inst3 = new LmaInstruction();
		
		left.setOutputReg(0);
		left.getInstruction(l);
		right.setOutputReg(1);
		right.getInstruction(l);
		
		inst1.setOpCode("COMP");
		inst1.setArg1(0);
		inst1.setArg2(1);
		inst1.setAddress(testAddr);
		l.add(inst1);
		
		inst2.setArg1(0);
		if(cop == LESS)
			inst2.setOpCode("JL");
		else if(cop == EQUAL)
			inst2.setOpCode("JE");

		inst2.setArg2(thenAddr);
		inst2.setAddress(testAddr+2);
		l.add(inst2);
		
		inst3.setOpCode("JUMP");
		inst3.setArg1(0);
		inst3.setArg2(elseAddr);
		inst3.setAddress(testAddr+6);
		l.add(inst3);
	}
	
	public int generateAddress(int startAddr)
	{
		int addr = startAddr;
		
		setStartAddress(addr);
		addr = left.generateAddress(addr);
		testAddr = right.generateAddress(addr);
		
		return SIZE+testAddr;
	}
	
	public int getThenLabel() { return thenLabel; }
	public void setThenLabel(int l) { thenLabel = l; }
	
	public int getNextLabel() { return thenLabel; }
	
	public int getElseLabel() { return elseLabel; }
	public void setElseLabel(int l) { elseLabel = l; }
	
	public int getThenAddr() { return thenAddr; }
	public void setThenAddr(int a) { thenAddr = a; }
	
	public int getElseAddr() { return elseAddr; }
	public void setElseAddr(int a) { elseAddr = a; }
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer("@");
		
		sb.append(getLabel());
		sb.append(" if ");
		sb.append(left.toString());
		sb.append(cop == EQUAL ? " = " : " < ");
		sb.append(right.toString());
		sb.append(" then -> @");
		sb.append(thenLabel);
		sb.append(" else -> @");
		sb.append(elseLabel);

		return sb.toString();
	}
}
