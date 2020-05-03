package lexical;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;

import compiler.InvalidProgramException;

public class Lexer
{
	private Peeker _peeker;
	
	/** Op�rateurs relationnels */
	public final static Token SCPPE = new SpecialChar("<=");
	public final static Token SCPPQ = new SpecialChar("<");
	public final static Token SCPGE = new SpecialChar(">=");
	public final static Token SCPGQ = new SpecialChar(">");
	public final static Token SCEGA = new SpecialChar("==");
	public final static Token SCDIF = new SpecialChar("!=");
	
	/** Caract�res sp�ciaux du langage */
	public final static Token SCDOT = new SpecialChar(".");
	public final static Token SCDIV = new SpecialChar("/");
	public final static Token SCEQUAL = new SpecialChar("=");
	public final static Token SCSTAR = new SpecialChar("*");
	public final static Token SCPLUS = new SpecialChar("+");
	public final static Token SCMINUS = new SpecialChar("-");
	public final static Token SCMOD = new SpecialChar("%");
	public final static Token SCOPAR = new SpecialChar("(");
	public final static Token SCCPAR = new SpecialChar(")");
	public final static Token SCSEP = new SpecialChar(",");
	public final static Token SCSEMIC = new SpecialChar(";");
	public final static Token SCOBRAC = new SpecialChar("{");
	public final static Token SCCBRAC = new SpecialChar("}");
	public final static Token SCSHARP = new SpecialChar("#");
	
	/** Mots r�serv�s */
	public final static Token RWIF = new ReservedWord("if");
	public final static Token RWELSE = new ReservedWord("else");
	public final static Token RWREAD = new ReservedWord("read");
	public final static Token RWWRITE = new ReservedWord("write");
	public final static Token RWWHILE = new ReservedWord("while");
	public final static Token RWTHIS = new ReservedWord("this");
	public final static Token RWNULL = new ReservedWord("null");
	public final static Token RWNEW = new ReservedWord("new");
	public final static Token RWRETURN = new ReservedWord("return");
	public final static Token RWSUPER = new ReservedWord("super");
	
	/** Symbole le d�but et la fin des commentaires */
	public final static char CMT_START_SYMBOL = '[';
	public final static char CMT_END_SYMBOL = ']'; 
	
	/** Symbole sp�cial identifiant la fin du fichier */
	public final static Token EOF = new SpecialChar("EOF");
	
	/** Les diff�rentes constantes pour la machine � �tat fini */
	private final static int A=0, B=1, C=2, D=3, E=4, F=5, G=6, H=7, I=8, T=9;
	
	/** Cl�: chaine de caract�re, Valeur: objet correspondant au string */
	private HashMap _specialCharMap;
	private HashMap _reservedWordMap;

	private Lexer() { /* Need some input */ }
	
	/**
	 * @pre:
	 *  - fileName n'est pas nul
	 * 
	 * @post:
	 *  - Un peeker est cr�e avec son pointeur d'entr�e sur le premier caract�re du fichier
	 *  - L'�tat de l'analyseur lexical est son �tat initial
	 *  - Le num�ro de la ligne actuelle est 1 
	 *  - Le num�ro de la colonne actuelle st 1
	 * 
	 * @throws IOException s'il y a un probl�me lors de l'ouverture du fichier 
	 */
	public Lexer(String fileName) throws IOException
	{
		_peeker = new Peeker(new FileInputStream(fileName));

		_reservedWordMap = new HashMap();
		_reservedWordMap.put("if", RWIF);
		_reservedWordMap.put("else", RWELSE);
		_reservedWordMap.put("read", RWREAD);
		_reservedWordMap.put("write", RWWRITE);
		_reservedWordMap.put("while", RWWHILE);
		_reservedWordMap.put("this", RWTHIS);
		_reservedWordMap.put("null", RWNULL);
		_reservedWordMap.put("new", RWNEW);
		_reservedWordMap.put("return", RWRETURN);
		_reservedWordMap.put("super", RWSUPER);
		
		_specialCharMap = new HashMap();
		_specialCharMap.put("<=", SCPPE);
		_specialCharMap.put("<", SCPPQ);
		_specialCharMap.put(">=", SCPGE);
		_specialCharMap.put(">", SCPGQ);
		_specialCharMap.put("==", SCEGA);
		_specialCharMap.put("!=", SCDIF);
		_specialCharMap.put(".", SCDOT);
		_specialCharMap.put("/", SCDIV);
		_specialCharMap.put("=", SCEQUAL);
		_specialCharMap.put("*", SCSTAR);
		_specialCharMap.put("+", SCPLUS);
		_specialCharMap.put("-", SCMINUS);
		_specialCharMap.put("%", SCMOD);
		_specialCharMap.put("(", SCOPAR);
		_specialCharMap.put(")", SCCPAR);
		_specialCharMap.put(",", SCSEP);
		_specialCharMap.put(";", SCSEMIC);
		_specialCharMap.put("{", SCOBRAC);
		_specialCharMap.put("}", SCCBRAC);
		_specialCharMap.put("#", SCSHARP);
	}
	
