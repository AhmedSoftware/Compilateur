package translator;

import java.util.*;

import compiler.InvalidProgramException;
import syntax.ParseTree;
import slip.*;

public class SLIPGenerator
{
	/** Racine de l'arbre syntaxique généré par LL1Parser */
	private ParseTree _root;
	
	/** Texte représentant une production vide */
	private String _lambda;
	
	/** Premier label libre */
	private int _first;
	
	/** Numéro du premier numéro de variable libre */
	private int _disp;
	
	/** Numéro de variable le plus élevé */
	private int _maxDisp;
	
	/** Table de correspondance entre les noms de variable et leur numéro */
	private HashMap _nameToNum;
	
	/**
	 * @pre:
	 *  - Le noeud racine de l'arbre est le symbole initial.
	 *  - Pour tout noeud interne de l'arbre contenant le symbole X, les
	 *    symboles contenus dans les noeuds descendants de celui-ci sont
	 *    les symboles d'une partie droite de règle de la grammaire de la
	 *    forme X -> Y1 ... Yn avec Y1, ..., Yn appartenant aux terminaux
	 *    et aux non-terminaux.
	 *  - lambda est la chaine de caractères représentant la production
	 *    vide dans la grammaire.
	 * 
	 * @post:
	 *  - Le générateur de SLIP initialisé.   
	 */
	public SLIPGenerator(ParseTree root, String lambda)
	{
		_root = root;
		_lambda = lambda;
	}
	
	/**
	 * @pre:
	 *  - Le générateur doit avoir été initialisé
	 * 
	 * @post:
	 *  - Retourne la racine de l'arbre en syntaxe abstraite correspond
	 *    à l'arbre syntaxique fourni dans le constructeur.
	 */
	public Program getProgram()
	{
		if(!_root.getSymbol().getText().equals("P"))
		{
			System.out.println("La racine de l'arbre syntaxique doit être <P> !");
			return null;
		}
		
		Program pgm = new Program();
		processProgram(pgm, _root);
		
		return pgm;
	}
	
	private Program processProgram(Program pgm, ParseTree n)
	{
		ParseTree t;
		Iterator it = n.getChildren().iterator();
		while(it.hasNext())
		{
			t = (ParseTree) it.next();
			
			if(t.getSymbol().getText().equals("MET"))
				processMethod(pgm, t);
			else if(t.getSymbol().getText().equals("P-T"))
				processProgram(pgm, t);
			else if(t.getSymbol().getText().equals(_lambda))
				return null;
		}
		
		return pgm;
	}
	
	private MethodDeclaration processMethod(Program pgm, ParseTree n)
	{
		_nameToNum = new HashMap();
		
		// nom de la méthode
		ParseTree name = n.getChild(0).getChild(0);
		ParseTree nameTail = n.getChild(0).getChild(1);
		
		MethodDeclaration md = new MethodDeclaration(name.getValue());
		pgm.addMethod(md);
		
		ParseTree levelNode = nameTail.getChild(0);
		if(!levelNode.getSymbol().getText().equals(_lambda))
			md.setMethodLevel(Integer.parseInt(nameTail.getChild(1).getValue()));
		
		// Liste de paramètre
		int nbVar = 0;
		ParseTree nonEmptyParameterListNode = n.getChild(2).getChild(0);
		if(!nonEmptyParameterListNode.getSymbol().getText().equals(_lambda))
		{
			VarIdentifier ve = new VarIdentifier(nonEmptyParameterListNode.getChild(0).getValue(), 1); 
			md.addParam(ve);
			_nameToNum.put(ve.getVariableName(), new Integer(1));
			nbVar = processParameterList(md, nonEmptyParameterListNode.getChild(1), 2);
		}
		
		// Ajout de la valeur de retour de la méthode
		md.setReturnValue(new SimpleDescriptor(name.getValue()+"Result", 0));
		
		// Création de la table
		nbVar = createVarTable(n, nbVar+1);
		
		// Corps de la boucle
		ParseTree body = (ParseTree)n.getChild(4);
		md.setStartLabel(1);
		_first = 1;
		
		_disp = nbVar;
		_maxDisp = _disp;
		processCommand(md, body);
		md.setMethodEndLabel(_first);
		md.setNumberOfVar(_maxDisp);
		
		return md;
	}
	
