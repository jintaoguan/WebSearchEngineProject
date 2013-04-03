package query;

import index.PageInfo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.zip.GZIPInputStream;


public class Snippet
{
	private ArrayList<PageInfo> m_DocIDList;
	private File m_DocIDFile;
	
	private File m_targetFile;
	
	private static final int BUFFERSIZE = 20*1000*1000;
	
	
	public Snippet()
	{
		this.m_DocIDList = new ArrayList<PageInfo>(3000000); 
		this.m_DocIDFile = new File( "D:/Work/NZ_data/docID/docID.txt" );
		this.loadDocIDFile();
		System.out.println(" Initilization Completed. ");
//		File folder = new File("D:/Work/NZ_data/data/");
//		getFileByName( folder, "2223_data" );
//		System.out.println( this.m_targetFile.getAbsolutePath() );
	}
	
	public void clear()
	{
		
	}
	
	private void loadDocIDFile()
	{
		FileReader reader = null;
		try {
			reader = new FileReader( this.m_DocIDFile );
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.out.println( "WordOffset File not found." );
		}
		BufferedReader br = new BufferedReader(reader);
		try {
			
			//load the word offset file into main memory
			String line = null;
			while( (line = br.readLine()) != null )
			{
				PageInfo info = new PageInfo();
				info.convertStringToObject(line);
				this.m_DocIDList.add(info);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println( "Read WordOffset File Error." );
		}
	}
	
	//*************************************************************
	public void outputSnippet( LinkedList<Integer> DocIDList )
	{
		for( int i = 0; i < 3; ++i )
		{
			int doc_id = DocIDList.get(i) - 1;
			PageInfo info = this.m_DocIDList.get( doc_id );
//			System.out.println( DocIDList.get(i) );
//			System.out.println( info.getPageURL() );
			
			getPageSnippet( doc_id );
		}
	}
	
	//*************************************************************
	private void getPageSnippet( int doc_id )
	{
//		System.out.println("Get Page Snippet for Doc ID: " + doc_id );
		PageInfo info = this.m_DocIDList.get( doc_id );
//		System.out.println("Get Page Snippet for Doc ID: " + info.getPageURL() );
		String page_content = getPageFromData( info );
		File snippet_file = new File("D:/Work/NZ_data/snippet/result.txt");
		try {
			FileWriter fw = new FileWriter( snippet_file, true );
			fw.write( page_content );
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private String getPageFromData( PageInfo info )
	{
		String filename = info.getFileID() + "_data";
		File folder = new File("D:/Work/NZ_data/data/");
		getFileByName( folder, filename );
		
		//get the page offset in the file 
		int pageOffset = Integer.parseInt( info.getPageOffset() );
		//get the index length of the word in index file
		int pageLen = Integer.parseInt( info.getPageLength() );
		
		String file_content = readGZFileToString( this.m_targetFile );
		String page_content = file_content.substring(  pageOffset, pageOffset + pageLen );
		return page_content;
	}
	
	private void getFileByName( File thisFile, String filename )
	{
		if(!thisFile.isDirectory())
		{
			if( thisFile.getName().equals(filename) ) 
			{
				this.m_targetFile = thisFile;
			}
		}
		else{
			String[] subFiles = thisFile.list();
			Arrays.sort(subFiles);
			for(String name : subFiles){
				getFileByName( new File(thisFile, name), filename );
			}
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
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	public int getDocLengthByDocID( int doc_id )
	{
		PageInfo info = this.m_DocIDList.get( doc_id - 1 );
		return Integer.parseInt( info.getPageLength() );
	}
	
	public String getURLByDocID( int doc_id )
	{
		PageInfo info = this.m_DocIDList.get( doc_id - 1 );
		return info.getPageURL();
	}
}