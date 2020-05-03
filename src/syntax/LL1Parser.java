package syntax;

import lexical.*;

import java.io.*;
import java.util.*;

public class LL1Parser
{
	private static Rule[][] _parseTable;
	private static Lexer _lexer;
	private final static String CST_STR = "cst";
	private final static String IDENT_STR = "identifier";
	private final static Token CST = new Constant("constant");
	private final static Token IDENT = new Identifier("identifier");
	
	private static Terminal _lambda;
	private static NonTerminal _startSymbol;
	
	private final static int ERR_NOMATCH = 1;
	private final static int ERR_NORULE = 2;

	/**
	 * Cl�: symbole de l'analyseur lexical
	 * Valeur: indice du terminal correspondant dans la table de parse
	 */
	private static Map _symbolToIndex;
	
	/**
	 * inputFileName: nom du fichier contenant le programme slip
	 */
	private static boolean initParser(String inputFileName)
	{
		BNFGrammarParser p;
		Grammar g;
		Iterator it;
		Terminal t;
		String text;
		Token ls;
		
		try
		{
			_lexer = new Lexer(inputFileName);
			p = new BNFGrammarParser("/home/aali/compilateur/src/syntax/grammar.txt");
			
			g = p.parse();
			if(g == null)
				return false;
		}
		catch (IOException e)
		{
			System.out.println("Erreur pendant l'initialisation du parser: "+e.getMessage());
			return false;
		}
		
		_lambda = g.getLambda();
		_startSymbol = g.getStartSymbol();
		_parseTable = generateTable(g);
		
		_symbolToIndex = new HashMap();
		it = g.getTerminal().iterator();
		while(it.hasNext())
		{
			ls = null;
			t = (Terminal)it.next();
			text = t.getText();
			
			if(text.compareTo("<=") == 0)
				ls = Lexer.SCPPE;
			else if(text.compareTo("<") == 0)
				ls = Lexer.SCPPQ;
			else if(text.compareTo(">=") == 0)
				ls = Lexer.SCPGE;
			else if(text.compareTo(">") == 0)
				ls = Lexer.SCPGQ;
			else if(text.compareTo("==") == 0)
				ls = Lexer.SCEGA;
			else if(text.compareTo("!=") == 0)
				ls = Lexer.SCDIF;
			else if(text.compareTo(".") == 0)
				ls = Lexer.SCDOT;
			else if(text.compareTo("/") == 0)
				ls = Lexer.SCDIV;
			else if(text.compareTo("=") == 0)
				ls = Lexer.SCEQUAL;
			else if(text.compareTo("*") == 0)
				ls = Lexer.SCSTAR;
			else if(text.compareTo("+") == 0)
				ls = Lexer.SCPLUS;
			else if(text.compareTo("-") == 0)
				ls = Lexer.SCMINUS;
			else if(text.compareTo("%") == 0)
				ls = Lexer.SCMOD;
			else if(text.compareTo("(") == 0)
		 		ls = Lexer.SCOPAR;
			else if(text.compareTo(")") == 0)
		 		ls = Lexer.SCCPAR;
			else if(text.compareTo(",") == 0)
		 		ls = Lexer.SCSEP;
			else if(text.compareTo(";") == 0)
		 		ls = Lexer.SCSEMIC;
			else if(text.compareTo("{") == 0)
		 		ls = Lexer.SCOBRAC;
			else if(text.compareTo("}") == 0)
		 		ls = Lexer.SCCBRAC;
			else if(text.compareTo("#") == 0)
		 		ls = Lexer.SCSHARP;
			else if(text.compareTo("if") == 0)
		 		ls = Lexer.RWIF;
			else if(text.compareTo("else") == 0)
		 		ls = Lexer.RWELSE;
			else if(text.compareTo("read") == 0)
		 		ls = Lexer.RWREAD;
			else if(text.compareTo("write") == 0)
		 		ls = Lexer.RWWRITE;
			else if(text.compareTo("while") == 0)
		 		ls = Lexer.RWWHILE;
			else if(text.compareTo("this") == 0)
		 		ls = Lexer.RWTHIS;
			else if(text.compareTo("null") == 0)
		 		ls = Lexer.RWNULL;
			else if(text.compareTo("new") == 0)
		 		ls = Lexer.RWNEW;
			else if(text.compareTo("return") == 0)
		 		ls = Lexer.RWRETURN;
			else if(text.compareTo("super") == 0)
		 		ls = Lexer.RWSUPER;
			else if(text.equals(IDENT_STR))
				ls = IDENT;
			else if(text.equals(CST_STR))
				ls = CST;
			else if(t.equals(g.getLambda()))
				ls = Lexer.EOF;
			
			_symbolToIndex.put(ls, new Integer(t.getParseTableIdx()));
		}
		
		return true;
	}
	
