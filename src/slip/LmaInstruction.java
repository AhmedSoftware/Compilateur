package slip;

import java.util.HashSet;

public class LmaInstruction extends Instruction
{
	private static HashSet shortInst = new HashSet();
	static
	{
		shortInst = new HashSet();
		shortInst.add("HALT");
		shortInst.add("NOP");
		shortInst.add("ADD");
		shortInst.add("SUB");
		shortInst.add("MUL");
		shortInst.add("DIV");
		shortInst.add("COMP");
	}
	
	/** Taille de l'instruction */
	private int size;
	
	/**
	 * Si instText != null, instText sera l'instruction renvoyé par
	 * getInstText pour représenter l'objet
	 */
	private String instText;
	
	/** Adresse de l'instruction en mémoire */
	private int instructionAdr;
	
	/** Code opérationnel de l'instruction*/
	private String opcode;
	
	/** Arguments de l'instruction */
	private int arg1;				
	private int arg2;							
	private int optionalArg;
	
	public LmaInstruction()
	{
		instText = null;
		instructionAdr = Constant.INVALID;
		opcode = "";
		arg1 = Constant.INVALID;
		arg2 = Constant.INVALID;
		optionalArg = Constant.INVALID;
	}
	
	public LmaInstruction(String s)
	{
		this();
		instText = s;
	}
	
	public int getAddress() { return instructionAdr; }
	public void setAddress(int a) { instructionAdr = a; }
	
	public void setOpCode(String s)
	{
		opcode = s;
		size = shortInst.contains(s) ? 2 : 4;
	}
	
	public void setArg1(int i) { arg1 = i; }
	
	public void setArg2(int i) { arg2 = i; }
	
	public void setOptArg(int a) { optionalArg = a; }
	
	public int getSize() { return size; }
	
	/**
	 * Renvoit un string formaté selon les règles syntaxiques pour
	 * les instructions LMA représentant le contenu de l'objet
	 */
	public String getInstText()
	{
		if(instText != null)
			return instText;
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(format(Integer.toString(instructionAdr), '0', 5, true));
		sb.append(format("", ' ', 3, true));
		sb.append(format(opcode, ' ', 4, false));
		sb.append(" ");
		sb.append(format(Integer.toString(arg1), ' ', 2, true));
		sb.append(", ");
		
		if(optionalArg == Constant.INVALID)
		{
			// instruction courte
			sb.append(format(Integer.toString(arg2), ' ', 2, false));
		}
		else
		{
			// instruction longue
			sb.append(format(Integer.toString(optionalArg), '0', 5, true));
			if(arg2 != Constant.INVALID)
				sb.append(" (" + arg2 + ")");
		}				
		
		return sb.toString();
	}
}
