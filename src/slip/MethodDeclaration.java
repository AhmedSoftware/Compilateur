package slip;

import java.util.*;

public class MethodDeclaration extends AbstractNode
{
	private final static int SIZE = 60;
	public final static int STATIC = -1;
	
	/** Nom de la méthode*/
	protected String _methodName;
	
	/** Liste de paramètres */
	protected List _paramList;
	
	/** Niveau de la méthode */
	protected int _methodLevel;
	
	/** Label de début de la méthode */
	protected int _methodStartLabel;
	
	/** Label de fin de la méthode */
	protected int _methodEndLabel;
	
	/** Variable dans laquelle on placera le résultat de la méthode */
	protected SimpleDescriptor _rv;
	
	/** Ensemble d'instruction de la méthode */
	protected LinkedList _stmt;
	
	/** Adresse de fin de la méthode */
	protected int _endAddrOfMethod;
	
	/**
	 * Plus grand numéro de variable utilisé dans les instructions
	 * (Utile pour allouer le frame)
	 */
	protected int _lastVarNumber;
	
	public MethodDeclaration(String name)
	{
		_methodName = name;
		_paramList = new LinkedList();
		_methodLevel = STATIC;
		_methodStartLabel = Constant.INVALID;
		_stmt = new LinkedList();
		_methodEndLabel = Constant.INVALID;
		_rv = null;
	}
	
	public void setStartLabel(int i) { _methodStartLabel = i; }
	public int getStartLabel() { return _methodStartLabel; }
	
	public void setMethodEndLabel(int i) { _methodEndLabel = i; }
	public int getMethodEndLabel() { return _methodEndLabel; }
	
	public void setReturnValue(SimpleDescriptor r) { _rv = r; }
	public SimpleDescriptor getReturnValue() { return _rv; }
	
	public void setMethodLevel(int l) { _methodLevel = l; }
	public int getMethodLevel() { return _methodLevel; }
	
	public void setMethodName(String n) { _methodName = n; }
	public String getMethodName() { return _methodName; }
	
	public void addParam(VarIdentifier ve) { _paramList.add(ve); }
	
	public void addStatement(AbstractCommand s) { _stmt.add(s); }
	public AbstractCommand getLastStatement() { return (AbstractCommand)_stmt.getLast(); }
	
	public void setNumberOfVar(int s) { _lastVarNumber = s; }
	public int getNumberOfVar() { return _lastVarNumber; }
		
