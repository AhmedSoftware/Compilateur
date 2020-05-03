package syntax;

import java.util.*;

import compiler.InvalidProgramException;

public class Grammar
{
	protected NonTerminal _startSymbol;
	protected Terminal _lambda;
	protected LinkedList _rules;
	protected Set _terms;
	protected Set _nonTerms;
	
	protected Grammar() { }
	
	public Grammar(NonTerminal start, Terminal l, Set T, Set N, LinkedList R)
	{
		_startSymbol = start;
		_lambda = l;
		_terms = T;
		_nonTerms = N;
		_rules = R;
		
		String error = checkGrammar();
		if(!error.equals(""))
			throw new InvalidProgramException("grammaire inconsistante: "+error);
		
		AugmentedGrammar eg = new AugmentedGrammar(this);
		eg.getS1Set(_nonTerms);
	}
	
	public static Set concatSet(LinkedList l, Terminal lambda)
	{
		Iterator it = l.iterator();
		boolean containEmptySet = false;
		Set result = new HashSet();
		
		while(it.hasNext())
		{
			Symbol s = (Symbol)it.next();
			if(s.getP1().isEmpty())
			{
				containEmptySet = true;
				break;
			}
			
			if(s.getP1().contains(lambda))
				result.addAll(s.getP1());
			else
			{
				result.remove(lambda);
				result.addAll(s.getP1());
				break;
			}
		}
		
		// si un ensemble est vide, on a pas assez d'element pour faire la concatenation
		// -> retourne un ensemble vide
		if(containEmptySet)
			return new HashSet();
		else
			return result;
	}
	
	protected void computeP1()
	{
		Iterator it;
		boolean hasChanged = true;
		Rule p;
		
		// on ajoute le symbol terminal pour chaque terminal
		it = _terms.iterator();
		while(it.hasNext())
		{
			Terminal t = (Terminal)it.next();
			t.getP1().add(t);
		}
		
		System.out.print("Iteration du point fixe ");
		while(hasChanged)
		{
			System.out.print(".");
			hasChanged = false;
			
			it = _rules.iterator();
			while(it.hasNext())
			{
				p = (Rule)it.next();
				
				//System.out.print("'"+p+"'\t"+p.getLeft().getP1()+"\t");				
				if(p.getLeft().getP1().addAll(concatSet(p.getRight(), _lambda)))
					hasChanged = true;
			
				//System.out.println(p.getLeft().getP1());
			}
		}
		System.out.println();
	}
	
	public String p1ToString()
	{
		StringBuffer sb = new StringBuffer();
		NonTerminal nt;
		Iterator it = _nonTerms.iterator();
		while(it.hasNext())
		{
			nt = (NonTerminal)it.next();
			sb.append("p1(");
			sb.append(nt);
			sb.append(") = ");
			sb.append(nt.getP1().toString());
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
	public String s1ToString()
	{
		StringBuffer sb = new StringBuffer();
		NonTerminal nt;
		Iterator it = _nonTerms.iterator();
		while(it.hasNext())
		{
			nt = (NonTerminal)it.next();
			sb.append("s1(");
			sb.append(nt);
			sb.append(") = ");
			sb.append(nt.getS1().toString());
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		Iterator it;
		
		sb.append("Symbol initial: ");
		sb.append(_startSymbol);
		sb.append("\n");
		
		sb.append("Symbole nul: ");
		sb.append(_lambda == null ? "null" : _lambda.toString());
		sb.append("\n");
		
		sb.append(_terms.size()+" terminaux: \n");
		it = _terms.iterator();
		while(it.hasNext())
		{
			sb.append(it.next());
			sb.append("\n");
		}
		sb.append("\n");
		
		sb.append(_nonTerms.size()+" non-terminaux:\n");
		it = _nonTerms.iterator();
		while(it.hasNext())
		{
			sb.append(it.next());
			sb.append("\n");
		}
		sb.append("\n");
		
		sb.append("Regle de production:\n");
		it = _rules.iterator();
		while(it.hasNext())
		{
			sb.append(it.next());
			sb.append("\n");
		}
		
		return sb.toString();
	}

	
	public Terminal getLambda()
	{
		return _lambda;
	}
	
	public NonTerminal getStartSymbol()
	{
		return _startSymbol;
	}
	
	public LinkedList getRules()
	{
		return new LinkedList(_rules);
	}
	
	public Set getTerminal()
	{
		return new HashSet(_terms);
	}
	
	public Set getNonTerminal()
	{
		return new HashSet(_nonTerms);
	}
	
	public int getNumberOfTerminal()
	{
		return _terms.size();
	}
	
	public int getNumberOfNonTerminal()
	{
		return _nonTerms.size();
	}
	
	/**
	 * Vérifie que
	 *  - la grammaire ait un symbole initial,
	 *  - il n'y ait pas de terminaux non-utilisés,
	 *  - tous les non-terminaux soient joignables à partir du symbole de base.
	 */
	private String checkGrammar()
	{
		if (_startSymbol == null)
		    return "Pas de symbole initial";

		// Clé: non-terminal
		// Valeur: production dont le membre de gauche est le non-terminal
		HashMap prodMap = new HashMap();

		Rule prod;
		HashSet prodSet;
		
		Iterator it = _rules.iterator();
		while (it.hasNext())
		{
			prod = (Rule) it.next();

		    prodSet = (HashSet) prodMap.get(prod.getLeft());
		    if (prodSet == null)
		    {
		    	prodSet = new HashSet();
		    	// le terminal n'était pas encore une clé dans prodMap
				prodMap.put(prod.getLeft(), prodSet);
		    }

		    prodSet.add(prod);
		}
		
		// Vérifie que tous les non-terminaux sont joignables
		NonTerminal nt;
		HashSet leftNT;
		Set reachable = new HashSet();
		LinkedList worklist = new LinkedList();
		
		worklist.add(_startSymbol);
		while (!worklist.isEmpty())
		{
		    nt = (NonTerminal) worklist.removeFirst();
		    reachable.add(nt);

		    prodSet = (HashSet) prodMap.get(nt);
		    if (prodSet == null || prodSet.isEmpty())
		    	return "aucune production pour le non terminal "+nt;

		    Iterator pit = prodSet.iterator();
		    while (pit.hasNext())
		    {
				prod = (Rule) pit.next();
	
				leftNT = collectNonterms(prod.getRight());
				
				leftNT.removeAll(reachable);
				worklist.addAll(leftNT);
		    }
		}

		// Print unreachable nonterminals.
		Set unreachable = new HashSet(_nonTerms);
		unreachable.removeAll(reachable);
		
		if(!unreachable.isEmpty())
		{
		    StringBuffer sb = new StringBuffer("non terminaux injoignables: ");
			it = unreachable.iterator();
		    while (it.hasNext())
				sb.append(((NonTerminal)it.next()).toString());
		    
		    return sb.toString();
		}

	    return "";
	}

	HashSet collectNonterms(List right)
	{
		HashSet ntSet = new HashSet();
		Symbol s;
		
		Iterator it = right.iterator();
		while (it.hasNext())
		{
		    s = (Symbol) it.next();
		    if (s instanceof NonTerminal)
		    	ntSet.add(s);
		}
		
		return ntSet;
	}
}