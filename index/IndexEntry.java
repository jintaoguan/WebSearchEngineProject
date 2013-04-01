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
	
	public IndexEntry( String[] indexLineSegs )
	{
		this.doc_num = Integer.parseInt( indexLineSegs[2] );
		this.indexList = new LinkedList<DocFreqPair>();
//		System.out.println( indexLineSegs[1] );
		//@ a 5 1 1 2 1 3 1 4 1 5 1 ----11
		for( int i = 3; i < doc_num * 2 + 3; i = i + 2 )
		{
			DocFreqPair pair = new DocFreqPair();
			pair.setDocID( Integer.parseInt( indexLineSegs[i] ) );
			pair.setFreq( Integer.parseInt( indexLineSegs[i+1] ) );
			this.indexList.add(pair);
		}
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
