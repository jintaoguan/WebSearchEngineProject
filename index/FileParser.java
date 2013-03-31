package index;


import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;


public class FileParser
{
	public File DataFile;
	public File IndexFile;
	public int NumOfPages;
	public int NumOfCurrentPage;
	public String DataFileString;
	public String[] IndexFileLines;
	
	private int m_lastDocID;
	
	private ArrayList<String> m_docIDList;
	public int CurrentOffset;
	
	
	private static final int BUFFERSIZE = 20*1000*1000;
	
	private TreeMap<String, IndexEntry> fileMap;
	
	public FileParser( File data_file, File index_file, ArrayList<String> docIDList, int lastDocID )
	{
		System.out.println( "Process Data File : " + data_file.getAbsolutePath() + " ..." );
		System.out.println( "Process Index File : " + index_file.getAbsolutePath() + " ..." );
		
		this.DataFile = data_file;
		this.IndexFile = index_file;
		this.NumOfCurrentPage = 0;
		this.CurrentOffset = 0;
		this.fileMap = new TreeMap<String, IndexEntry>();
		
		this.m_docIDList = docIDList;
		this.DataFileString = readGZFileToString(DataFile);
		this.DataFileString = this.DataFileString.toLowerCase();
		
		String indexFileString = readGZFileToString(IndexFile);
		this.IndexFileLines = indexFileString.split("\n");
		this.m_lastDocID = lastDocID;
//		System.out.println( indexFileString );
//		System.out.println( DataFileString.length() );
//		System.out.println( IndexFileLines.length );
	}
	
	// parse a file with 300 pages in it
	public TreeMap<String, IndexEntry> parse()
	{
		HashMap<String, Integer> pageMap = null;
//		for( int i = 0; i < 3; ++i )
		for( int i = 0; i < IndexFileLines.length; ++i )
		{
			String urlindex = IndexFileLines[i];
			String[] strseg = urlindex.split(" ");
			
			int endOffset = CurrentOffset + Integer.parseInt(strseg[3]);
			String page_content = DataFileString.substring( CurrentOffset, endOffset );
			CurrentOffset = endOffset;
			
//			System.out.println( page_content );

			PageParser pp = new PageParser( page_content, urlindex, m_docIDList );
			pageMap = pp.parse();
//			size = size + pageMap.size();
//			System.out.println( pageMap.size());
//			ToolKit.OutputMap(pageMap);
			
			//***************************************************
			fileMap = ToolKit.mergeMap( fileMap, pageMap, m_lastDocID + m_docIDList.size() );
//			System.out.println( "Current Map Size : " + fileMap.size() );
			
			//***************************************************
		}
//		System.out.println( size );
		return fileMap;
	}
	
	private String readGZFileToString(File inFile)
	{
		StringBuffer sb = new StringBuffer();
		try{
			GZIPInputStream gin = new GZIPInputStream(new FileInputStream(inFile), BUFFERSIZE);
			byte[] buf = new byte[100000];
			int len;
			while ( ( len = gin.read(buf) ) > 0 )
			{
				for(int i = 0; i < len; i++)
					sb.append((char) buf[i]);
			}
			gin.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return sb.toString();
	}
}