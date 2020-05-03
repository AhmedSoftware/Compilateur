package syntax;

import java.io.*;
import java.util.*;

public class LLGrammarChecker
{
	private static void printParseTable(Rule[][] table, Set T, Set NT)
	{
		HashMap intToTerm = new HashMap();
		HashMap intToNonTerm = new HashMap();
		
		Iterator it = T.iterator();
		while(it.hasNext())
		{
			Terminal t = (Terminal)it.next();
			intToTerm.put(new Integer(t.getParseTableIdx()), t.toString());
		}
		
		it = NT.iterator();
		while(it.hasNext())
		{
			NonTerminal nt = (NonTerminal)it.next();
			intToNonTerm.put(new Integer(nt.getParseTableIdx()), nt.toString());
		}
		
		for(int i=0; i<table.length; i++)
		{
			String A = (String)intToNonTerm.get(new Integer(i));
			for(int j=0; j<table[0].length; j++)
				if(table[i][j] != null)
					System.out.println("parseTable["+A+", "+intToTerm.get(new Integer(j))+"] = "+table[i][j]);
			}
	}
	
	private static boolean check1stCondition(Grammar g)
	{
		boolean isOk = true;
		
		// Clé: non-terminal, Valeur: production dont le membre de gauche est le non-terminal
		HashMap prodMap = new HashMap();

		Rule rule1, rule2;
		HashSet ruleSet;
		
		Iterator it = g.getRules().iterator();
		while (it.hasNext())
		{
			rule1 = (Rule) it.next();

		    ruleSet = (HashSet) prodMap.get(rule1.getLeft());
		    if (ruleSet == null)
		    {
		    	ruleSet = new HashSet();
		    	// le terminal n'était pas encore une clé dans prodMap
				prodMap.put(rule1.getLeft(), ruleSet);
		    }

		    ruleSet.add(rule1);
		}
		
		NonTerminal x;
		Iterator ri, rj;
		HashSet r1Set;
		HashSet r2Set;
		
		// Pour chaque X -> B1 | ... | Bn
		it = prodMap.keySet().iterator();
		while(it.hasNext())
		{
			x = (NonTerminal) it.next();
			ruleSet = (HashSet) prodMap.get(x);
			
			// Pour chaque Bi
			ri = ruleSet.iterator();
			while(ri.hasNext())
			{
				rule1 = (Rule) ri.next();
				
				// calcul de p1(Bi)
				r1Set = (HashSet) Grammar.concatSet(rule1.getRight(), g.getLambda());
				
				// pour chaque Bj
				rj = ruleSet.iterator();
				while(rj.hasNext())
				{
					rule2 = (Rule) rj.next();
					
					// si Bi != Bj
					if(rule2 != rule1)
					{
						// calcul de p1(Bj)
						r2Set = (HashSet) Grammar.concatSet(rule2.getRight(), g.getLambda());
						
						// p1(Bi) inter p1(Bj) = vide ?
						Iterator i = r2Set.iterator();
						while(i.hasNext())
						{
							if(r1Set.contains(i.next()))
							{
								System.out.println("Intersection de p1("+rule1+") et "+
												   "p1("+rule2+") non vide -> début de partie droite commune");
								isOk = false;
							}
						}
					}
				}
			}
		}
		
		return isOk;
	}
	
	private static boolean check2ndCondition(Grammar g)
	{
		boolean isOk = true;
		
		// Clé: non-terminal, Valeur: production dont le membre de gauche est le non-terminal
		HashMap prodMap = new HashMap();

		Rule rule1, rule2;
		HashSet ruleSet;
		
		Iterator it = g.getRules().iterator();
		while (it.hasNext())
		{
			rule1 = (Rule) it.next();

		    ruleSet = (HashSet) prodMap.get(rule1.getLeft());
		    if (ruleSet == null)
		    {
		    	ruleSet = new HashSet();
		    	// le terminal n'était pas encore une clé dans prodMap
				prodMap.put(rule1.getLeft(), ruleSet);
		    }

		    ruleSet.add(rule1);
		}
		
		NonTerminal x;
		Iterator ri;
		Iterator rj;
		HashSet r1Set;
		HashSet r2Set;
		
		// Pour chaque X -> B1 | ... | Bn
		it = prodMap.keySet().iterator();
		while(it.hasNext())
		{
			x = (NonTerminal) it.next();
			ruleSet = (HashSet) prodMap.get(x);
			
			// Pour chaque Bi
			ri = ruleSet.iterator();
			while(ri.hasNext())
			{
				rule1 = (Rule) ri.next();
				// avec Bi -> lambda (en 0 ou plusieurs dérivations)
				if(Grammar.concatSet(rule1.getRight(), g.getLambda()).contains(g.getLambda()))
				{
					// calcul de s1(X)
					r1Set = (HashSet) x.getS1();
					
					// pour chaque Bj
					rj = ruleSet.iterator();
					while(rj.hasNext())
					{
						rule2 = (Rule) rj.next();
						
						// si Bi != Bj
						if(rule2 != rule1)
						{
							// calcul de p1(Bj)
							r2Set = (HashSet) Grammar.concatSet(rule2.getRight(), g.getLambda());
							
							// s1(X) inter p1(Bj) = vide ?
							Iterator i = r2Set.iterator();
							while(i.hasNext())
							{
								if(r1Set.contains(i.next()))
								{
									System.out.println("Intersection de s1("+rule1.getLeft()+") et "+
											   "p1("+rule2.getRight()+") non vide");
									isOk = false;
								}
							}
						}
					}
				}
			}
		}
		
		return isOk;
	}
	
	public static void check(String inputFileName)
	{
		Grammar g = null;
		Rule[][] table;
		
		try
		{
			BNFGrammarParser p = new BNFGrammarParser(inputFileName);
			g = p.parse();
		}
		catch(IOException e)
		{
			System.out.println("Erreur d'entrée/sortie durant la vérification de la grammaire:");
			System.out.println(e.getMessage());
		}
		
		if(g == null)
		{
			System.out.println("Problème durant le parsing de la grammaire "+inputFileName);
			System.exit(-1);
		}
		
		System.out.println(g);
		System.out.println(g.p1ToString());
		System.out.println(g.s1ToString());
		
		if(check1stCondition(g))
			System.out.println("Première condition satisfaite");
		
		if(check2ndCondition(g))
			System.out.println("Deuxième condition satisfaite");
		
		table = LL1Parser.generateTable(g);
		printParseTable(table, g.getTerminal(), g.getNonTerminal());
	}
	
	public static void main(String[] argll_je_me_meurs)
	{	
		if(argll_je_me_meurs.length != 1)
		{
			System.out.println("LLGrammarChecker <path_to_bnf_grammar>");
			return ;
		}
		
		check(argll_je_me_meurs[0]);
	}
}