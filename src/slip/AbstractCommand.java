package slip;

public abstract class AbstractCommand extends AbstractNode
{
	protected int label;
	protected int nextLabel;
	
	public int getLabel() { return label; }
	public void setLabel(int i) { label = i; }
	
	public int getNextLabel() { return nextLabel; }
	public void setNextLabel(int i) { nextLabel = i; }
	
	protected AbstractCommand()
	{
		label = -1;
		nextLabel = -1;
	}
}
