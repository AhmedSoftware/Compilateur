package slip;

public abstract class AbstractAssignment extends AbstractCommand
{
	protected AbstractDescriptor des;
	protected AbstractExpression expr;
	
	public void setDescriptor(AbstractDescriptor ad) { des = ad; }
	public AbstractDescriptor getDescriptor() { return des; }
	
	public void setExpression(AbstractExpression ae) { expr = ae; }
	public AbstractExpression getExpression() { return expr; }
}
