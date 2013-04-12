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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.PriorityQueue;

import index.HeapIndexNode;
import index.IndexEntry;
import index.DocFreqPair;

public class Query
{
	//key word
	private ArrayList<String> keyWords;
	
	private ArrayList< ArrayList<Integer> > indexes;
	private ArrayList<Integer> cursors;
	
	private ArrayList<Integer> docIDs;
	
	private PriorityQueue<HeapBM25Node> heap;
	
	private LinkedList<Double> bm25List;
	
	private String wordOffset;
	private File wordOffsetFile;
	private File indexFile;
	
	private Snippet snpt;
	
	private final int TOP_K_PAGE = 10;
	
	private final int AVERAGE_PAGE_LENGTH = 587;
	private final int TOTAL_PAGE_NUM = 2635851;
	
	private int max_id;
	
	public static void main( String[] argv )
	{
		Query query = new Query();
		while( true )
		{
			query.inputKeyWords();
			query.searchKeyWord();
			query.clear();
		}
	}
	
	public Query()
	{
		this.keyWords = new ArrayList<String>(20);
		this.docIDs = new ArrayList<Integer>();
		this.bm25List = new LinkedList<Double>();
		
		this.indexes = new ArrayList<ArrayList<Integer>>();
		this.cursors = new ArrayList<Integer>();
		
		this.snpt = new Snippet();
		this.max_id = 0;
		
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
		this.indexes.clear();
		this.docIDs.clear();
		this.bm25List.clear();
		this.cursors.clear();
		this.max_id = 0;
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
		for( int i = 0; i < this.keyWords.size(); ++i )
		{
			String keyword = keyWords.get(i);
			keyword = '\n' + keyword + ' ';
			int wordBeginPos = this.wordOffset.indexOf( keyword );
			if( wordBeginPos == -1 )
			{
				System.out.println("No such key word.");
				return;
			}
			wordBeginPos = wordBeginPos + 1; 
			String offsetLine = getOffsetLine( wordBeginPos );
			String[] segs = offsetLine.split("[ \n]");
			long indexBeginPos = Long.parseLong( segs[1] );
			int indexEndPos = Integer.parseInt( segs[2] );
			
			ArrayList<Integer> index = getIndex( indexBeginPos, indexEndPos );
			this.indexes.add(index);
		}
		//the start time
		Date before = new Date();
		
		this.docIDs = getAllDocIDs( this.indexes );

//		for( int i = 0; i < this.docIDs.size(); ++i )
		for( int i = this.docIDs.size() - 1; i >= 0; --i )
		{
			System.out.print( (this.docIDs.size() - i) + ": " + this.bm25List.get(i) + "                " );
			System.out.print( this.snpt.getURLByDocID( this.docIDs.get(i) ) + '\n' );
		}
		
		Date after = new Date();
		System.out.println("used " + (after.getTime() - before.getTime()) + " miliseconds");
//		showDocID( this.docIDs );
		
		
//		System.out.println( "\nShow " + this.docIDs.size() +" Snippets." );
//		this.snpt.outputSnippet( this.docIDs, this.keyWords );
//		this.snpt.clear();
	}
	
	
	//***************************************************************************
	//                   the most important step of searching
	//***************************************************************************
	private ArrayList<Integer> getAllDocIDs( ArrayList<ArrayList<Integer>> indexes )
	{
		for( int i = 0; i < indexes.size(); ++i )
		{
			System.out.println("the keyword " + this.keyWords.get(i) + " has " 
					+ this.indexes.get(i).size()/2 + " pages");
		}
		int cnt = 0;
		for( int i = 0; i < indexes.size(); ++i )
			this.cursors.add(0);
		ArrayList<Integer> list = indexes.get(0);
		for( int i = 0; i < list.size(); i = i + 2 )
		{
			int doc_id = list.get(i);
			if( doc_id < this.max_id ) continue;
			boolean has = haveCommonDocID( doc_id, 1 );
			
			//get BM25 then push it into heap  
			if( has == true )
			{
				cnt++;
				double bm25_score = calculateBM25( doc_id ); 
				HeapBM25Node node = new HeapBM25Node( doc_id, bm25_score );
				
				if( this.heap.size() < TOP_K_PAGE )
					this.heap.add( node );
				else if( node.getBM25() > this.heap.peek().getBM25() )
				{
					this.heap.poll();
					this.heap.add( node );
				}
			}
		}
		while( !this.heap.isEmpty() )
		{
			HeapBM25Node node = this.heap.poll();
			int tmp_doc_id = node.getDocID();
			double bm25 = node.getBM25();
			Double obj = new Double( bm25);
			this.bm25List.addLast( obj );
			this.docIDs.add( tmp_doc_id );
		}
		System.out.println("Have found " + cnt + " Pages.");
		return this.docIDs;
	}
	//search for the common docID using recursion
	private boolean haveCommonDocID( int target_id, int depth )
	{
		if( depth >= this.indexes.size() )
			return true;
//		int doc_id = entrys[depth].getCurDocID();
		int doc_id = getCurrentDocID(depth);
		if( target_id == doc_id )
			return haveCommonDocID( target_id, depth + 1 );
		else if( target_id > doc_id )
		{
			while( target_id > doc_id )
			{
//				if( entrys[depth].hasNext() )
				if( this.cursors.get(depth) < this.indexes.get(depth).size() - 2 )
					this.cursors.set( depth, this.cursors.get(depth) + 2 );
				else 
					return false;
//				doc_id = entrys[depth].getCurDocID();
//				int cursor_pos = this.cursors.get(depth);
//				doc_id = this.indexes.get(depth).get(cursor_pos);
				doc_id = getCurrentDocID(depth);
			}
			if( doc_id == target_id )
				return haveCommonDocID( target_id, depth + 1 );
			else
			{
				this.max_id = doc_id;
				return false;
			}
		}else{
			return false;
		}
	}
	private int getCurrentDocID( int depth )
	{
		int cursor_pos = this.cursors.get(depth);
		return this.indexes.get(depth).get(cursor_pos);
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
	//***************************************************************************
	//                   calculate the bm25 score of this page
	//***************************************************************************
	private double calculateBM25( int doc_id )
	{
		double bm25 = 0.0;
		
		for( int i = 0; i < this.indexes.size(); ++i )
		{
			double w = calculateW( doc_id, i );
			double r = calculateR( doc_id, i );
			bm25 += w * r;
		}
		return bm25;
	}
	private double calculateW( int doc_id, int i )
	{
		double w = 0.0;
		double tmp1 = ( this.TOTAL_PAGE_NUM - this.indexes.get(i).size() / 2 + 0.5 );
		double tmp2 = ( this.indexes.get(i).size() + 0.5 );
		w = Math.log10( tmp1 / tmp2 );
		return w;
	}
	private double calculateR( int doc_id, int i )
	{
		double k = calculateK( doc_id );
//		double tmp1 = entrys[i].getCurFreq() * ( 2.0 + 1.0 );
//		double tmp1 = entrys[i].getCurFreq() + k;
		
		int pos = this.cursors.get(i);
		double tmp1 = this.indexes.get(i).get( pos + 1 ) * ( 2.0 + 1.0 );
		double tmp2 = this.indexes.get(i).get( pos + 1 ) + k;
		return ( tmp1 / tmp2 );
	}
	private double calculateK( int doc_id )
	{
		double ratio = ( this.snpt.getDocLengthByDocID(doc_id) / this.AVERAGE_PAGE_LENGTH );
		double k = 2.0 * ( 1 - 0.75 + 0.75 * ratio );
		return k;
	}
	//***************************************************************************
	//						get bytes from index file
	//***************************************************************************
	private ArrayList<Integer> getIndex( long beginPos, int length )
	{
		ArrayList<Integer> result = null;
		FileInputStream fis = null;
		byte[] fileLine = new byte[length];
		try {
			fis = new FileInputStream( this.indexFile );
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Index File not found.");
		}
		BufferedInputStream bis = new BufferedInputStream(fis);
		try {
			bis.skip( beginPos );
			bis.read( fileLine, 0, length );
			
			result = getIntFromBytes( fileLine );
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Read Index Line Error.");
		}
		return result;
	}
	
	//***************************************************************************
	//                   decode the bytes to integer
	//***************************************************************************
	public static ArrayList<Integer> getIntFromBytes( byte[] line )
	{
		ArrayList<Integer> arr = new ArrayList<Integer>();
		int pos = 0;
		while( pos < line.length )
		{
			int result = 0;
			while((line[pos] & 0x80) == 0x80){
				result = result * 128 + (line[pos++] & 0x7F);
			}
			result = result * 128 + (line[pos++] & 0x7F);
			arr.add(result);
		}
		return arr;
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
	
	private ArrayList<String> getKeyWordsFromQuery( String query_str )
	{
		query_str = query_str.toLowerCase();
		String[] words = query_str.split(" ");
		HashSet<String> set = new HashSet<String>();
		for( int i = 0; i < words.length; ++i )
		{
			if( !set.contains(words[i]) )
				set.add(words[i]);
		}
		this.keyWords.addAll(set);
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
	
	//for test used
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