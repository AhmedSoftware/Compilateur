package syntax;

public class Terminal extends Symbol
{
	/** Indice courrant pour la table de parsing pour les terminaux */ 
	private static int _currentIdx = 0;
	
    public Terminal(String sym)
    {
    	super(sym);
    	_parseTableIdx = _currentIdx;
    	_currentIdx += 1;
    }

    public boolean equals(Object o)
    {
	    return (o instanceof Terminal && super.equals(o));
    }
    
    public String toString() { return "'"+getText()+"'"; }
}
