package syntax;

import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

public class BNFGrammarParser
{
	private final Symbol INVALID_SYMBOL = new Symbol("");
	
	private NonTerminal _startSymbol;
	private Terminal _lambda;
	private HashMap _T;
	private HashMap _NT;
	private LinkedList _rules;
	private BufferedReader _input;
	
	public BNFGrammarParser(String fileName) throws FileNotFoundException
	{
		_T = new HashMap();
		_NT = new HashMap();
		_rules = new LinkedList();
		
		_input = new BufferedReader(new FileReader(fileName));
	}
	
	// On suppose que l'utilisateur n'est pas assez bete que pour entrer deux fois la meme regle !
	// Meme si tel était le cas, ca "devrait" fonctionner (non tester)
	public Grammar parse() throws IOException
	{
		String line;
		Symbol s;
		
		// première ligne du fichier = symbole initial
		s = getSymbol(_input.readLine());
		if(s instanceof NonTerminal)
			_startSymbol = (NonTerminal)s;
		else
		{
			System.out.println("Première ligne de la grammaire = symbole initial !");
			return null;
		}
		
		// deuxième ligne du fichier = symbole nul
		s = getSymbol(_input.readLine());
		if(s instanceof Terminal)
			_lambda = (Terminal)s;
		else
		{
			System.out.println("Deuxième ligne de la grammaire = symbole vide !");
			return null;
		}
		
		line = _input.readLine();
		while(line != null)
		{
			if(line.length() == 0 || line.startsWith("#")) { line = _input.readLine(); continue; }
			
			String[] splitRes = line.trim().split("::=");
			String token;
			StringTokenizer st;
			Symbol left;
			LinkedList right;
			
			if(splitRes.length != 2)
			{
				System.out.println("Ligne: '"+line+": Production: <partie_gauche> ::= <partie_droite>");
				return null;
			}
			
			left = getSymbol(splitRes[0].trim());
			if(!(left instanceof NonTerminal))
			{
				System.out.println("Ligne: '"+line+": Partie gauche de règle DOIT etre un non-terminal !");
				return null;
			}
			
			st = new StringTokenizer(splitRes[1]);
			right = new LinkedList();
			while(st.hasMoreTokens())
			{
				token = st.nextToken();
				
				if(token.equals("|"))
				{
					if(right.isEmpty())
					{
						System.out.println("Ligne: '"+line+"': | doit etre utiliser pour séparer deux regles");
						return null;
					}
					
					// on est à la fin d'une règle avec le terminal left et
					// right contient tous les symboles de la règle
					_rules.add(new Rule((NonTerminal)left, right));
					right = new LinkedList(); // on doit recréer une nouvelle liste pour ne pas modifier
											  // celle qu'on a mis dans la production
				}
				else
				{
					s = getSymbol(token);
					if(!(s instanceof Terminal) && !(s instanceof NonTerminal))
					{
						System.out.println("Invalid symbole: '"+token+"' at line: '"+line+"'");
						return null;
					}
					right.add(s);
				}
			}
			
			// CLOT
			// On ajoute la denière regle qui n'a pas été ajoutée
			// comme il n'y a pas de | à la fin d'une ligne
			if(right.isEmpty())
			{
				System.out.println("Line: '"+line+"': illegal use of |");
				return null;
			}
			
			// on est à la fin d'une règle avec le terminal left et
			// right contient tous les symboles de la règle
			_rules.add(new Rule((NonTerminal)left, right));
			
			line = _input.readLine();
		}
		
		return new Grammar(_startSymbol, _lambda, new HashSet(_T.values()), new HashSet(_NT.values()), _rules);
	}
	
	private Symbol getSymbol(String s)
	{
		if(s == null || s.length() < 2)
			return INVALID_SYMBOL;
		
		Symbol sym = null;
		StringBuffer sb = new StringBuffer(s);
		char c1 = sb.charAt(0);
		char cn = sb.charAt(sb.length()-1);
		
		sb.deleteCharAt(0);
		sb.deleteCharAt(sb.length()-1);
		String name = sb.toString();
		
		if(c1 == '\'' && cn == '\'')
		{
			sym = (Terminal)_T.get(name);
			if(sym == null)
			{
				sym = new Terminal(name);
				_T.put(name, sym);
			}
		}
		else if(c1 == '<' && cn == '>')
		{
			sym = (NonTerminal)_NT.get(name);
			if(sym == null)
			{
				sym = new NonTerminal(name);
				_NT.put(name, sym);
			}
		}
		else
			sym = INVALID_SYMBOL;
		
		return sym;
	}
}