	// 1) Ajouter un nouveau frame
	// 2) Sauter à l'endroit du code de la méthode
	// 3) Preparer le retour
	public void getInstruction(List l)
	{
		int currentAddr = getStartAddress();
		int methodReturnAddr;
		
		l.add(new Comment("Begin of "+_methodName+(_methodLevel > STATIC ? "/"+_methodLevel : "")));
		l.add(new Comment("Assez de place pour un nouveau frame ?"));
		
		LmaInstruction inst;
		
		// Test pour savoir si on a assez de place pour créer un
		// nouveau frame pour this+param+local_var+reg_R+reg_F
		inst = new LmaInstruction();
		inst.setOpCode("LDA");
		inst.setArg1(0);
		inst.setArg2(Constant.REG_STACK);
		inst.setOptArg(4*(_lastVarNumber+3));
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
		inst.setOptArg(currentAddr+14);
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
		inst.setAddress(getStartAddress()+14);
		currentAddr += 4;
		l.add(inst);
		
		inst = new LmaInstruction();
		inst.setOpCode("HALT");
		inst.setArg1(0);
		inst.setArg2(0);
		inst.setAddress(currentAddr);
		currentAddr += 2;
		l.add(inst);
			
		// Ajout d'un nouveau frame
		inst = new LmaInstruction();
		inst.setOpCode("STM");
		inst.setArg1(Constant.REG_FRAME);
		inst.setArg2(Constant.REG_STACK);
		inst.setOptArg(4*(_lastVarNumber+1));
		inst.setAddress(currentAddr);
		currentAddr += 4;
		l.add(inst);
		
		inst = new LmaInstruction();
		inst.setOpCode("STM");
		inst.setArg1(Constant.REG_RETADR);
		inst.setArg2(Constant.REG_STACK);
		inst.setOptArg(4*(_lastVarNumber+2));
		inst.setAddress(currentAddr);
		currentAddr += 4;
		l.add(inst);
		
		inst = new LmaInstruction();
		inst.setOpCode("LDA");
		inst.setArg1(Constant.REG_FRAME);
		inst.setArg2(Constant.REG_STACK);
		inst.setOptArg(0);
		inst.setAddress(currentAddr);
		currentAddr += 4;
		l.add(inst);
		
		inst = new LmaInstruction();
		inst.setOpCode("ADDA");
		inst.setArg1(Constant.REG_STACK);
		inst.setOptArg(4*(_lastVarNumber+3));
		inst.setAddress(currentAddr);
		currentAddr += 4;
		l.add(inst);
		
		// Jump vers le corp de la méthode
		inst = new LmaInstruction();
		inst.setOpCode("JUMP");
		inst.setArg1(0);
		inst.setArg2(0);
		inst.setOptArg(getStartAddress()+SIZE);
		inst.setAddress(currentAddr);
		currentAddr += 4;
		l.add(inst);
		
		// Préparation du retour
		methodReturnAddr = currentAddr;
		inst = new LmaInstruction();
		inst.setOpCode("LDM");
		inst.setArg1(0);
		inst.setArg2(Constant.REG_FRAME);
		inst.setOptArg(4*_rv.getVarNumber());
		inst.setAddress(currentAddr);
		currentAddr += 4;
		l.add(inst);
		
		inst = new LmaInstruction();
		inst.setOpCode("LDA");
		inst.setArg1(Constant.REG_STACK);
		inst.setArg2(Constant.REG_FRAME);
		inst.setOptArg(0);
		inst.setAddress(currentAddr);
		currentAddr += 4;
		l.add(inst);
		
		inst = new LmaInstruction();
		inst.setOpCode("LDM");
		inst.setArg1(Constant.REG_FRAME);
		inst.setArg2(Constant.REG_STACK);
		inst.setOptArg(4*(_lastVarNumber+1));
		inst.setAddress(currentAddr);
		currentAddr += 4;
		l.add(inst);
		
		inst = new LmaInstruction();
		inst.setOpCode("LDM");
		inst.setArg1(Constant.REG_RETADR);
		inst.setArg2(Constant.REG_STACK);
		inst.setOptArg(4*(_lastVarNumber+2));
		inst.setAddress(currentAddr);
		currentAddr += 4;
		l.add(inst);
		
		inst = new LmaInstruction();
		inst.setOpCode("JUMP");
		inst.setArg1(0);
		inst.setArg2(Constant.REG_RETADR);
		inst.setOptArg(0);
		inst.setAddress(currentAddr);
		currentAddr += 4;
		l.add(inst);
		
		AbstractCommand s;
		int prevNextLabel = _methodStartLabel;
		int i = 0;
		while(i<_stmt.size())
		{
			s = (AbstractCommand)_stmt.get(i);
			
			if(s instanceof IfStatement)
			{
				// on recheche l'adresse des 2 instructions qui doivent aller dans le if
				// on sait qu'elles sont après les if ...
				int thenLabel = ((IfStatement)s).getThenLabel();
				int elseLabel = ((IfStatement)s).getElseLabel();
				int j = i+1;
				int exit = 2;
				AbstractCommand as;
				
				while(j<_stmt.size() && exit > 0)
				{
					as = (AbstractCommand)_stmt.get(j);
					if(as.getLabel() == thenLabel)
					{
						((IfStatement)s).setThenAddr(as.getStartAddress());
						exit--;
					}
					else if(as.getLabel() == elseLabel)
					{
						((IfStatement)s).setElseAddr(as.getStartAddress());
						exit--;
					}	
					j++;
				}
				
				if(exit != 0)
				{
					// un des 2 labels non trouvé
					if(elseLabel == _methodEndLabel)
						((IfStatement)s).setElseAddr(methodReturnAddr);
				}
				
				l.add(new Comment(s.toString()));
				s.getInstruction(l);
				prevNextLabel = ((IfStatement)s).getThenLabel();
			}
			else
			{
				if(prevNextLabel != s.getLabel())
				{
					if(prevNextLabel == _methodEndLabel)
					{
						LmaInstruction previousInst = (LmaInstruction)((LinkedList)l).getLast();
						LmaInstruction jumpBack = new LmaInstruction();
						jumpBack.setOpCode("JUMP");
						jumpBack.setArg1(0);
						jumpBack.setArg2(0);
						jumpBack.setOptArg(methodReturnAddr); // Saute vers l'instruction de préparation du retour
						jumpBack.setAddress(previousInst.getAddress()+previousInst.getSize());
						l.add(jumpBack);
					}
					else
					{
						// Il faut trouver l'instruction qui a le label prevNextLabel et ajouter un saut
						// vers celle-ci avant d'ajouter le code du statement suivant
						inst = (LmaInstruction)((LinkedList)l).getLast();
						addJump(l, prevNextLabel, inst.getAddress()+inst.getSize());
					}
				}
				
				l.add(new Comment(s.toString()));
				s.getInstruction(l);
				prevNextLabel = ((AbstractCommand)s).getNextLabel();
			}
			i++;
		}
		
		if(prevNextLabel != _methodEndLabel)
		{
			inst = (LmaInstruction)((LinkedList)l).getLast();
			addJump(l, prevNextLabel, inst.getAddress()+inst.getSize());
		}
		else
		{
			LmaInstruction previousInst = (LmaInstruction)((LinkedList)l).getLast();
			LmaInstruction jumpBack = new LmaInstruction();
			jumpBack.setOpCode("JUMP");
			jumpBack.setArg1(0);
			jumpBack.setArg2(0);
			jumpBack.setOptArg(methodReturnAddr); // Saute vers l'instruction de préparation du retour
			jumpBack.setAddress(previousInst.getAddress()+previousInst.getSize());
			l.add(jumpBack);
		}
		
		l.add(new Comment("End of "+_methodName+(_methodLevel > STATIC ? "/"+_methodLevel : "")));
	}
		
