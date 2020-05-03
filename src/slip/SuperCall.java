package slip;

import java.util.List;
import java.util.Iterator;

public class SuperCall extends SimpleCall
{
	private final static int PARTIALSIZE = 24;
	private int currentLevel;
	
	public SuperCall(SimpleDescriptor rv, String name, List pl, int cl)
	{
		super(rv, name, pl);
		currentLevel = cl;
	}
	
	public void getInstruction(List l)
	{
		addNewFrameInstruction(l);
		
		addActualParameterInstruction(l);
		
		addJumpToMethodInstruction(l);
		
		addSaveResultInstruction(l);
	}
	
	public int generateAddress(int startAddr)
	{
		setStartAddress(startAddr);
		return _rv.generateAddress(startAddr+PARTIALSIZE+8*(_paramList.size()+1));
	}
	
	protected void addActualParameterInstruction(List l)
	{
		// Le passage de parametre est le meme que pour SimpleCall
		super.addActualParameterInstruction(l);
		
		int currentAddr = getStartAddress()+20+_paramList.size()*8;
		
		// Sans oublier this
		LmaInstruction insta = new LmaInstruction();
		insta.setOpCode("LDM");
		insta.setArg1(1);
		insta.setArg2(Constant.REG_FRAME);
		insta.setOptArg(0);
		insta.setAddress(currentAddr);
		currentAddr += 4;
		l.add(insta);
		
		LmaInstruction instb = new LmaInstruction();
		instb.setOpCode("STM");
		instb.setArg1(1);
		instb.setArg2(Constant.REG_STACK);
		instb.setOptArg(0);
		instb.setAddress(currentAddr);
		l.add(instb);
	}
	
	protected void addJumpToMethodInstruction(List l)
	{
		LmaInstruction jump = new LmaInstruction();
		jump.setOpCode("JUMP");
		jump.setArg1(Constant.REG_RETADR);
		jump.setArg2(LmaGenerator.getIndirectionTable().getSuperMethodAddr(_functionName, currentLevel));
		jump.setAddress(getStartAddress()+20+(_paramList.size()+1)*8);
		l.add(jump);
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer("@");
		Iterator it;
		
		sb.append(getLabel());
		sb.append(" ");
		sb.append(_rv.toString());
		sb.append(" := super.");
		sb.append(_functionName);
		
		sb.append("(");
		it = _paramList.iterator();
		while(it.hasNext())
		{
			sb.append(it.next());
			if(it.hasNext())
				sb.append(", ");
		}
		sb.append(")");
		
		sb.append(" -> ");
		sb.append(getNextLabel());
		
		return sb.toString();
	}
}
