package slip;

public abstract class AbstractDescriptor extends AbstractNode
{
	protected int varNumber;
	protected String variableName;
	
	public int getVarNumber() { return varNumber; }
	public void setVarNumber(int i) { varNumber = i; }
	
	public String getVariableName() { return variableName; }
	public void setVariableName(String s) { variableName = s; }
}
