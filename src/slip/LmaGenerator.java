package slip;

import java.util.*;
import java.io.*;

public class LmaGenerator
{
	/** Racine de l'arbre à traduire*/
	private static Program pgm;
	
	/** Ensemble des instructions du programme traduit */
	private LinkedList pgmInstructions;
	
	/** Table d'indirection pour les méthodes dynamiques */
	private static IndirectionTable table;
	
	/** Adresse du début de la pile */
	private LmaInstruction stackInit;
	
	/** Adresse du début du heap */
	private LmaInstruction heapInit;
	
	/**
	 * @pre:
	 *  - p est la racine d'un arbre en syntaxe abstraite
	 *    représentant une programme valide.
	 * 
	 * @post:
	 *  - L'ensemble des instructions LMA correspondant à la
	 *    représentation p du program a été généré   
	 */
	public LmaGenerator(Program p)
	{
		int stackBeginning;
		int heapBeginning;
		int mainBeginning;
		
		pgm = p;
		pgmInstructions = new LinkedList();
		
		// on génère les tables d'indirection pour connaitre leur taille
		table = new IndirectionTable(pgm.getMethDeclList());
		
		// on genère toutes les adresses avant de créer les instructions
		stackBeginning = p.generateAddress(table.getEndAddress());
		
		// une fois toutes les adresses connues, on peut créer le contenu des tables
		table.getInstruction(pgmInstructions);
		
		// génération des instructions
		pgm.getInstruction(pgmInstructions);
		
		// ajout du code d'initilisation du pgm
		addInitialisationInstruction(stackBeginning, pgm.getMainMethod().getStartAddress());
	}
	
	/**
	 * Ajoute les instructions d'initialisation au début de la liste des instructions
	 */
	private void addInitialisationInstruction(int stackBeginning, int mainBeginning)
	{	
		LmaInstruction jumpToMain;
		int litAdr = LiteralFactory.addLiteral(pgmInstructions, "STOP");
		
		LmaInstruction frameInit = new LmaInstruction();
		frameInit.setOpCode("LDA");
		frameInit.setArg1(Constant.REG_FRAME);
		frameInit.setArg2(0);
		frameInit.setAddress(0);
		
		stackInit = new LmaInstruction();
		stackInit.setOpCode("LDA");
		stackInit.setArg1(Constant.REG_STACK);
		stackInit.setArg2(stackBeginning);
		stackInit.setAddress(4);
		
		heapInit = new LmaInstruction();
		heapInit.setOpCode("LDA");
		heapInit.setArg1(Constant.REG_HEAP);
		heapInit.setArg2(LiteralFactory.getCurrentAddress()-4);
		heapInit.setAddress(8);
		
		jumpToMain = new LmaInstruction();
		jumpToMain.setOpCode("JUMP");
		jumpToMain.setArg1(Constant.REG_RETADR);
		jumpToMain.setArg2(mainBeginning);
		jumpToMain.setAddress(12);
		
		LmaInstruction msg = new LmaInstruction();
		msg.setOpCode("LDM");
		msg.setArg1(Constant.REG_MSG);
		msg.setArg2(litAdr);
		msg.setAddress(16);
		
		LmaInstruction stop = new LmaInstruction();
		stop.setOpCode("HALT");
		stop.setArg1(0);
		stop.setArg2(0);
		stop.setAddress(20);
		
		LmaInstruction jumpBackToHalt = new LmaInstruction();
		jumpBackToHalt.setOpCode("JUMP");
		jumpBackToHalt.setArg1(0);
		jumpBackToHalt.setArg2(20);
		jumpBackToHalt.setAddress(22);
		
		// ajout des instructions au programme
		pgmInstructions.addFirst(jumpBackToHalt);
		pgmInstructions.addFirst(stop);
		pgmInstructions.addFirst(msg);
		pgmInstructions.addFirst(jumpToMain);
		pgmInstructions.addFirst(heapInit);
		pgmInstructions.addFirst(stackInit);
		pgmInstructions.addFirst(frameInit);
	}
	
	/**
	 * @pre:
	 *  - os contient une référence valide vers le fichier dans lequel
	 *    on doit écrire les instructions.
	 *  - pgmInstructions contient la suite non-vides des instructions
	 *    à écrire dans os
	 * 
	 * @post:
	 *  - la représentation correcte (respectant la syntaxe LMA) sous
	 *    forme de text de toutes les instructions est écrite dans
	 *    os.
	 * 
	 * @throws IOException si une erreur d'écriture se produit
	 */
	public void writeCode(OutputStream os) throws IOException
	{
		Iterator i;
		Instruction inst;
		
		i = pgmInstructions.iterator();
		while(i.hasNext())
		{
			inst = (Instruction)i.next();
			os.write(inst.getInstText().getBytes());
			os.write('\n');
		}
	}
	
	public static IndirectionTable getIndirectionTable() { return table; }
	
	public static int getMaxLevel(String name) { return pgm.getMaxLevel(name); }
	
	public static MethodDeclaration getMethod(String name, int level)
	{
		return pgm.getMethod(name, level);
	}
}