	private int processParameterList(MethodDeclaration md, ParseTree n, int varNumber)
	{
		ParseTree pt = n.getChild(0);
		
		if(!pt.getSymbol().getText().equals(_lambda))
		{
			VarIdentifier ve = new VarIdentifier(n.getChild(1).getValue(), varNumber);
			md.addParam(ve);
			_nameToNum.put(ve.getVariableName(), new Integer(varNumber));
			return processParameterList(md, n.getChild(2), varNumber+1);
		}
		
		return varNumber-1;
	}
	
	private void processCommandList(MethodDeclaration md, ParseTree n)
	{		
		if(!n.getChild(0).getSymbol().getText().equals(_lambda))
		{
			processCommand(md, n.getChild(0));
			processCommandList(md, n.getChild(1));
		}
	}
	
	private void processCommand(MethodDeclaration md, ParseTree n)
	{
		ParseTree cmdNode = n.getChild(0);
		String commandType = cmdNode.getSymbol().getText();
		
		if(commandType.equals("ASS"))
			processAssignment(md, cmdNode);
		else if(commandType.equals("CONDCMD"))
			processConditionalCommand(md, cmdNode);
		else if(commandType.equals("LOOPCMD"))
			processLoopCommand(md, cmdNode);
		else if(commandType.equals("INCMD"))
			processInputCommand(md, cmdNode);
		else if(commandType.equals("OUTCMD"))
			processOutputCommand(md, cmdNode);
		else if(commandType.equals("RTNCMD"))
			processReturnCommand(md, cmdNode);
		else if(commandType.equals("CMDSEQ"))
			// <CMDSEQ> --> "{" <CMDLIST> "}"
			processCommandList(md, cmdNode.getChild(1));
	}
	
	private void processAssignment(MethodDeclaration md, ParseTree n)
	{
		StringBuffer sb = new StringBuffer();
		AbstractDescriptor ad = processLeftExpression(n.getChild(0), sb);
		ad.setVarNumber(getVarNumber(sb.toString()));
		
		processRightExpression(md, n.getChild(2), ad);
	}
	
	private void processConditionalCommand(MethodDeclaration md, ParseTree n)
	{
		AbstractCommand lastThenStmt;
		
		// <CONDCMD> --> "if" <COND> <CMD> <CONDCMD-T>
		ParseTree cond = n.getChild(1);
		ParseTree thenCmd = n.getChild(2);
		ParseTree elseCmd = n.getChild(3);
		
		IfStatement ifStmt = processCondition(md, cond);
		
		// Le label du else de ifStmt doit encore être mis à jour !
		processCommand(md, thenCmd);
		lastThenStmt = md.getLastStatement();
		ifStmt.setElseLabel(_first);
		if(!elseCmd.getChild(0).getSymbol().getText().equals(_lambda))
		{	
			processCommand(md, elseCmd.getChild(1));
			lastThenStmt.setNextLabel(_first);
		}
	}
	
	private void processLoopCommand(MethodDeclaration md, ParseTree n)
	{
		// <LOOPCMD> --> "while" <COND> <CMD>
		ParseTree cond = n.getChild(1);
		ParseTree cmd = n.getChild(2);
		
		int ifStartLabel = _first;
		IfStatement ifStmt = processCondition(md, cond);

		processCommand(md, cmd);
		
		// Retourne à l'évaluation de la condition du if
		AbstractCommand ac = md.getLastStatement();
		ac.setNextLabel(ifStartLabel);
		
		// Retourne après la dernière instruction si la condition est fausse
		ifStmt.setElseLabel(_first);
	}
	
