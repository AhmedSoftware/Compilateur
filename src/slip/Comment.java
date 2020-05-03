package slip;

public class Comment extends Instruction
{
	private final static String commentDelimiter = "// ";
	private String commentText;
	
	public Comment(String s)
	{
		commentText = s;
	}
	
	public String getInstText()
	{
		return commentDelimiter+commentText;
	}
	
	// Pas de sens pour un commentaire
	public int getAddress() { return 0; }
	
	// Pas de sens pour un commentaire
	public void setAddress(int i) { }
	
	// Pas de sens pour un commentaire
	public int getSize() { return 0; }
}
