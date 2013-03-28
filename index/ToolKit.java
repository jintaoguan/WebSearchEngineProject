package index;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ToolKit
{
	public static HashMap<String, Integer> mergeMap( HashMap<String,Integer> m1, HashMap<String,Integer> m2 )
	{
		Iterator iter = m2.entrySet().iterator();
		while(iter.hasNext())
		{
			Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iter.next();
			String key = entry.getKey();
			int value = entry.getValue();
			if( m1.containsKey(key) )
				m1.put(key, m1.get(key) + m2.get(key));
			else
				m1.put(key, m2.get(key));
		}
		return m1;
	}
}