	/**
	 * @pre:
	 *  - inputFileName est un nom de fichier slip valide 
	 * 
	 * @post:
	 *  - renvoie null si la chaine de lex�mes n'appartient au langage
	 *    d�finit par la grammaire
	 *  - renvoie l'arbre syntaxique si la chaine de lex�mes appartient
	 *    au langage d�fini par la grammaire. Le noeud racine de l'arbre est
	 *    le symbole initial. Pour tout noeud interne de l'arbre contenant
	 *    le symbole X, les symboles contenus dans les noeuds descendants
	 *    de celui-ci sont les symboles d'une partie droite de r�gle de la
	 *    grammaire de la forme X -> Y1 ... Yn avec Y1, ..., Yn appartenant
	 *    aux terminaux et aux non-terminaux.
	 * 
	 * NOTE: renvoie aussi nul si l'initialisation du parseur a �chou� mais
	 *       un messsage d'erreur ad�quat a �t� affich�   
	 */
	public static ParseTree parse(String inputFileName)
	{
		/*
		 * Initialisation:
		 *  - cr�ation du lexer qui renverra la suite de lex�me,
		 *  - chargement de la grammaire du fichier syntax\grammar.txt,
		 *  - v�rification des propri�t�s de la grammaire,
		 *  - cr�ation de la table de parsing,
		 *  - initialisation de la liste des indices correspondants �
		 *    chaque terminal pour les retrouver dans la table.
		 */
		if(!initParser(inputFileName))
			return null;
		
		// flag pour sortir de la boucle quand une erreur
		// syntaxique est d�tect�e
		boolean syntaxError = false;
		
		// Lit le premier lex�me
		Token token = _lexer.getNextSymbol();
		
		// Symbol contenu dans le noeud pris au sommet de la pile
		Symbol x = null;
		
		// Pile de symbole terminaux et non-terminaux
		Stack stack = new Stack();
		
		// Racine de l'arbre syntaxique
		ParseTree root = new ParseTree(_startSymbol);
		
		// Arbre (sans enfant quand on le sort de la pile)
		// contenant x
		ParseTree xTree;
		
		stack.push(root);
		
		while(!stack.isEmpty() && syntaxError == false)
		{
			// R�cup�ration du premier symbole au sommet de la pile
			xTree = (ParseTree)stack.pop();
			x = (Symbol)xTree.getSymbol();
			
			if(x instanceof Terminal)
			{
				// Si x est un terminal et que ...
				if((x.getText().equals(IDENT_STR) && (token instanceof Identifier)) ||
				   (x.getText().equals(CST_STR) && (token instanceof Constant)))
				{
					// ... x correspond au lex�me, on enl�ve x de la pile (d�j� fait)
					// et on lit un nouveau lex�me. Pour les constantes et les identifieurs
					// on sauvegarde le texte re�u du lexer.
					xTree.setValue(token.getText());
					token = _lexer.getNextSymbol();
				}
				else if(x.getText().equals(token.getText()))
					// ... sauf que pour les autres symboles,
					// il n'y a pas de texte � sauver
					token = _lexer.getNextSymbol();						
				else if(!x.equals(_lambda))
				{
					// ... x ne correspond pas au lex�me -> SYNTAX ERROR
					reportError(ERR_NOMATCH, x, token.toString());
					syntaxError = true;
				}
			}
			else if(x instanceof NonTerminal)
			{
				// Si x est un non-terminal 
				Rule r;
				int termIdx;
				
				// calcul indice correspondant au lex�me
				if(token instanceof Identifier)
					termIdx = ((Integer)_symbolToIndex.get(IDENT)).intValue();
				else if(token instanceof Constant)
					termIdx = ((Integer)_symbolToIndex.get(CST)).intValue();
				else
					termIdx = ((Integer)_symbolToIndex.get(token)).intValue();
				
				// R�cup�ration de la r�gle
				r = _parseTable[x.getParseTableIdx()][termIdx];
				if(r == null)
				{
					// s'il n'y pas de r�gle dans table[x, token] -> SYNTAX ERROR 
					reportError(ERR_NORULE, x, token.getText());
					syntaxError = true;
				}
				else
				{
					// table[x, token] = (X ::= A1 ... An)  
					ParseTree newNode;
					LinkedList right = r.getRight();
					Symbol ss;
					int i = right.size()-1; 
					
					// En place tous les Ai dans la pile en commencer
					// par i=n jusqu'� i=1
					while(0 <= i)
					{
						ss = (Symbol)right.get(i);
						newNode = new ParseTree(ss);
						
						xTree.addChild(newNode);
						stack.push(newNode);
						i -= 1;
					}
				}	
			}
		}
		
		// S'il n'y a pas d'erreur syntaxique, que la pile est vide est que
		// le dernier lex�me lu est le symbole de fin du fichier.
		if(token.equals(Lexer.EOF) && stack.isEmpty() && syntaxError == false)
			return root;
		
		// Erreur syntaxique
		return null;
	}
	
