package index;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ToolKit
{
	public static HashMap<String, Integer> mergeMap( HashMap<String,Integer> m1, HashMap<String,Integer> m2 )
	{
		Iterator<Entry<String, Integer>> iter = m2.entrySet().iterator();
		while(iter.hasNext())
		{
			Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iter.next();
			String key = entry.getKey();
			int value = entry.getValue();
			if( m1.containsKey(key) )
				m1.put(key, m1.get(key) + value );
			else
				m1.put(key, value);
		}
		return m1;
	}
	
	//**************************************************************************************/
	public static TreeMap<String,IndexEntry> mergeMap
		( TreeMap<String,IndexEntry> m1, HashMap<String,Integer> m2, int docID )
	{
		Iterator<Entry<String, Integer>> iter = m2.entrySet().iterator(); 
		while(iter.hasNext())
		{
			Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iter.next();
			String key = entry.getKey();
			int value = (int) entry.getValue();
			
			DocFreqPair pair = new DocFreqPair();
			pair.docID = docID;
			pair.freq = value;
			if( m1.containsKey(key) )
			{
				IndexEntry treeMapEntry = m1.get(key);
				treeMapEntry.indexList.add(pair);
				treeMapEntry.doc_num++;
			}
			else
			{
				IndexEntry treeMapEntry = new IndexEntry();
				treeMapEntry.doc_num = 1;
				treeMapEntry.indexList.add(pair);
				m1.put( key, treeMapEntry );
			}
		}
		return m1;
	}
	
	public static TreeMap<String,IndexEntry> mergeTreeMap( TreeMap<String,IndexEntry> m1, TreeMap<String,IndexEntry> m2 )
	{
		Iterator<Entry<String, Integer>> iter = m2.entrySet().iterator(); 
		while(iter.hasNext())
		{
			
		}
		return m1;
	}
}