package index;

public class PageInfo
{
	private String m_pageURL;
	private String m_pageLength;
	private String m_fileID;
	private String m_pageOffset;
	private String m_WordCount;
	private StringBuffer m_sb;
	
	public PageInfo( String textLine )
	{
		String[] segs = textLine.split(" ");
		this.m_pageURL = segs[0] ;
		this.m_pageLength =  segs[3];
		this.m_fileID = "0";
		this.m_pageOffset = "0";
		this.m_WordCount = "0";
		this.m_sb = new StringBuffer();
	}
	
	public PageInfo()
	{
		this.m_pageURL = null ;
		this.m_pageLength =  null;
		this.m_fileID = null;
		this.m_pageOffset = null;
		this.m_WordCount = null;
		this.m_sb = new StringBuffer();
		
	}
	
	public void convertStringToObject( String textLine )
	{
		String[] segs = textLine.split(" ");
		this.m_pageURL = segs[0];
		this.m_fileID = segs[1];
		this.m_pageOffset = segs[2];
		this.m_pageLength = segs[3];
		this.m_WordCount = segs[4];
	}
	
	//output format [ url, file_id, offset, length ]
	public String pageInfoToString()
	{
		this.m_sb.append( this.m_pageURL + ' ' );
		this.m_sb.append( this.m_fileID + ' ' );
		this.m_sb.append( this.m_pageOffset + ' ' );
		this.m_sb.append( this.m_pageLength + ' ');
		this.m_sb.append( this.m_WordCount );
		String ret = this.m_sb.toString();
		this.m_sb.delete( 0, m_sb.length() - 1 );
		return ret;
	}
	
	// DataFileID setter function
	public void setFileID( int file_id )
	{
		this.m_fileID = String.valueOf( file_id );
	}
	public void setFileID( String file_id )
	{
		this.m_fileID = file_id;
	}
	// PageOffset setter function
	public void setPageOffset( int page_offset )
	{
		this.m_pageOffset = String.valueOf( page_offset );
	}
	public void setPageOffset( String page_offset )
	{
		this.m_pageOffset = page_offset;
	}
	public void setPageLength( int page_length )
	{
		this.m_pageLength = String.valueOf( page_length );
	}
	public void setPageLength( String page_length )
	{
		this.m_pageLength = page_length;
	}
	public void setWordCount( int word_count )
	{
		this.m_WordCount = String.valueOf( word_count );
	}
	public void setWordCount( String word_count )
	{
		this.m_WordCount = word_count;
	}
	
	//getter function
	public String getPageURL()
	{
		return this.m_pageURL;
	}
	public String getFileID()
	{
		return this.m_fileID;
	}
	public String getPageOffset()
	{
		return this.m_pageOffset;
	}
	public String getPageLength()
	{
		return this.m_pageLength;
	}
	public String getWordCount()
	{
		return this.m_WordCount;
	}
}