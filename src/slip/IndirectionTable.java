package slip;

import java.util.Iterator;
import java.util.List;
import java.util.HashMap;

import compiler.InvalidProgramException;

public class IndirectionTable
{
	/** Cl�: Nom de m�thode, Valeur: Indice dans le tableau des adresses */
	private HashMap nameToIdx;
	
	/** Cl�: indice, Valeur: nom de la m�thode */
	private String[] indexToName;
	
	/** Cl�: Nom de la m�thode, Valeur: adresse de la table d'indirection */
	private HashMap addressTable;
	
	/** X = indice correspondant au nom de la variable, Y = Niveau de la m�thode, Valeur: Adresse de la meth */
	private int[][] table;
	
	/** Adresse de la premi�re case m�moire libre apr�s la table d'indirection */
	private int endAddress;
	
	public IndirectionTable(List met)
	{
		MethodDeclaration m;
		nameToIdx = new HashMap();
		Integer i;
		
		// Recherche de toutes les m�thodes dynamiques
		HashMap dynamicMeth = new HashMap();
		Iterator it = met.iterator();
		while (it.hasNext())
		{
			m = (MethodDeclaration) it.next();
			if (m.getMethodLevel() > MethodDeclaration.STATIC)
			{
				i = (Integer)dynamicMeth.get(m.getMethodName());
				
				if(i == null || i.intValue() < m.getMethodLevel())
					dynamicMeth.put(m.getMethodName(), new Integer(m.getMethodLevel()));
			}
		}
		
		// Cr�ation de la table pour chaque m�thode dynamique
		String name;
		int maxLevel;
		int currentIdx = 0;
		table = new int[dynamicMeth.size()][];
		indexToName = new String[dynamicMeth.size()];
		
		it = dynamicMeth.keySet().iterator();
		while(it.hasNext())
		{
			// nom de la m�thode
			name = (String) it.next();
			
			// Ajout du couple (Nom de m�thode, Indice dans table)
			nameToIdx.put(name, new Integer(currentIdx));
			
			// Ajout du couple (Indice dans la table, Nom de m�thode)
			indexToName[currentIdx] = name;
			
			// Recuperation du niveau max de la m�thode
			maxLevel = ((Integer)dynamicMeth.get(name)).intValue();
			
			// Cr�ation de la table d'adresse pour cette m�thode
			table[currentIdx] = new int[maxLevel+1];
			
			// Au suivant ...
			currentIdx += 1;
		}

		// Calculons l'adresse de d�but de chaque table
		addressTable = new HashMap();
		int currentAddr = Constant.STARTADDR;
		int idx;
		
		it = dynamicMeth.keySet().iterator();
		while(it.hasNext())
		{
			name = (String)it.next();
			addressTable.put(name, new Integer(currentAddr));
			
			idx = ((Integer)nameToIdx.get(name)).intValue();
			currentAddr += table[idx].length*4;
		}
		
		endAddress = currentAddr;
	}
	
	public int getEndAddress() { return endAddress; }
	
	public void setAdress(String name, int level, int addr)
	{
		Integer i = (Integer) nameToIdx.get(name);
			
		table[i.intValue()][level] = addr;
	}
	
	public int getTableAddress(String name)
	{
		Integer i = (Integer) addressTable.get(name);
		
		if(i == null)
			throw new InvalidProgramException("M�thode dynamique '"+name+"' ind�finie");
		
		return i.intValue();
	}
	
	public void getInstruction(List pgmInstructions)
	{	
		// Remplissage des "trous" des tables
		int currentVal;

		for(int i=0; i < table.length; i++)
		{
			currentVal = table[i][0];
			for(int j=1; j < table[i].length; j++)
			{
				if(table[i][j] == 0)
					table[i][j] = currentVal;
				else
					currentVal = table[i][j];
			}
		}
		
		// G�neration de tous les lit�raux pour chaque adresses
		Literal lit;
		int currentAddr = Constant.STARTADDR;
		
		for(int i=0; i < table.length; i++)
		{
			pgmInstructions.add(new Comment("Table de la m�thode: "+indexToName[i]));
			for(int j=0; j < table[i].length; j++)
			{
				lit = new Literal(table[i][j]);
				lit.setAddress(currentAddr);
				pgmInstructions.add(lit);
				
				currentAddr += 4;
			}
		}
	}
	
	public int getSuperMethodAddr(String name, int level)
	{
		int addr;
		int idx;
		Integer i = (Integer) nameToIdx.get(name);
		
		if(level == 0)
			throw new InvalidProgramException("Pas de m�thode super pour une m�thode de niveau 0");
		
		if(i == null)
			throw new InvalidProgramException("M�thode '"+name+"' ind�finie");
		idx = i.intValue();
		
		return table[idx][level-1];
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(addressTable.toString());
		sb.append("\n");
		sb.append(table.toString());
		return sb.toString();
	}
}