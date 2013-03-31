package query;

import java.util.Iterator;

import index.DocFreqPair;
import index.IndexEntry;

public class QueryNode
{
	Iterator<DocFreqPair> curPos;
	IndexEntry entry;
	DocFreqPair curPair;
	
	public QueryNode()
	{
		curPos = null;
		this.entry = null;
		curPair = null;
	}
	
	public QueryNode( IndexEntry entry )
	{
		this.entry = entry;
		curPos = this.entry.indexList.iterator();
		curPair = curPos.next();
	}
	public boolean hasNext()
	{
		return this.curPos.hasNext();
	}
	public void moveToNext()
	{
		this.curPair = curPos.next();
	}
	public int getCurDocID()
	{
		return this.curPair.getDocID();
	}
	public int getCurFreq()
	{
		return this.curPair.getFreq();
	}
}