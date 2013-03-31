package index;

public class DocFreqPair
{
	private int docID;
	private int freq;
	
	public DocFreqPair()
	{
		this.docID = 0;
		this.freq = 0;
	}
	public DocFreqPair( String docID, String freq )
	{
		this.docID = Integer.parseInt( docID );
		this.freq = Integer.parseInt( freq );
	}
	
	public int getDocID()
	{
		return this.docID;
	}
	public void setDocID( int docid )
	{
		this.docID = docid;
	}
	public int getFreq()
	{
		return this.freq;
	}
	public void setFreq( int freq )
	{
		this.freq = freq;
	}
}