	/**
	 * @pre:
	 *  - l'�tat actuel de l'analyseur est son �tat initial
	 *  - p est la position actuelle du pointeur d'entr�e du peeker  
	 * 
	 * @post:
	 *  - le pointeur d'entr�e du peeker est sur le caract�re suivant
	 *    le dernier caract�re du symbole renvoy�
	 *  - retourne le symbole correspondant aux caract�res lus du fichier
	 *    depuis la position p jusqu'� la position actuelle et l'�tat de
	 *    l'analyseur est son �tat initial (s'il y avait encore des choses
	 *    � lire)
	 *  - retourne EOF s'il n'y a plus rien � lire
	 *  - le num�ro de ligne est augment� du nombre de ligne parcourue pour
	 *    trouver le symbole suivant
	 *  - le num�ro de la colonne a �t� augment� du nombre de caract�re lu
	 *    et a �t� remis � 1 si on est pass� � la ligne suivante.  
	 * 
	 * @throws InvalidProgramException si un caract�re ill�gal est rencontr� dans
	 *         le fichier source.
	 */
	public Token getNextSymbol()
	{
		int state = A;
		StringBuffer lexeme = new StringBuffer();
		char c;
		
		try
		{
			// la deuxi�me partie de la condition est n�cessaire si jamais le fichier source
			// ne se terminait pas par une ligne vide. 
			while(_peeker.hasNext() || lexeme.length() != 0)
			{
				switch(state)
				{
					case A:
						if(Character.isWhitespace(_peeker.peek()))
						{
							_peeker.get();
							continue;
						}
		
						c = _peeker.get();
						lexeme.append(c);
						switch(c)
						{
							// operateur de relation (entre autre)
							case '<': state=B;
								break;
								
							case '>': state=C;
								break;
								
							case '!':
								if(_peeker.peek() == '=')
								{
									_peeker.get();
									return SCDIF;
								}
								else
								{
									lexeme.append('!');
									state = T;
								}
								break;
								
							case '=': state=E;
								break;
							
							// caract�res sp�ciaux
							case ',': case '.': case '(': case ')': case '{': case '}':
							case ';': case '+': case '-': case '*': case '/': case '%':
							case '#': state = H; break;
							
							// commentaire
							case CMT_START_SYMBOL: state=I; break;
							
							// identifier, constante ou autre
							default:
								if(Character.isDigit(c)) state=F;
								else if(Character.isLetter(c)) state=G;
								else state=T;
								break;
						}
						break;
						
					case B:
						if(_peeker.peek() != '=') return SCPPQ;
						else
						{
							_peeker.get();
							return SCPPE;
						}
						
					case C:
						if(_peeker.peek() != '=') return SCPGQ;
						else
						{
							_peeker.get();
							return SCPGE;
						}
					
					case E:
						if(_peeker.peek() != '=') return SCEQUAL;
						else
						{
							_peeker.get();
							return SCEGA;
						}
	
					case F:
						if(Character.isDigit(_peeker.peek()))
							lexeme.append(_peeker.get());
						else
							return new Constant(lexeme.toString());
						break;
						
					case G:
						if(Character.isLetterOrDigit(_peeker.peek()))
							lexeme.append(_peeker.get());
						else
						{
							String s = lexeme.toString();
							Token sym = (Token) _reservedWordMap.get(s);
							return (sym != null) ? sym : new Identifier(s);
						}
						break;
						
					case H:
						String s = lexeme.toString();
						return (Token) _specialCharMap.get(s);
						
					case I:
						if(_peeker.peek() == CMT_END_SYMBOL) // fin de commentaire
						{
							_peeker.get();
							lexeme = new StringBuffer(); // on jette le commentaire
							state = A;
						}
						else
							if(_peeker.hasNext())
								_peeker.get();
							else
							{
								// On aurait pas oublier de fermer le commentaire ?
								_peeker.close();
								return EOF;
							}
						break;
						
					case T:
						throw new InvalidProgramException("Caract�re ill�gal: '"+lexeme.toString()+"'");
				}
			}
		}
		catch(IOException e)
		{
			System.out.println("Exception lors de la lecture du fichier source: "+e.getMessage());
			return EOF;
		}
		
		_peeker.close();
			
		return EOF;
	}
	
	/**
	 * @pre: -
	 * 
	 * @post: renvoie la num�ro de la colonne actuelle du pointeur d'entr�e du peeker
	 */
	public int getCurrentCol() { return _peeker.getCurrentCol(); }
	
	/**
	 * @pre: -
	 * 
	 * @post: renvoie la num�ro de la ligne actuelle du pointeur d'entr�e du peeker
	 */
	public int getCurrentRow() { return _peeker.getCurrentRow(); }
	
	private class Peeker
	{
		/** Position actuelle du pointeur d'entr�e du peeker dans le fichier source */
		private int _currentRowNumber;
		private int _currentColNumber;
		
		/** Flux d'entr�e du fichier source */
		private InputStream _input;
		
		/** Prochain caract�re */
		private char _char;
		
		private Peeker() { /* Need some input */ }
		
		public Peeker(InputStream is) throws IOException
		{
			this._input = is;
			_char = (char)is.read();
			_currentRowNumber = 1;
			_currentColNumber = 1;
		}
		
		public char peek()
		{
			return _char;
		}
		
		public char get() throws IOException
		{
			char old = _char;
			
			if(old == '\n')
			{
				_currentRowNumber += 1;
				_currentColNumber = 1;
			}
			else
				_currentColNumber++;
			
			_char = (char)_input.read();
			return old;
		}
		
		public boolean hasNext()
		{
			return ( _char != (char)-1 );
		}
		
		public int getCurrentCol()
		{
			return _currentColNumber;
		}
		
		public int getCurrentRow()
		{
			return _currentRowNumber;
		}
		
		public void close()
		{
			try { _input.close(); }
			catch(IOException e)
			{
				System.out.println("Erreur d'entr�e/sortie lors de la fermeture du fichier source "+_input);
			}
		}
	}
}