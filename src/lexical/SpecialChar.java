package lexical;

public class SpecialChar extends Token
{
	private SpecialChar() {/* Need an word ! */ }
	
	public SpecialChar(String c)
	{
		_text = c;
	}
	
	public String toString()
	{
		return "Special character: '"+_text+"'";
	}
}
