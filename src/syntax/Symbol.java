package syntax;

import java.util.*;

public class Symbol
{
	/** Text du symbol */
    private String _text;
    
    /** Ensemble premier */
    private Set _p1;
    
    /** Ensemble suivant */
    private Set _s1;
    
    /** Indice du symbol dans la table de parsing */
    protected int _parseTableIdx;

    public Symbol(String s)
    {
    	_text = s;
    	_p1 = new HashSet();
    	_s1 = new HashSet();
    	_parseTableIdx = -1;
    }

    public String getText() { return _text; }

    public boolean equals(Object o)
    {
    	return o instanceof Symbol &&
    		   _text != null &&
			   _text.equals(((Symbol) o)._text);
    }

    public int hashCode()
    {
        return _text.hashCode();
    }
    
    public Set getP1() { return _p1; }
    
    public Set getS1() { return _s1; }
    
    public void setP1(Set s) { _p1 = s; }
    
    public int getParseTableIdx() { return _parseTableIdx; }
}
