package slip;

import java.util.Iterator;
import java.util.List;

public class ObjectCall extends SimpleCall
{
	private final static int SIZE = 70;
	private VarIdentifier obj;
	
	public ObjectCall(SimpleDescriptor rv, VarIdentifier vi, String name, List pl)
	{
		_rv = rv;
		obj = vi;
		_functionName = name;
		_paramList = pl;
	}
	
	/**
	 * Appel de méthode de la forme: L x0 = x1.m(x1, ..., xn) L'
	 */
	public void getInstruction(List l)
	{
		// idem que SimpleCall -> 24
		addNewFrameInstruction(l);
		
		// idem que SimpleCall + this -> auto
		addActualParameterInstruction(l);
		
		// Totalement différent de SimpleCall ->
		addJumpToMethodInstruction(l);
		
		// idem que SimpleCall -> auto
		addSaveResultInstruction(l);
	}

	public int generateAddress(int startAddr)
	{
		setStartAddress(startAddr);
		// +1 pour this
		return _rv.generateAddress(startAddr+SIZE+(_paramList.size()+1)*8);
	}
	
	protected void addActualParameterInstruction(List l)
	{
		// Le passage de paramètre est le meme que pour SimpleCall
		super.addActualParameterInstruction(l);
		
		int currentAddr = getStartAddress()+20+_paramList.size()*8;
		LmaInstruction inst;
		
		// Sans oublier this
		inst = new LmaInstruction();
		inst.setOpCode("LDM");
		inst.setArg1(1);
		inst.setArg2(Constant.REG_FRAME);
		inst.setOptArg(4*obj.getVarNumber());
		inst.setAddress(currentAddr);
		currentAddr += 4;
		l.add(inst);
		
		inst = new LmaInstruction();
		inst.setOpCode("STM");
		inst.setArg1(1);
		inst.setArg2(Constant.REG_STACK);
		inst.setOptArg(0);
		inst.setAddress(currentAddr);
		l.add(inst);
	}
	
	protected void addJumpToMethodInstruction(List l)
	{
		int currentAddr = getStartAddress()+20+(_paramList.size()+1)*8;
		LmaInstruction inst;
		
		inst = new LmaInstruction();
		inst.setOpCode("LDM");
		inst.setArg1(1);
		inst.setArg2(1);
		inst.setOptArg(0);
		inst.setAddress(currentAddr);
		currentAddr += 4;
		l.add(inst);
		
		// Vérifie que le niveau de l'objet n'est pas strictement plus grand
		// que le niveau maximum pour la méthode qu'on souhaite appeler
		// Si oui, on "fait comme si" le niveau de l'objet était égal au niveau
		// de la méthode la plus élevé
		// Certains champs de l'objet ne seront pas utilisés mais ca ne pose pas
		// d'autres problèmes
		inst = new LmaInstruction();
		inst.setOpCode("LDA");
		inst.setArg1(2);
		inst.setOptArg(LmaGenerator.getMaxLevel(_functionName));
		inst.setAddress(currentAddr);
		currentAddr += 4;
		l.add(inst);
		
		inst = new LmaInstruction();
		inst.setOpCode("COMP");
		inst.setArg1(1);
		inst.setArg2(2);
		inst.setAddress(currentAddr);
		currentAddr += 2;
		l.add(inst);
		
		inst = new LmaInstruction();
		inst.setOpCode("JLE");
		inst.setArg1(0);
		inst.setOptArg(currentAddr+8);
		inst.setAddress(currentAddr);
		currentAddr += 4;
		l.add(inst);
		
		inst = new LmaInstruction();
		inst.setOpCode("LDA");
		inst.setArg1(1);
		inst.setArg2(2);
		inst.setOptArg(0);
		inst.setAddress(currentAddr);
		currentAddr += 4;
		l.add(inst);
		
		// Récupération de l'adresse dans la table d'indirection
		inst = new LmaInstruction();
		inst.setOpCode("MULA");
		inst.setArg1(1);
		inst.setArg2(4);
		inst.setAddress(currentAddr);
		currentAddr += 4;
		l.add(inst);
		
		inst = new LmaInstruction();
		inst.setOpCode("LDM");
		inst.setArg1(1);
		inst.setArg2(1);
		inst.setOptArg(LmaGenerator.getIndirectionTable().getTableAddress(_functionName));
		inst.setAddress(currentAddr);
		currentAddr += 4;
		l.add(inst);
		
		// Vérification que l'adresse de la méthode ne soit pas nulle !
		inst = new LmaInstruction();
		inst.setOpCode("LDA");
		inst.setArg1(2);
		inst.setArg2(0);
		inst.setAddress(currentAddr);
		currentAddr += 4;
		l.add(inst);
		
		inst = new LmaInstruction();
		inst.setOpCode("COMP");
		inst.setArg1(1);
		inst.setArg2(2);
		inst.setAddress(currentAddr);
		currentAddr += 2;
		l.add(inst);
		
		inst = new LmaInstruction();
		inst.setOpCode("JGE");
		inst.setArg1(0);
		inst.setOptArg(currentAddr+14);
		inst.setAddress(currentAddr);
		currentAddr += 4;
		l.add(inst);

		inst = new LmaInstruction();		
		inst.setOpCode("LDM");
		inst.setArg1(Constant.REG_MSG);
		inst.setOptArg(LiteralFactory.addLiteral(l, "ERR:"));
		inst.setAddress(currentAddr);
		currentAddr += 4;
		l.add(inst);
		
		inst = new LmaInstruction();
		inst.setOpCode("LDM");
		inst.setAddress(currentAddr);
		inst.setArg1(Constant.REG_INOUT);
		inst.setArg2(LiteralFactory.addLiteral(l, "IADR"));
		currentAddr += 4;
		l.add(inst);
		
		inst = new LmaInstruction();
		inst.setOpCode("HALT");
		inst.setArg1(0);
		inst.setArg2(0);
		inst.setAddress(currentAddr);
		currentAddr += 2;
		l.add(inst);
		
		// addresse valide ... un petit saut
		inst = new LmaInstruction();
		inst.setOpCode("JUMP");
		inst.setArg1(Constant.REG_RETADR);
		inst.setArg2(1);
		inst.setOptArg(0);
		inst.setAddress(currentAddr);
		currentAddr += 4;
		l.add(inst);
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer("@");
		Iterator it;
		
		sb.append(getLabel());
		sb.append(" ");
		sb.append(_rv.toString());
		sb.append(" = ");
		sb.append(obj);
		sb.append(".");
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
		
		sb.append(" -> @");
		sb.append(getNextLabel());
		
		return sb.toString();
	}
}
