package index;

import java.util.LinkedList;
import index.DocFreqPair;

public class IndexEntry
{
	public int doc_num;
	public LinkedList<DocFreqPair> indexList; 
	
	public IndexEntry()
	{
		this.doc_num = 0;
		this.indexList = new LinkedList<DocFreqPair>();
	}
	
	public void setDocNum( int num )
	{
		this.doc_num = num;
	}
	public int getDocNum()
	{
		return this.doc_num;
	}
	
}