	private static void reportError(int type, Object a, Object b)
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append("ERREUR DE SYNTAXE (Ligne: ");
		sb.append(_lexer.getCurrentRow());
		sb.append(" Colone: ");
		sb.append(_lexer.getCurrentCol());
		sb.append(")\n");
		
		if(type == ERR_NOMATCH)
		{
			
			sb.append("le terminal au sommet de la pile et le token du fichier source diff�re");
			sb.append("\nSymbole au sommet de la pile: ");
			sb.append(a);
			sb.append("\nSymbole lu dans le fichier source: ");
			sb.append(b);
		}
		else if(type == ERR_NORULE)
		{
			sb.append("parseTable[");
			sb.append(a);
			sb.append(", ");
			sb.append(b);
			sb.append("] n'a pas de r�gle.");
		}
		
		System.out.println(sb.toString());
	}
	
	public static Terminal getLambda()
	{
		return _lambda;
	}

	/**
	 * G�n�re la table de parsing pour la grammaire g.
	 * 
	 * R�sultat:
	 *  - null si la grammaire n'est pas LL(1)
	 *  - un tableau de productions dont une case se trouvant sur la colone
	 *    i et la ligne j contient la r�gle � appliquer lorsqu'on rencontre
	 *    le terminal Ti dans la chaine d'input et que NTj est au sommet
	 *    de la pile.
	 */
	public static Rule[][] generateTable(Grammar g)
	{
		Terminal lambda = g.getLambda();
		LinkedList prod = g.getRules();
		Set NT = g.getNonTerminal();
		
		Rule[][] table = new Rule[g.getNumberOfNonTerminal()][g.getNumberOfTerminal()];
		
		Set p1Right;
		Set s1Left;
		
		Rule p;
		NonTerminal A;
		Terminal a;
		Terminal b;
		LinkedList alpha;
		
		/*
		 * Pour tout p ds prod tel que p = (A -> alpha)
		 * faire
		 * 		Pour tout a ds p1(alpha), table[A, a]=(A -> alpha)
		 * 		Si lambda ds p1(alpha) alors pour tout b ds s1(A) table[A, b]=(A -> alpha)
		 */
		boolean containsLambda;
		Iterator lit;
		Iterator rit;
		Iterator it = prod.iterator();
		while(it.hasNext())
		{
			p = (Rule)it.next();
			
			A = p.getLeft();
			alpha = p.getRight();
			
			p1Right = Grammar.concatSet(alpha, lambda);
			rit = p1Right.iterator();
			containsLambda = false;
			while(rit.hasNext())
			{
				a = (Terminal)rit.next();
				
				if(a.equals(lambda))
					containsLambda = true;
				else
					table[A.getParseTableIdx()][a.getParseTableIdx()] = p;
			}
			
			if(containsLambda)
			{
				s1Left = A.getS1();
				lit = s1Left.iterator();
				while(lit.hasNext())
				{
					b = (Terminal)lit.next();
					table[A.getParseTableIdx()][b.getParseTableIdx()] = p;
				}
			}
		}
		
		return table;
	}
}
