package lexical;

public class ReservedWord extends Token
{
	private ReservedWord() {/* Need an word ! */ }
	
	public ReservedWord(String w)
	{
		_text = w;
	}
	
	public String toString()
	{
		return "Reserved word: '"+_text+"'";
	}
}
