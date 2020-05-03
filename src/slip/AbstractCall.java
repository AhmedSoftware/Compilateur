package slip;

import java.util.List;

public abstract class AbstractCall extends AbstractCommand
{
	protected SimpleDescriptor _rv;
	protected String _functionName;
	protected List _paramList;
	
	public SimpleDescriptor getReturnValue() { return _rv; }
	public void setReturnValue(SimpleDescriptor sd) { _rv = sd; }
	
	public String getCalledMethodName() { return _functionName; }
	public void setCalledMethodName(String n) { _functionName = n; }
	
	public List getParamList() { return _paramList; }
	public void setParamList(List l) { _paramList = l; }
}
