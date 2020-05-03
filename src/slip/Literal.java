package slip;

public class Literal extends Instruction
{
	public static final int CHR = 1;
	public static final int INT = 2;
	
	/** adresse du literal dans le code */
	private int litAdr;
	
	/** type du literal */
	private int type;
	
	/** contenu du literal */
	private int value;
	private String msg;
	
	public Literal(int i)
	{
		type = INT;
		value = i;
	}

	public Literal(String s)
	{
		if(s.length() > 4)
			throw new IllegalArgumentException("Taille d'une literal de type caractère <= 4 !");
		type = CHR;
		msg = s;
	}
	
	public int getSize() { return 4; }
	
	public int getType() { return type; }
	
	public int getAddress() { return litAdr; }
	public void setAddress(int a) { litAdr = a; }
	
	public String getInstText()
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append(format(Integer.toString(litAdr), '0', 5, true));
		sb.append("   ");
		sb.append("LIT");
		sb.append(format("", ' ', 2, false));
		
		switch(type)
		{
			case CHR: sb.append("C'"+msg+"'"); break;
			case INT: sb.append("I("+value+")"); break;
		}
		
		return sb.toString();
	}
	
	public String toString()
	{
		switch(type)
		{
			case CHR: return msg;
			case INT: return Integer.toString(value);
			default: return "default";
		} 
	}
}