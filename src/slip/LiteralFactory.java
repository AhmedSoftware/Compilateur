package slip;

import java.util.*;

public class LiteralFactory
{
	private final static int startAddr = 65536;
	private static int currentAddr = startAddr;
	private static HashMap existingLiteral = new HashMap();
	
	public static int addLiteral(List l, int value)
	{
		Literal lit = new Literal(value);
		return add(l, new Integer(value), lit);
	}
	
	public static int addLiteral(List l, String value)
	{
		Literal lit = new Literal(value);		
		return add(l, value, lit);
	}
	
	public static int getCurrentAddress() { return currentAddr; }
	
	private static int add(List l, Object key, Literal lit)
	{
		int addr;
		currentAddr -= 4;
		
		Integer oldAddr = (Integer)existingLiteral.get(key);
		if(oldAddr == null)
		{
			// nouveau litéral
			addr = currentAddr;
			existingLiteral.put(key, new Integer(addr));
			l.add(lit);
		}
		else
			// renvoit l'adresse de l'ancien littéral
			addr = oldAddr.intValue();

		lit.setAddress(addr);
		
		return addr;
	}
}
