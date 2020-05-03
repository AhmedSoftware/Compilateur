package compiler;

import java.io.FileOutputStream;
import java.io.IOException;

import syntax.LL1Parser;
import syntax.ParseTree;
import translator.SLIPGenerator;
import slip.Program;
import slip.LmaGenerator;

public class Compiler
{
	public static void main(String[] arg)
	{
		if(arg.length != 1)
		{
			System.out.println("Compiler <input_file_name>");
			return ;
		}
		
		String name = getFileName(arg[0]);
		if(name == null)
		{
			System.err.println("Invalid file name");
			return ;
		}
		
		try
		{
			FileOutputStream fos;
			
			System.out.println("Parsing du fichier source:");
			System.out.println("--------------------------");
			ParseTree tree = LL1Parser.parse(arg[0]);
			if(tree == null)
				return;
			
			System.out.println("\nGeneration de la representation interne:");
			System.out.println("----------------------------------------");
			SLIPGenerator slipGen = new SLIPGenerator(tree, LL1Parser.getLambda().getText());
			Program pgm = slipGen.getProgram();
			if(pgm == null)
				return;
			
			// Ecrit dans un fichier '.int' la repr?sentation interne du programme
			fos = new FileOutputStream(name+".int");
			fos.write(pgm.toString().getBytes());
			fos.write('\n');
			
			System.out.println("\nGénération du code lma:");
			System.out.println("-----------------------");
			LmaGenerator gen = new LmaGenerator(pgm);
			// Ecrit dans un fichier '.lma' le code lma correspondant au programme pgm
			fos = new FileOutputStream(name+".lma");
			gen.writeCode(fos);
			fos.write('\n');
		}
		catch(InvalidProgramException pe)
		{
			System.out.println("Probl?me lors de la compilation de '"+arg[0]+"':");
			System.out.println(pe.getMessage());
		}
		catch(IOException e)
		{
			System.out.println(e.getMessage());
		}
	}
	
	private static String getFileName(String s)
	{
		StringBuffer sb = new StringBuffer(s);
		int extIndex = sb.lastIndexOf(".");
		
		if(extIndex == -1)
			return s;
		return sb.substring(0, extIndex);
	}
}