	private void addJump(List l, int label, int jumpAddr)
	{
		Iterator it = _stmt.iterator();
		AbstractCommand inst = null;
		while(it.hasNext())
		{
			inst = (AbstractCommand)it.next();
			if(inst.getLabel() == label)
				break;
		}
		
		LmaInstruction jump = new LmaInstruction();
		jump.setOpCode("JUMP");
		jump.setArg1(0);
		jump.setArg2(0);
		jump.setOptArg(inst.getStartAddress());
		jump.setAddress(jumpAddr);
		l.add(jump);
	}
	
	public int generateAddress(int startAddress)
	{
		setStartAddress(startAddress);
		if(_methodLevel != STATIC)
		{
			// on ajoute l'adresse de la méthode dans la table d'indirection
			LmaGenerator.getIndirectionTable().setAdress(_methodName, _methodLevel, startAddress);
		}
		
		int currentAddr = startAddress+SIZE;
		int previousNextLabel = _methodStartLabel;
		Iterator it = _stmt.iterator();
		while(it.hasNext())
		{
			AbstractCommand ac = (AbstractCommand)it.next();
			if(previousNextLabel != ac.getLabel())
				// on devra ajouter un jump -> on "réserve" 4 octects
				currentAddr += 4;
			currentAddr = ac.generateAddress(currentAddr);
			previousNextLabel = ac.getNextLabel();
		}
		_endAddrOfMethod = currentAddr;
		
		// le "+4" vient du fait qu'on doit rajouter un jump pour retourner dans la partie statique
		// code pour préparer le retour de l'appel de méthode
		return currentAddr+4;
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		Iterator it;

		sb.append(_methodName);
		if(_methodLevel > STATIC)
		{
			sb.append("/");
			sb.append(Integer.toString(_methodLevel));
		}
		
		sb.append("(");
		it = _paramList.iterator();
		while(it.hasNext())
		{
			sb.append(it.next());
			if(it.hasNext())
				sb.append(", ");
		}
		sb.append(")");
		sb.append(" { -> ");
		sb.append(_methodStartLabel);
		sb.append("\n");
		
		it = _stmt.iterator();
		while(it.hasNext())
		{
			sb.append("\t");
			sb.append(it.next());
			sb.append("\n");
		}
		sb.append("} ");
		
		sb.append(Integer.toString(_methodEndLabel));
		sb.append(" ");
		sb.append(_rv.toString());
		sb.append("\n");

		return sb.toString();
	}
}