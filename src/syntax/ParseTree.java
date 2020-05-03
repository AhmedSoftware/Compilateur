package syntax;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ParseTree
{
	/** Liste des descendants du noeud courant: enfant = ensemble de ParseTree */
	private LinkedList _child;
	
	/** Symbole syntaxique stocké dans ce noeud*/
	private Symbol _element;
	
	/** Valeur du terminal si element est du type Terminal */
	private String _value;
	
	public ParseTree(Symbol ls)
	{
		_child = new LinkedList();
		_element = ls;
		_value = "";
	}
	
	public void addChild(ParseTree pt)
	{
		_child.addFirst(pt);
	}
	
	public List getChildren()
	{
		return _child;
	}
	
	public ParseTree getChild(int n)
	{
		return (ParseTree)_child.get(n);
	}
	
	public int getNbChildren()
	{
		return _child.size();
	}
	
	public Symbol getSymbol()
	{
		return _element;
	}
	
	public void setSymbol(Symbol s)
	{
		_element = s;
	}
	
	public void setValue(String v)
	{
		_value = v;
	}
	
	public String getValue()
	{
		return _value;
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		Iterator it;
		
		sb.append(_element.toString());
		if(!_value.equals(""))
		{
			sb.append("#");
			sb.append(_value);
		}
		
		if(!_child.isEmpty())
		{
			sb.append("(");
			sb.append(_child);
			sb.append(")\n");
		}
		
		return sb.toString();
	}
}