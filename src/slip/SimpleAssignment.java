package slip;

import java.util.List;

public class SimpleAssignment extends AbstractAssignment
{
	public SimpleAssignment(AbstractDescriptor d, AbstractExpression e)
	{
		setDescriptor(d);
		setExpression(e);
	}
	
	public void getInstruction(List l)
	{
		getExpression().getInstruction(l);
		getDescriptor().getInstruction(l);
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer("@");
		
		sb.append(getLabel());
		sb.append(" ");
		sb.append(des.toString());
		sb.append(" := ");
		sb.append(expr.toString());
		sb.append(" -> @");
		sb.append(getNextLabel());
		
		return sb.toString();
	}
	
	public int generateAddress(int startAddr)
	{
		setStartAddress(startAddr);
		return getDescriptor().generateAddress(getExpression().generateAddress(startAddr));
	}
}