	private IfStatement processCondition(MethodDeclaration md, ParseTree cond)
	{
		// <COND> --> "(" <REL> ")"
		// <REL> --> <EXP> <CO> <EXP>
		ParseTree relation = cond.getChild(1);
		
		AbstractDescriptor d1 = new SimpleDescriptor("", _disp);
		processRightExpression(md, relation.getChild(0), d1);
		VarIdentifier exp1 = new VarIdentifier("", _disp);
		
		incrementDisp();
		AbstractDescriptor d2 = new SimpleDescriptor("", _disp);
		processRightExpression(md, relation.getChild(2), d2);
		VarIdentifier exp2 = new VarIdentifier("", _disp);
		decrementDisp();
		
		String oprel = relation.getChild(1).getChild(0).getSymbol().getText();
		IfStatement ifStmt = null;
		
		if(oprel.equals("==") || oprel.equals("<"))
		{
			ifStmt = new IfStatement(exp1, oprel.equals("<") ? IfStatement.LESS : IfStatement.EQUAL, exp2);
			ifStmt.setLabel(_first);
			ifStmt.setThenLabel(_first+1);
			md.addStatement(ifStmt);
			
			_first += 1;
		}
		else if(oprel.equals("!="))
		{
			// exp1 != exp2 -> identique à (exp1 < exp2 | exp2 < exp1)
			IfStatement if2 = new IfStatement(new VarIdentifier("", _disp), IfStatement.LESS, new VarIdentifier("", _disp+1));
			if2.setLabel(_first);
			if2.setThenLabel(_first+2);
			if2.setElseLabel(_first+1);
			md.addStatement(if2);
			
			ifStmt = new IfStatement(exp2, IfStatement.LESS, exp1);
			ifStmt.setLabel(_first+1);
			ifStmt.setThenLabel(_first+2);
			md.addStatement(ifStmt);
			
			_first += 2;
		}
		else if(oprel.equals(">"))
		{
			// exp1 > exp2 identique à exp2 < exp1
			ifStmt = new IfStatement(exp2, IfStatement.LESS, exp1);
			ifStmt.setLabel(_first);
			ifStmt.setNextLabel(_first+1);
			ifStmt.setThenLabel(_first+1);
			md.addStatement(ifStmt);
			_first += 1;
		}
		else if(oprel.equals("<="))
		{
			// exp1 <= exp2 identique à (exp1 < exp2 | exp1 == exp2)
			IfStatement if2 = new IfStatement(new VarIdentifier("", _disp), IfStatement.LESS, new VarIdentifier("", _disp+1));
			if2.setLabel(_first);
			if2.setThenLabel(_first+2);
			if2.setElseLabel(_first+1);
			md.addStatement(if2);
			
			ifStmt = new IfStatement(exp1, IfStatement.EQUAL, exp2);
			ifStmt.setLabel(_first+1);
			ifStmt.setThenLabel(_first+2);
			md.addStatement(ifStmt);
			
			_first += 2;
		}
		else if(oprel.equals(">="))
		{
			// exp1 >= exp2 identifique à (exp1 > exp2 | exp1 == exp2)
			IfStatement if2 = new IfStatement(new VarIdentifier("", _disp), IfStatement.LESS, new VarIdentifier("", _disp+1));
			if2.setLabel(_first);
			if2.setThenLabel(_first+1);
			if2.setElseLabel(_first+2);
			md.addStatement(if2);
			
			ifStmt = new IfStatement(exp1, IfStatement.EQUAL, exp2);
			ifStmt.setLabel(_first+1);
			ifStmt.setThenLabel(_first+2);
			md.addStatement(ifStmt);
			
			_first += 2;
		}
		
		return ifStmt;
	}
	
	private void processInputCommand(MethodDeclaration md, ParseTree n)
	{
		// <INCMD> --> "read" "(" <LEXPLST> ")" ";"
		ParseTree leftExprList = n.getChild(2);
		ParseTree firstParam = leftExprList.getChild(0);
		ParseTree tail = leftExprList.getChild(1);
		
		StringBuffer name = new StringBuffer();
		
		AbstractDescriptor ad = processLeftExpression(firstParam, name);
		ad.setVarNumber(getVarNumber(name.toString()));
		
		InputCommand ic = new InputCommand(ad);
		ic.setLabel(_first);
		ic.setNextLabel(_first+1);
		md.addStatement(ic);
		_first += 1;
		
		// La suite des arguments: tail.getChild(0) = "," OU "lambda"
		while(!tail.getChild(0).getSymbol().getText().equals(_lambda))
		{
			name.delete(0, name.length());
			ad = processLeftExpression(tail.getChild(1), name);
			ad.setVarNumber(getVarNumber(name.toString()));
			ic = new InputCommand(ad);
			ic.setLabel(_first);
			ic.setNextLabel(_first+1);
			md.addStatement(ic);
			tail = tail.getChild(2);
			_first += 1;
		}
	}
	
