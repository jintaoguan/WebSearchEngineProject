package query;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import index.IndexEntry;
import index.DocFreqPair;

public class Query
{
	private ArrayList<String> keyWords;
	private ArrayList<String> indexLines;
	private LinkedList<Integer> docIDs;
	
	private String wordOffset;
	private File wordOffsetFile;
	private File indexFile;
	
	public static void main( String[] argv )
	{
//		@SuppressWarnings("resource")
//		Scanner scanner = new Scanner(System.in);
//		System.out.println("Search: ");
//		String query_str = scanner.nextLine();
//		System.out.print( query_str );
		Query query = new Query();
		query.searchKeyWord();
	}
	
	public Query()
	{
		this.keyWords = new ArrayList<String>(20);
		this.indexLines = new ArrayList<String>(20);
		this.docIDs = new LinkedList<Integer>();
		
		//input
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner( System.in );
		System.out.println("Search: ");
		String query_str = scanner.nextLine();
		
		wordOffsetFile = new File( "D:/Work/NZ_data/index/WordOffset.txt" );
		indexFile = new File( "D:/Work/NZ_data/index/final_idx.txt" );
		
		//get key words from input
		this.keyWords = getKeyWordsFromQuery( query_str );
		
		//load the word offset file into main memory
		this.loadWordOffset();
	}
	
	public void searchKeyWord()
	{
		//get the key word index from index file
		this.indexLines = getIndexLines();
		
//		this.indexLines = setIndexLines();
		Date before = new Date();
		QueryNode[] entrys = convertIndexLineToQueryNode();
		this.docIDs = getAllDocIDs( entrys );
		Date after = new Date();
		System.out.println("used " + (after.getTime() - before.getTime()) + " miliseconds");
		showDocID( this.docIDs );
	}
	
//	private ArrayList<String> setIndexLines()
//	{
//		String a  = "a 10 1 1 2 1 3 1 4 1 5 1 6 1 7 1 8 1 9 1 10 1";
//		String aa = "aa 3 4 1 7 2 9 1 ";
//		String b =  "b 3 5 1 7 1 10 1";  // 7 is the common doc id
//		this.indexLines.add(a);
//		this.indexLines.add(aa);
//		this.indexLines.add(b);
//		return this.indexLines;
//	}
	
	//the most important step of searching
	private LinkedList<Integer> getAllDocIDs( QueryNode[] entrys )
	{
		while( entrys[0].hasNext() )
		{
			int doc_id = entrys[0].getCurDocID();
			boolean has = haveCommonDocID( entrys, doc_id, 1, entrys.length  );
			if( has == true )
				this.docIDs.add( entrys[0].getCurDocID() );
			entrys[0].moveToNext();
		}
		return this.docIDs;
	}
	
	//search for the common docID using recursion
	private boolean haveCommonDocID( QueryNode[] entrys, int target_id, int depth, int num )
	{
		if( depth >= num )
			return true;
		int doc_id = entrys[depth].getCurDocID();
		if( target_id == doc_id )
			return haveCommonDocID( entrys, target_id, depth + 1, num );
		else if( target_id > doc_id )
		{
			while( target_id > doc_id )
			{
				if( entrys[depth].hasNext() ) 
					entrys[depth].moveToNext();
				else 
					return false;
				doc_id = entrys[depth].getCurDocID();
			}
			if( doc_id == target_id )
				return haveCommonDocID( entrys, target_id, depth + 1, num );
			else
				return false;
		}else{
			return false;
		}
	}
	
	private QueryNode[] convertIndexLineToQueryNode()
	{
		QueryNode[] entrys = new QueryNode[this.keyWords.size()];
		for( int i = 0; i < entrys.length; ++i )
			entrys[i] = convertLineToQueryNode( indexLines.get(i) );
		return entrys;
	}
	
