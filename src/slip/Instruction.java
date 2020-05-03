package slip;

public abstract class Instruction
{
	public abstract String getInstText();
	
	public abstract int getAddress();
	
	public abstract void setAddress(int a);
	
	public abstract int getSize();
	
	/**
	 * Renvoit un string ayant un taille de n caract�res
	 * Si la taille du string s est inf�rieur � n, la m�thode
	 * rajoute le caract�re devant ou derri�re le string s 
	 * slon la valeur de addInFront
	 */
	public static String format(String s, char c, int n, boolean addInFront)
	{
		StringBuffer sb = new StringBuffer(s);
		
		while(sb.length()<n)
			if(addInFront)
				sb.insert(0, c);
			else
				sb.append(c);
		
		return sb.toString();
	}
}