	private void processOutputCommand(MethodDeclaration md, ParseTree n)
	{
		ParseTree rightExprList = n.getChild(2);
		ParseTree firstParam = rightExprList.getChild(0);
		ParseTree tail = rightExprList.getChild(1);
		
		// on ne peut "écrire" que des identifier (variable, this, object.field)
		// On est donc obliger de créer un variable intermédiaire
		AbstractDescriptor ad = new SimpleDescriptor("", _disp);
		VarIdentifier vi = new VarIdentifier("", _disp);
		processRightExpression(md, firstParam, ad);
		
		OutputCommand oc = new OutputCommand(vi);
		oc.setLabel(_first);
		oc.setNextLabel(_first+1);
		md.addStatement(oc);
		_first += 1;
		
		// La suite des arguments: tail.getChild(0) = "," OU "lambda"
		while(!tail.getChild(0).getSymbol().getText().equals(_lambda))
		{ 
			processRightExpression(md, tail.getChild(1), ad);
			oc = new OutputCommand(vi);
			oc.setLabel(_first);
			oc.setNextLabel(_first+1);
			md.addStatement(oc);
			tail = tail.getChild(2);
			_first += 1;
		}
	}
	
	private void processReturnCommand(MethodDeclaration md, ParseTree n)
	{
		SimpleDescriptor sd = new SimpleDescriptor(md.getReturnValue().getVariableName(), 0);
		processRightExpression(md, n.getChild(2), sd);
	}
	
	private AbstractDescriptor processLeftExpression(ParseTree n, StringBuffer name)
	{
		AbstractDescriptor ad = null;
		name.append(n.getChild(0).getChild(0).getValue());
		if(n.getChild(1).getChild(0).getSymbol().getText().equals(_lambda))
			ad = new SimpleDescriptor(name.toString() , 0); 
		else
			ad = new ObjectDescriptor(name.toString(), 0, Integer.parseInt(n.getChild(1).getChild(1).getValue()));
		
		return ad;
	}

	private void processRightExpression(MethodDeclaration md, ParseTree n, AbstractDescriptor ad)
	{
		int oldDisp = _disp;
		
		Stack s = new Stack();
		treeTraversal(md, n, s);
		
		if(s.size() == 1)
		{
			SimpleAssignment sa = new SimpleAssignment(ad, (AbstractExpression)s.pop());
			sa.setLabel(_first);
			sa.setNextLabel(_first+1);
			md.addStatement(sa);
			
			_first += 1;
		}
		
		_disp = oldDisp;
	}
	
