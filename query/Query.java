package query;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.PriorityQueue;

import index.HeapIndexNode;
import index.IndexEntry;
import index.DocFreqPair;

public class Query
{
	private ArrayList<String> keyWords;
	private ArrayList<String> indexLines;
	private LinkedList<Integer> docIDs;
	
	private PriorityQueue<HeapBM25Node> heap;
	
	private String wordOffset;
	private File wordOffsetFile;
	private File indexFile;
	
	private Snippet snpt;
	
	private final int TOP_K_PAGE = 10;
	
	private final int AVERAGE_PAGE_LENGTH = 30000;
	private final int TOTAL_PAGE_NUM = 2635851;
	
	
	
	public static void main( String[] argv )
	{
//		@SuppressWarnings("resource")
//		Scanner scanner = new Scanner(System.in);
//		System.out.println("Search: ");
//		String query_str = scanner.nextLine();
//		System.out.print( query_str );
		Query query = new Query();
		while( true )
		{
			query.inputKeyWords();
			query.searchKeyWord();
		}
	}
	
	public Query()
	{
		this.keyWords = new ArrayList<String>(20);
		this.indexLines = new ArrayList<String>(20);
		this.docIDs = new LinkedList<Integer>();
		
		this.snpt = new Snippet();
		
		wordOffsetFile = new File( "D:/Work/NZ_data/index/WordOffset.txt" );
		indexFile = new File( "D:/Work/NZ_data/index/final_idx.txt" );
		
		//generate heap
		generateHeap();
		
		//load the word offset file into main memory
		this.loadWordOffset();
		
	}
	
	//clear the buffer of query
	private void clear()
	{
		this.keyWords.clear();
		this.indexLines.clear();
	}
	
	public void inputKeyWords()
	{
		//input
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner( System.in );
		System.out.println("Search: ");
		String query_str = scanner.nextLine();
		//get key words from input
		this.keyWords = getKeyWordsFromQuery( query_str );
	}
	
	public void searchKeyWord()
	{
		//get the key word index from index file
		this.indexLines = getIndexLines();
		
//		this.indexLines = setIndexLines();
		Date before = new Date();
		QueryNode[] entrys = convertIndexLineToQueryNode();
		this.docIDs = getAllDocIDs( entrys );

		for( int i = 0; i < this.docIDs.size(); ++i )
		{
			System.out.print( (i+1) + ": " + this.docIDs.get(i) + " " );
			System.out.print( this.snpt.getURLByDocID( this.docIDs.get(i) ) + '\n' );
			
		}
		
		Date after = new Date();
		System.out.println("used " + (after.getTime() - before.getTime()) + " miliseconds");
//		showDocID( this.docIDs );
		System.out.println( "\nHave found " + this.docIDs.size() +" pages." );
		this.snpt.outputSnippet( this.docIDs );
		this.snpt.clear();
	}
	
	//***************************************************************************
	//                   the most important step of searching
	//***************************************************************************
	private LinkedList<Integer> getAllDocIDs( QueryNode[] entrys )
	{
		int cnt = 0;
		while( entrys[0].hasNext() )
		{
			int doc_id = entrys[0].getCurDocID();
			boolean has = haveCommonDocID( entrys, doc_id, 1, entrys.length  );
			//get BM25 then it into heap  
			if( has == true )
			{
				cnt++;
				double bm25_score = calculateBM25( entrys, doc_id ); 
//				this.docIDs.add( entrys[0].getCurDocID() );
				HeapBM25Node node = new HeapBM25Node( doc_id, bm25_score );
				
				if( this.heap.size() < TOP_K_PAGE )
					this.heap.add( node );
				else if( node.getBM25() > this.heap.peek().getBM25() )
				{
					this.heap.poll();
					this.heap.add( node );
				}
			}
			entrys[0].moveToNext();
		}
		while( !this.heap.isEmpty() )
		{
			int doc_id = this.heap.poll().getDocID();
			this.docIDs.addLast(doc_id);
		}
		System.out.println("Have found " + cnt + " Pages.");
		return this.docIDs;
	}
	
	//***************************************************************************
	//                   calculate the bm25 score of this page
	//***************************************************************************
	private double calculateBM25( QueryNode[] entrys, int doc_id )
	{
		double bm25 = 0.0;
		
		for( int i = 0; i < entrys.length; ++i )
		{
			double w = calculateW( entrys, doc_id, i );
			double r = calculateR( entrys, doc_id, i );
			bm25 += w * r;
		}
		return bm25;
	}
	private double calculateW( QueryNode[] entrys, int doc_id, int i )
	{
		double w = 0.0;
		double tmp1 = ( this.TOTAL_PAGE_NUM - entrys[i].entry.doc_num + 0.5 );
		double tmp2 = ( entrys[i].entry.doc_num + 0.5 );
		w = Math.log10( tmp1 / tmp2 );
		return w;
	}
	private double calculateR( QueryNode[] entrys, int doc_id, int i )
	{
		double k = calculateK( entrys, doc_id, i );
		double tmp1 = entrys[i].getCurFreq() * ( 2.0 + 1.0 );
		double tmp2 = entrys[i].getCurFreq() + k;
		return ( tmp1 / tmp2 );
	}
	private double calculateK( QueryNode[] entrys, int doc_id, int i )
	{
		double ratio = ( this.snpt.getDocLengthByDocID(doc_id) / this.AVERAGE_PAGE_LENGTH );
		double k = 2.0 * ( 1 - 0.75 + 0.75 * ratio );
		return k;
	}
	
	private void generateHeap(){
		this.heap = new PriorityQueue<HeapBM25Node>( this.TOP_K_PAGE, new Comparator<HeapBM25Node>(){
			//compare word alphabetic order and first docID
			public int compare( HeapBM25Node a, HeapBM25Node b )
			{
				double bm25A = a.getBM25();
				double bm25B = b.getBM25();
				
				if( bm25A > bm25B ) return 1;
				else return -1;				
			}
		});
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
//			System.out.println(offsetLine);
			
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
//				System.out.println( "The key word " + this.keyWords.get(i) + " index :{" + indexLine + "}" );
				
				
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


class HeapBM25Node
{
	private double bm25;
	private int doc_id;
	
	public HeapBM25Node( int doc_id, double bm25 )
	{
		this.doc_id = doc_id;
		this.bm25 = bm25;
	}
	
	public double getBM25()
	{
		return this.bm25;
	}
	public int getDocID()
	{
		return this.doc_id;
	}
	
}