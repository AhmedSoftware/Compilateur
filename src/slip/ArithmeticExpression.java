package slip;

import java.util.List;

public class ArithmeticExpression extends AbstractComplexExpression
{
	public final static int ADD = 1;
	public final static int MIN = 2;
	public final static int MUL = 3;
	public final static int DIV = 4;
	public final static int MOD = 5;
	
	private AbstractSimpleExpression left;
	private int operator;
	private AbstractSimpleExpression right;
	
	private int operationAddr;
	
	public ArithmeticExpression(AbstractSimpleExpression l, int op, AbstractSimpleExpression r)
	{
		left = l;
		operator = op;
		right = r;
		operationAddr = Constant.INVALID;
	}
	
	public void getInstruction(List l)
	{
		// traduction de l'expression gauche et résultat dans le registre 0
		left.setOutputReg(0);
		left.getInstruction(l);
		
		// traduction de l'expression droite et résultat dans le registre 1
		right.setOutputReg(1);
		right.getInstruction(l);
		
		LmaInstruction inst1 = new LmaInstruction();
		LmaInstruction inst2 = new LmaInstruction();
		
		inst1.setArg1(0);
		inst1.setArg2(1);
		inst1.setAddress(operationAddr);
		
		switch(operator)
		{
			case ADD: inst1.setOpCode("ADD"); l.add(inst1); break;
			case MIN: inst1.setOpCode("SUB"); l.add(inst1); break;
			case MUL: inst1.setOpCode("MUL"); l.add(inst1); break;
			case DIV: inst1.setOpCode("DIV"); l.add(inst1); break;
			case MOD:
				inst1.setOpCode("DIV"); l.add(inst1); 
				inst2.setOpCode("LDA");
				inst2.setArg1(0);
				inst2.setArg2(1);
				inst2.setOptArg(0);
				inst2.setAddress(operationAddr+2);
				l.add(inst2); 
				break;
		}
	}
	
	public int generateAddress(int startAddr)
	{
		int SIZE = (operator == MOD) ? 6 : 2;
		setStartAddress(startAddr);
		operationAddr = right.generateAddress(left.generateAddress(startAddr));
		return SIZE+operationAddr;		
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer(left.toString());
		
		switch(operator)
		{
			case ADD: sb.append("+"); break;
			case MIN: sb.append("-"); break;
			case MUL: sb.append("*"); break;
			case DIV: sb.append("/"); break;
			case MOD: sb.append("%"); break;
		}
		sb.append(right.toString());
		
		return sb.toString();
	}
}