	private void treeTraversal(MethodDeclaration md, ParseTree n, Stack s)
	{
		int i = 0;
		int size = n.getNbChildren(); 
		boolean isOp = false;
		boolean isNeg = false;
		
		if(size > 0)
		{
			if((n.getChild(0).getSymbol().getText().equals("MO") ||
				n.getChild(0).getSymbol().getText().equals("AO")))
			{
				// on fait un parcours postfixe de l'arbre mais l'arbre produit par l'analyseur syntaxique
				// LL(1) n'est pas "conventiel" pour une évaluation d'expression et pour un parcours de ce type.
				// L'opérateur n'est pas le parent des 2 opérandes mais il est le premier enfant. On a des noeuds
				// du style T-T(<*>, <Facteur1>, <Facteur2>)
				//
				// S'il s'agit d'un opérateur, pour simuler un parcours postfixe, on effectuera l'opération
				// voulue après avoir visité les 2 opérandes.
				isOp = true;
				i += 1;
			}
			else if(n.getChild(0).getSymbol().getText().equals("-"))
			{
				isNeg = true;
				i += 1;
			}
		}
		
		// S'il s'agit d'un facteur, on ne continue pas le parcours postfixe de l'arbre pour le facteur,
		// c'est processFactor qui s'en chargera.
		if(n.getSymbol().getText().equals("F"))
			s.push(processFactor(md, n));
		else
		{
			while(i < size)
			{
				treeTraversal(md, n.getChild(i++), s);
				if(isNeg && !s.isEmpty())
				{
					ArithmeticExpression neg = new ArithmeticExpression(new IntExpression(0), ArithmeticExpression.MIN, (AbstractSimpleExpression)s.pop()); 
					SimpleAssignment sa = new SimpleAssignment(new SimpleDescriptor("", _disp), neg);
					sa.setLabel(_first);
					sa.setNextLabel(_first+1);
					md.addStatement(sa);
					
					s.push(new VarIdentifier("", _disp));
					
					_first += 1;
					incrementDisp();
					
					isNeg = false;
				}
			}
		}
		
		if(isOp)
		{
			int op = -1;
			
			// n.getChild(0) -> AO ou MO
			// AO.getChild(0) -> opérateur additif
			// MO.getChild(0) -> opérateur multiplicatif
			String sym = n.getChild(0).getChild(0).getSymbol().getText();
			if(sym.equals("+")) 		op = ArithmeticExpression.ADD;
			else if(sym.equals("-"))	op = ArithmeticExpression.MIN;
			else if(sym.equals("*"))	op = ArithmeticExpression.MUL;
			else if(sym.equals("/"))	op = ArithmeticExpression.DIV;
			else if(sym.equals("%"))	op = ArithmeticExpression.MOD;
		
			AbstractSimpleExpression x2 = (AbstractSimpleExpression) s.pop();
			AbstractSimpleExpression x1 = (AbstractSimpleExpression) s.pop();
			ArithmeticExpression me = new ArithmeticExpression(x1, op, x2);
			
			// Ajout de l'instruction dans la méthode
			SimpleAssignment sa = new SimpleAssignment(new SimpleDescriptor("", _disp), me);
			sa.setLabel(_first);
			sa.setNextLabel(_first+1);
			md.addStatement(sa);
			
			// On replace le résultat dans la pile
			s.push(new VarIdentifier("", _disp));
			
			_first += 1;
			incrementDisp();
		}
	}
	
	private AbstractSimpleExpression processFactor(MethodDeclaration md, ParseTree n)
	{
		AbstractSimpleExpression ase = null;
		String type = n.getChild(0).getSymbol().getText();

		if(type.equals("identifier"))
		{
			// <F> --> "identifier" <IDENT-T>
			String name = n.getChild(0).getValue();
			ParseTree identTail = n.getChild(1);
			String tailType = identTail.getChild(0).getSymbol().getText();
			
			if(tailType.equals(_lambda))
				// <IDENT-T> --> "lambda"
				ase = new VarIdentifier(name, getVarNumber(name));
			else if(tailType.equals("."))
			{
				// <IDENT-T> --> "." <DF>
				String fieldOrMethod = identTail.getChild(1).getChild(0).getSymbol().getText();
				if(fieldOrMethod.equals("cst"))
				{
					// <DF> --> "cst"
					int fieldNb = 0;
					
					try
					{
						fieldNb = Integer.parseInt(identTail.getChild(1).getChild(0).getValue());
					}
					catch(NumberFormatException e)
					{
						throw new InvalidProgramException("Identifiant de champ illégal: "+
								identTail.getChild(1).getChild(0).getValue());
					}
					
					ase = new ObjectIdentifier(name, getVarNumber(name), fieldNb);
				}
				else if(fieldOrMethod.equals("identifier"))
				{
					// <DF> --> "identifier" "(" <APL> ")"
					String methodName = identTail.getChild(1).getChild(0).getValue();
					
					VarIdentifier vi = new VarIdentifier(name, getVarNumber(name));
					LinkedList pl = new LinkedList();
					processActualParamList(md, identTail.getChild(1).getChild(2).getChild(0), pl);
					
					SimpleDescriptor sd = new SimpleDescriptor("", _disp);
					ase = new VarIdentifier("", _disp);
					
					ObjectCall oc = new ObjectCall(sd, vi, methodName, pl);
					oc.setLabel(_first);
					oc.setNextLabel(_first+1);
					md.addStatement(oc);
					
					incrementDisp();
					_first += 1;
				}
			}
			else if(tailType.equals("("))
			{
				// <IDENT-T> --> "(" <APL> ")"
				List pl = new LinkedList();
				processActualParamList(md, identTail.getChild(1).getChild(0), pl);
				
				SimpleDescriptor sd = new SimpleDescriptor("", _disp);
				ase = new VarIdentifier("", _disp);

				SimpleCall sc = new SimpleCall((SimpleDescriptor)sd, name, pl);
				sc.setLabel(_first);
				sc.setNextLabel(_first+1);
				md.addStatement(sc);
				
				incrementDisp();
				_first += 1;
			}
		}
		else if(type.equals("cst"))
		{
			int value = 0;
			
			try
			{
				value = Integer.parseInt(n.getChild(0).getValue());
			}
			catch(NumberFormatException e)
			{
				throw new InvalidProgramException("Valeur de constante illégale ... doit être un entier compris entre "+
							Integer.MIN_VALUE+" et "+Integer.MAX_VALUE);
			}
			ase = new IntExpression(value);
		}
		else if(type.equals("new"))
		{
			int level = Integer.parseInt(n.getChild(2).getValue());
			
			SimpleDescriptor sd = new SimpleDescriptor("", _disp);
			ase = new VarIdentifier("", _disp);
			
			NewAssignment na = new NewAssignment(sd, level);
			na.setLabel(_first);
			na.setNextLabel(_first+1);
			md.addStatement(na);
			
			_first += 1;
			incrementDisp();
		}
		else if(type.equals("this"))
			ase = new ThisIdentifier();
		else if(type.equals("null"))
			ase = new NullExpression();
		else if(type.equals("("))
		{
			AbstractDescriptor d = new SimpleDescriptor("", _disp);
			processRightExpression(md, n.getChild(1), d);
			
			return new VarIdentifier("", _disp);
		}
		else if(type.equals("super"))
		{
			// "super" "." "identifier" "(" <APL> ")"
			String methodName = n.getChild(2).getValue();
			
			if(md.getMethodLevel() == MethodDeclaration.STATIC)
				throw new InvalidProgramException("Super ne peut être appelée dans une méthode statique !");
			
			List pl = new LinkedList();
			processActualParamList(md, n.getChild(4).getChild(0), pl);
			
			SimpleDescriptor sd = new SimpleDescriptor("", _disp);
			ase = new VarIdentifier("", _disp);
			
			SuperCall sc = new SuperCall(sd, methodName, pl, md.getMethodLevel());
			sc.setLabel(_first);
			sc.setNextLabel(_first+1);
			md.addStatement(sc);
			
			_first += 1;
			incrementDisp();
		}
		
		return ase;
	}
	
