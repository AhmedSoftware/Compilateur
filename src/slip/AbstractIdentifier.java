package slip;

public abstract class AbstractIdentifier extends AbstractSimpleExpression
{
	protected int varNumber;
	
	public int getVarNumber() { return varNumber; }
	public void setVarNumber(int n) { varNumber = n; }
}
