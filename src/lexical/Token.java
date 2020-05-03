package lexical;

/**
 * Définition d'une classe mère à tous les symboles renvoyés par l'analyseur lexical
 */
public abstract class Token
{
	protected String _text;
	
	public abstract String toString();
	
	public String getText()
	{
		return _text;
	}
	
	public int hashCode()
	{
		return _text.hashCode();
	}
	
	public boolean equals(Object o)
	{
		if(!(o instanceof Token))
			return false;
		
		return _text.equals(((Token)o).getText());
	}
}
