package index;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;


public class FileParser
{
	public File DataFile;
	public File IndexFile;
	public int NumOfPages;
	public int NumOfCurrentPage;
	public String DataFileString;
	public String[] IndexFileLines;
	
	private ArrayList<String> m_docIDList;
	public int CurrentOffset;
	
	
	private static final int BUFFERSIZE = 20*1000*1000;
	
	private HashMap<String, Integer> fileMap;
	
	public FileParser( File data_file, File index_file, ArrayList<String> docIDList )
	{
		System.out.println( "Process Data File : " + data_file.getAbsolutePath() + " ..." );
		System.out.println( "Process Index File : " + index_file.getAbsolutePath() + " ..." );
		
		this.DataFile = data_file;
		this.IndexFile = index_file;
		this.NumOfCurrentPage = 0;
		this.CurrentOffset = 0;
		this.fileMap = new HashMap<String, Integer>();
		
		this.m_docIDList = docIDList;
		this.DataFileString = readGZFileToString(DataFile);
		String indexFileString = readGZFileToString(IndexFile);
		this.IndexFileLines = indexFileString.split("\n");
//		System.out.println( indexFileString );
//		System.out.println( DataFileString.length() );
//		System.out.println( IndexFileLines.length );
	}
	
	// parse a file with 300 pages in it
	public HashMap<String, Integer> parse()
	{
		HashMap<String, Integer> pageMap = null;
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
//			System.out.println(pageMap.size());
			
			//***************************************************
			fileMap = ToolKit.mergeMap( fileMap, pageMap );
			
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