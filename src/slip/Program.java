package slip;

import java.util.*;

import compiler.InvalidProgramException;

public class Program extends AbstractNode
{
	private LinkedList declarations;
	private MethodDeclaration main;
	
	public Program()
	{
		main = null;
		declarations = new LinkedList();
	}
	
	public void addMethod(MethodDeclaration met)
	{
		if(met.getMethodName().equals("main"))
			main = met;
		declarations.add(met);
	}
	
	public LinkedList getMethDeclList() { return declarations; }
	
	public MethodDeclaration getMainMethod() { return main; }
	
	public MethodDeclaration getMethod(String name, int level)
	{
		Iterator it = declarations.iterator();
		MethodDeclaration md;
		while(it.hasNext())
		{
			md = (MethodDeclaration)it.next();

			if(md.getMethodName().equals(name) && md.getMethodLevel() == level)
				return md;
		}
		
		return null;
	}
	
	public int getMaxLevel(String name)
	{
		int currentMaxLevel = -1;
		
		Iterator it = declarations.iterator();
		MethodDeclaration md;
		while(it.hasNext())
		{
			md = (MethodDeclaration)it.next();
			
			if(md.getMethodName().equals(name) && md.getMethodLevel() > currentMaxLevel)
				currentMaxLevel = md.getMethodLevel();
		}
		
		return currentMaxLevel;
	}
	
	public void getInstruction(List l)
	{
		if(main == null)
			throw new InvalidProgramException("le programme doit au moins contenir une méthode main");
		
		Iterator it = declarations.iterator();
		while(it.hasNext())
			((MethodDeclaration)it.next()).getInstruction(l);
	}
	
	public int generateAddress(int startAddr)
	{
		int currentAddr = startAddr;
		Iterator it = declarations.iterator();
		while(it.hasNext())
			currentAddr = ((MethodDeclaration)it.next()).generateAddress(currentAddr);
		
		return currentAddr; 
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		
		Iterator it = declarations.iterator();
		while(it.hasNext())
		{
			sb.append(((MethodDeclaration)it.next()).toString());
			sb.append("\n");
		}
		
		return sb.toString();
	}
}
