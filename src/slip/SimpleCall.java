package slip;

import java.util.List;
import java.util.Iterator;

public class SimpleCall extends AbstractCall
{
	private final static int SIZE = 24;
	
	protected SimpleCall() { }
	
	public SimpleCall(SimpleDescriptor rv, String name, List pl)
	{
		_rv = rv;
		_functionName = name;
		_paramList = pl;
	}
	
	/**
	 * Appel de m�thode: L x0 = m(x1, ..., xn) L'
	 */
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
		return _rv.generateAddress(startAddr+SIZE+8*_paramList.size());
	}
	
	/**
	 * 1�re partie: Ajout des instructions d'initialisation de l'appel de m�thode:
	 * Cr�ation du nouveau frame
	 */
	protected void addNewFrameInstruction(List l) // -> 20
	{
		LmaInstruction inst;
		int currentAddr = getStartAddress();
		
		l.add(new Comment("Assez de place pour les param�tres ?"));
		
		inst = new LmaInstruction();
		inst.setOpCode("LDA");
		inst.setArg1(0);
		inst.setArg2(Constant.REG_STACK);
		inst.setOptArg(4*(_paramList.size()+1)); // on passe n param�tres + this
		inst.setAddress(currentAddr);
		currentAddr += 4;
		l.add(inst);
		
		inst = new LmaInstruction();
		inst.setOpCode("COMP");
		inst.setArg1(Constant.REG_HEAP);
		inst.setArg2(0);
		inst.setAddress(currentAddr);
		currentAddr += 2;
		l.add(inst);
		
		inst = new LmaInstruction();
		inst.setOpCode("JGE");
		inst.setArg1(0);
		inst.setArg2(0);
		inst.setOptArg(getStartAddress()+20);
		inst.setAddress(currentAddr);
		currentAddr += 4;
		l.add(inst);
		
		inst = new LmaInstruction();
		inst.setOpCode("LDM");
		inst.setArg1(Constant.REG_MSG);
		inst.setArg2(LiteralFactory.addLiteral(l, "ERR:"));
		inst.setAddress(currentAddr);
		currentAddr += 4;
		l.add(inst);
		
		inst = new LmaInstruction();
		inst.setOpCode("LDM");
		inst.setArg1(Constant.REG_INOUT);
		inst.setArg2(LiteralFactory.addLiteral(l, "OVFL"));
		inst.setAddress(currentAddr);
		currentAddr += 4;
		l.add(inst);
		
		inst = new LmaInstruction();
		inst.setOpCode("HALT");
		inst.setArg1(0);
		inst.setArg2(0);
		inst.setAddress(currentAddr);
		currentAddr += 2;
		l.add(inst);
	}
	
	/**
	 * 2�me partie: Ajout des instructions d'initialisation de l'appel de m�thode:
	 * Passage de param�tre
	 */
	protected void addActualParameterInstruction(List l) // -> paramList.size()*8
	{
		LmaInstruction insta;
		LmaInstruction instb;
		int c=0;
		int currentAddr = getStartAddress()+20;
		while(c < _paramList.size())
		{
			insta = new LmaInstruction();
			instb = new LmaInstruction();
			
			// charge la valeur effective du ni�me param�tre
			insta.setOpCode("LDM");
			insta.setArg1(0);
			insta.setArg2(Constant.REG_FRAME);
			insta.setOptArg(4*((VarIdentifier)_paramList.get(c)).getVarNumber());
			insta.setAddress(currentAddr);
			l.add(insta);
			currentAddr += 4;
			
			// on place cette valeur dans la future ni�me variable de la m�thode appell�e
			instb.setOpCode("STM");
			instb.setArg1(0);
			instb.setArg2(Constant.REG_STACK);
			instb.setOptArg(4*(c+1)); // c+1 car 0 est r�serv� pour this
			instb.setAddress(currentAddr);
			l.add(instb);
			currentAddr += 4;
			
			c++;
		}
	}
	
	/**
	 * 3�me partie: Ajout des instructions d'initialisation de l'appel de m�thode:
	 * Saut vers les instructions de la m�thode � proprement parler
	 */
	protected void addJumpToMethodInstruction(List l) // -> 4
	{
		LmaInstruction jump = new LmaInstruction();
		jump.setOpCode("JUMP");
		jump.setArg1(Constant.REG_RETADR);
		jump.setArg2(LmaGenerator.getMethod(_functionName, MethodDeclaration.STATIC).getStartAddress());
		jump.setAddress(getStartAddress()+20+_paramList.size()*8);
		l.add(jump);
	}
	
	/**
	 * 4�me partie: Ajout des instructions d'initialisation de l'appel de m�thode:
	 * Sauvegarde du r�sultat de la m�thode dans la variable X 
	 */
	protected void addSaveResultInstruction(List l)
	{
		_rv.getInstruction(l);
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer("@");
		Iterator it;
		
		sb.append(getLabel());
		sb.append(" ");
		sb.append(_rv.toString());
		sb.append(" := ");
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