	//convert a line of index to an IndexEntry object
	private QueryNode convertLineToQueryNode( String indexLine )
	{
//		System.out.println( indexLine );
//		QueryNode node = new QueryNode();
		IndexEntry entry = new IndexEntry();
		String[] segs = indexLine.split(" ");
		entry.doc_num = Integer.parseInt( segs[1] );
//		entry.doc_num = Integer.parseInt( segs[1] );
		for( int i = 2; i < segs.length - 1; i = i + 2 )
		{
			DocFreqPair pair = new DocFreqPair( segs[i], segs[i+1] );
			entry.indexList.add(pair);
		}
		QueryNode node = new QueryNode( entry );
		return node;
	}
	
	private ArrayList<String> getKeyWordsFromQuery( String query_str )
	{
		query_str = query_str.toLowerCase();
		String[] words = query_str.split(" ");
		for( int i = 0; i < words.length; ++i )
			this.keyWords.add( words[i] );
		return this.keyWords;
	}
	
	private void loadWordOffset()
	{
		StringBuffer sb = new StringBuffer();
		FileReader reader = null;
		try {
			reader = new FileReader( this.wordOffsetFile );
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.out.println( "WordOffset File not found." );
		}
		BufferedReader br = new BufferedReader(reader);
		try {
			String line = null;
			while( (line = br.readLine()) != null )
				sb.append( line + '\n' );
			
			//load the word offset file into main memory
			this.wordOffset = sb.toString();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println( "Read WordOffset File Error." );
		}
	}
	
	private ArrayList<String> getIndexLines()
	{
		for( int i = 0; i < this.keyWords.size(); ++i )
		{
			String keyword = keyWords.get(i);
			keyword = '\n' + keyword + ' ';
//			System.out.println(keyword);
			int wordBeginPos = this.wordOffset.indexOf( keyword ) + 1;
			
			String offsetLine = getOffsetLine( wordBeginPos );
			System.out.println(offsetLine);
			
			//get the word offset in index file
			long wordOffset = getWordOffsetInIndex( offsetLine );
			//get the index length of the word in index file
			int wordIndexLen = getWordIndexLenInIndex( offsetLine );
			
			//read line index of certain word from index file
			FileInputStream fis = null;
			byte[] fileLine = new byte[wordIndexLen];
			try {
				fis = new FileInputStream( this.indexFile );
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.out.println("Index File not found.");
			}
			BufferedInputStream bis = new BufferedInputStream(fis);
			try {
				bis.skip( wordOffset );
				bis.read( fileLine, 0, wordIndexLen );
				String indexLine = new String(fileLine);
				this.indexLines.add( indexLine );
//				System.out.println( "The length of the index:" + indexLine.length() );
				
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Read Index Line Error.");
			}
		}
		return this.indexLines;
	}

	private String getOffsetLine( int wordBeginPos )
	{
		StringBuffer sb = new StringBuffer();
		int curPos = wordBeginPos;
		while( curPos < this.wordOffset.length() )
		{
			char curChar = this.wordOffset.charAt(curPos);
			sb.append( curChar );
			if( curChar == '\n' ) break;
			curPos++;
		}
		return sb.toString(); 
	}
	
	private long getWordOffsetInIndex( String offsetLine )
	{
		long wordoffset = 0;
		String[] offsetSegs = offsetLine.split(" ");
//		System.out.println( offsetSegs[1] );
		
		//convert offsetSegs[1] to long
		wordoffset = Long.parseLong(offsetSegs[1]);
		return wordoffset;
	}
	private int getWordIndexLenInIndex( String offsetLine )
	{
		int indexLen = 0;
		String[] offsetSegs = offsetLine.split("[ \n]");
//		System.out.println( offsetSegs[2] );
		
		//convert offsetSegs[2] to int
		indexLen = Integer.parseInt( offsetSegs[2] );
		return indexLen;
	}
	
	//for test use
	private void showDocID( LinkedList<Integer> docIDs )
	{
		Iterator<Integer> iter = docIDs.iterator();
		int cnt = 0;
		System.out.println( "Found " + docIDs.size() + " pages." );
		while( iter.hasNext() )
		{
			int docid = iter.next().intValue();
			System.out.print( docid );
			System.out.print( " " );
			cnt++;
			if( cnt == 20 )
			{
				System.out.print( "\n" );
				cnt = 0;
			}
		}
	}
}