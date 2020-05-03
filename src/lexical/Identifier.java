package lexical;

public class Identifier extends Token
{
	private Identifier() { /* Need an identifier ! */ }
	
	public Identifier(String id)
	{
		_text = id;
	}
	
	public String toString()
	{
		return "Identifier: '"+_text+"'";
	}
}
