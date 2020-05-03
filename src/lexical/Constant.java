package lexical;

public class Constant extends Token
{
	private Constant() {/* Need a word ! */ }
	
	public Constant(String c)
	{
		_text = c;
	}
	
	public String toString()
	{
		return "Constant: '"+_text+"'";	
	}
}
