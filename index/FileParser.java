package index;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.GZIPInputStream;


public class FileParser
{
	public File DataFile;
	public File IndexFile;
	public int NumOfPages;
	public int NumOfCurrentPage;
	public String DataFileString;
	public String[] IndexFileLines;
	
	public int CurrentOffset;
	
	private static final int BUFFERSIZE = 20*1000*1000;
	
	public FileParser( File data_file, File index_file )
	{
		System.out.println( "Process Data File : " + data_file.getAbsolutePath() + " ..." );
		System.out.println( "Process Index File : " + index_file.getAbsolutePath() + " ..." );
		
		this.DataFile = data_file;
		this.IndexFile = index_file;
		this.NumOfCurrentPage = 0;
		this.CurrentOffset = 0;
		
		this.DataFileString = readGZFileToString(DataFile);
		String indexFileString = readGZFileToString(IndexFile);
		this.IndexFileLines = indexFileString.split("\n");
		System.out.println( DataFileString.length() );
		System.out.println( IndexFileLines.length );
	}
	
	public void parse()
	{
		for( int i = 0; i < IndexFileLines.length; ++i )
		{
			String urlindex = IndexFileLines[i];
			String[] strseg = urlindex.split(" ");
			String page_content = DataFileString.substring( CurrentOffset, Integer.parseInt(strseg[3]) );
//			System.out.println( page_content );
			PageParser pp = new PageParser( page_content );
			pp.parse();
		}
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