package syntax;

import java.util.*;

public class Rule
{
	/** Partie gauche de la règle */
    NonTerminal _left;
    
    /** Partie droite de la règle contenant un ou plusieurs Symbol */
    LinkedList _right;

    public Rule(NonTerminal left, LinkedList right)
    {
    	this._left = left;
    	this._right = right;
    }
    
    public NonTerminal getLeft()
    {
    	return _left;
    }

    public LinkedList getRight()
    {
    	return _right;
    }

    public String toString()
    {
    	StringBuffer sb = new StringBuffer(_left+" ::=");

    	Iterator it = _right.iterator();
    	while (it.hasNext())
    	{
    		sb.append(" ");
    		sb.append(it.next());
    	}
    	
    	return sb.toString();
    }
    
    public boolean equals(Object o)
    {
    	Rule r;
    	
    	if(o != null && o instanceof Rule)
    		r = (Rule)o;
    	else
    		return false;
    	
    	return r.getLeft().equals(_left) && r.getRight().equals(_right);
    }
    
    public int hashCode()
    {
    	return _left.hashCode() ^ _right.hashCode();
    }
}
