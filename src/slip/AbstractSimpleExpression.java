package slip;

public abstract class AbstractSimpleExpression extends AbstractExpression
{
	protected int outputReg;
	protected String variableName;
	
	public int getOutputReg() { return outputReg; }
	public void setOutputReg(int r) { outputReg = r; }
	
	public void setVariableName(String name) { variableName = name; }
	public String getVariableName() { return variableName; }
}
