package compiler;

public class InvalidProgramException extends RuntimeException
{
	public InvalidProgramException()
	{
		super("Programme incorrect");
	}
	
	public InvalidProgramException(String s)
	{
		super("Programme incorrect: "+s);
	}
}
