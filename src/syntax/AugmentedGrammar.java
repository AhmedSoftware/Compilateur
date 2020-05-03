package syntax;

import java.util.*;

public class AugmentedGrammar extends Grammar
{
	/** Clé: symbole original, valeur: symbole étendu */
	private Map _newSymbol;
	
	public AugmentedGrammar(Grammar g)
	{
		_newSymbol = new HashMap();
		
		_rules = g.getRules();
		_nonTerms = g.getNonTerminal();
		_terms = g.getTerminal();
		
		_startSymbol = new NonTerminal(g.getStartSymbol().getText()+"'");
		_nonTerms.add(_startSymbol);
		_lambda = (g.getLambda() != null) ? g.getLambda() : new Terminal("lambda"); 
		_terms.add(_lambda);
		
		createExtendedGrammar(g);
		
		// calcule l'ensemble premier pour la grammaire de base ET
		// la grammaire étendue !
		computeP1();
	}
	
	private void createExtendedGrammar(Grammar g)
	{
		// Clé: nom du symbol, valeur: l'object symbol -> permet d'éviter les doublons
		HashMap m = new HashMap();
		LinkedList right;
		
		String xPrimeName;
		String yPrimeName;
		NonTerminal xPrime;
		NonTerminal yPrime;
		
		// S' -> lambda
		right = new LinkedList();
		right.add(_lambda);
		_newSymbol.put(g.getStartSymbol(), _startSymbol);
		_rules.add(new Rule(_startSymbol, right));
		m.put(_startSymbol.getText(), _startSymbol);
		
		Iterator it = g.getRules().iterator();
		while(it.hasNext())
		{
			Symbol s = null;
			Rule p = (Rule)it.next();
			right = new LinkedList(p.getRight());
			NonTerminal left = p.getLeft();
			
			while(!right.isEmpty())
			{
				s = (Symbol)right.removeFirst();
				if(s instanceof NonTerminal)
				{
					// Y  -> alpha X beta
					// X' -> beta Y'
					// avec X appertenant à l'ensemble des terminaux
					NonTerminal x = (NonTerminal)s;
					
					// on ne rajoute pas les règles du style A' -> A'
					if(x.toString().equals(left.toString()) && right.isEmpty()) continue;
					
					xPrimeName = x.getText()+"'";
					yPrimeName = left.getText()+"'";
					
					xPrime = (NonTerminal)m.get(xPrimeName);
					if(xPrime == null)
					{
						xPrime = new NonTerminal(xPrimeName);
						m.put(xPrimeName, xPrime);
						_nonTerms.add(xPrime);
					}

					yPrime = (NonTerminal)m.get(yPrimeName);
					if(yPrime == null)
					{
						yPrime = new NonTerminal(yPrimeName);
						m.put(yPrimeName, yPrime);
						_nonTerms.add(yPrime);
					}

					LinkedList newRight = new LinkedList(right);
					newRight.add(yPrime);
					_rules.add(new Rule(xPrime, newRight));
					_newSymbol.put(x, xPrime);
				}
			}
		}
	}
	
	public void getS1Set(Set s)
	{
		Iterator it = s.iterator();
		while(it.hasNext())
		{
			NonTerminal nt = (NonTerminal)it.next();
			NonTerminal ntPrime = (NonTerminal)_newSymbol.get(nt);
			
			nt.getS1().addAll(ntPrime.getP1());
		}
	}
}
