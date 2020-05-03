package syntax;

public class NonTerminal extends Symbol
{
	/** Indice courant pour la table de parsing pour les non-terminaux */ 
	private static int _currentIdx = 0;
	
    public NonTerminal(String sym)
    {
    	super(sym);
    	_parseTableIdx = _currentIdx;
    	_currentIdx += 1;
    }

    public boolean equals(Object o)
    {
    	return (o instanceof NonTerminal && super.equals(o));
    }
    
    public String toString()
    {
    	return "<"+getText()+">";
    }
}