	private void processActualParamList(MethodDeclaration md, ParseTree n, List pl)
	{
		if(n.getSymbol().getText().equals(_lambda))
			return;
		
		ParseTree expr = n.getChild(0);
		ParseTree tail = n.getChild(1);
		
		AbstractDescriptor ad = new SimpleDescriptor("", _disp);
		processRightExpression(md, expr, ad);
		pl.add(new VarIdentifier("", _disp));
		incrementDisp();
		
		while(!tail.getChild(0).getSymbol().getText().equals(_lambda))
		{
			ad = new SimpleDescriptor("", _disp);
			processRightExpression(md, tail.getChild(1), ad);
			pl.add(new VarIdentifier("", _disp));
			incrementDisp();
			tail = tail.getChild(2);
		}
	}
	
	private int createVarTable(ParseTree root, int n)
	{
		int nextN = n;
		
		// Faire un parcours préfixe de l'arbre en cherchant des noeuds dont le symbol est VAR
		if(root.getSymbol().getText().equals("VAR"))
		{
			String varName = root.getChild(0).getValue(); 
			if(!_nameToNum.containsKey(varName))
			{
				_nameToNum.put(varName, new Integer(n));
				nextN = n+1;
			}
		}
		
		Iterator it = root.getChildren().iterator();
		while(it.hasNext())
			nextN = createVarTable((ParseTree)it.next(), nextN);
		
		return nextN;
	}
	
	private int incrementDisp()
	{
		_disp += 1;
		
		if(_disp > _maxDisp)
			_maxDisp = _disp;
		
		return _disp;
	}
	
	private int decrementDisp()
	{
		_disp -= 1;
		return _disp;
	}
	
	private int getVarNumber(String name)
	{
		Integer i = (Integer)_nameToNum.get(name);
		
		if(i == null)
			throw new InvalidProgramException("Variable '"+name+"' non initialisée");
		
		return i.intValue();
	}
}