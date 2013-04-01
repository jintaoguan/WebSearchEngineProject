/*
 * node in the merge sort big heap, represent each blocks
 */
package index;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

public class HeapIndexNode
{
	private String word;
	private IndexEntry index;
	private int fromFile;
	
	public HeapIndexNode( String indexLine )
	{
		String[] segs = indexLine.split(" ");
		this.word = segs[0];
		this.index = new IndexEntry(segs);
		this.fromFile = 0;
	}
	
	public HeapIndexNode( String indexLine, int filenum )
	{
		String[] segs = indexLine.split(" ");
		this.word = segs[1];
		this.index = new IndexEntry(segs);
		this.fromFile = filenum;
	}
	
	public HeapIndexNode( String keyword, IndexEntry index, int filenum )
	{
		this.word = keyword;
		this.index = index;
		this.fromFile = filenum;
	}
	
	public IndexEntry getIndex()
	{
		return this.index;
	}
	public String getWord()
	{
		return this.word;
	}
	public int getFirstDocID()
	{
		return this.index.indexList.get(0).getDocID();
	}
	public int getFromFile()
	{
		return this.fromFile;
	}
	public String toString()
	{
		return this.word + " " + this.getFirstDocID();
	}
	
	public String nodeToString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(this.word + " " +  this.index.doc_num + " ");
		Iterator<DocFreqPair> iter = this.index.indexList.iterator();
		while( iter.hasNext() )
		{
			DocFreqPair pair = iter.next();
			sb.append( pair.getDocID() + " " );
			sb.append( pair.getFreq() + " " );
		}
		return sb.toString();
	}
	
	public String pairToString()
	{
		StringBuffer sb = new StringBuffer();
//		sb.append(this.word + " " +  this.index.doc_num + " ");
		Iterator<DocFreqPair> iter = this.index.indexList.iterator();
		while( iter.hasNext() )
		{
			DocFreqPair pair = iter.next();
			sb.append( pair.getDocID() + " " );
			sb.append( pair.getFreq() + " " );
		}
		return sb.toString();
	}
	
	public void addPairs( HeapIndexNode node )
	{
		if( !this.word.equals(node.getWord()) )
			return;
		Iterator<DocFreqPair> iter = node.getIndex().indexList.iterator();
		while( iter.hasNext() )
		{
			DocFreqPair pair = iter.next();
			this.index.indexList.addLast(pair);
		}
		this.index.doc_num = this.index.doc_num + node.getIndex().indexList.size(); 
	}
	
	public void clear()
	{
		this.index.doc_num = 0;
		this.index.indexList.clear();
		this.fromFile = 0;
	}
//	private int currentPointToTempIndex = -1;
//	private ArrayList<ArrayList<Integer>> tempIndex;
//	private Block block;
//
//	MergeSortNode(File tempIndexFile){
//		//[WordID, DocFrequency, [DocIDs], [Frequencies], [Positions], [Contexts]]
//		this.tempIndex = new ArrayList<ArrayList<Integer>>();
//		this.block = new Block(tempIndexFile);
//		next();
//	}
//
//	//get next word index in this block
//	//if the cached word indexed are used up, then read more form disk
//	public boolean next(){
//		if(++currentPointToTempIndex >= tempIndex.size()){
//			tempIndex = this.block.readNextBlock();
//			currentPointToTempIndex = 0;
//			if(tempIndex == null) return false;
//		}
//		return true;
//	}
//
//	//see the current word index
//	public ArrayList<Integer> peak(){
//		return tempIndex.get(currentPointToTempIndex);
//	}
}

//gradually read and pare data from disk
class Block{
	private final int BUFFERSIZE = 1*1000*1000;
	private GZIPInputStream gin;
	private byte[] tempData;
	private int pos;
	private int len;

	Block(File tempIndexFile){
		try{
			this.gin = new GZIPInputStream(new FileInputStream(tempIndexFile), BUFFERSIZE);
			tempData = new byte[1000];
			readMore();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	//parse more indexes from disk
	public ArrayList<ArrayList<Integer>> readNextBlock(){
		if(len == -1) return null;

		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
		int count = 0;
		int thisLen = this.len - this.pos;
		while(count < thisLen){
			ArrayList<Integer> thisIndex= readNextIndex();
			result.add(thisIndex);
			count += thisIndex.size() * 4;
		}
		return result;

	}

	//parse one index from disk
	private ArrayList<Integer> readNextIndex(){
		ArrayList<Integer> thisResult = new ArrayList<Integer>();
		int remain = 0;
		//add wordID
		thisResult.add(getNextInt());
		//add docFrequency
		int docFrequency = getNextInt();
		thisResult.add(docFrequency);
		//add docIDs
		for(int i = 1; i <= docFrequency; i++) thisResult.add(getNextInt());
		//add frequencies
		for(int i = 1; i <= docFrequency; i++){
			int thisFre = getNextInt();
			thisResult.add(thisFre);
			remain += thisFre;
		}
		//add positions
		for(int i = 1; i <= remain; i++) thisResult.add(getNextInt());
		//add contexts
		for(int i = 1; i <= remain; i++) thisResult.add((int) getNextByte());

		return thisResult;
	}

	//uncompress an integer from binary file
	private int getNextInt(){
		int result = (getNextByte() & 0xFF) << 24 | (getNextByte() & 0xFF) << 16 |
					(getNextByte() & 0xFF) << 8 | (getNextByte() & 0xFF);
		return result;
	}

	//get one byte from disk
	//if cached data is used up, then read more from disk
	private byte getNextByte(){
		byte temp = tempData[pos++];
		if(len != -1 && this.pos >= this.len) readMore();
		return temp;
	}

	//read BUFFERSIZE data from disk
	private void readMore(){
		try{
			this.len = this.gin.read(this.tempData);
			this.pos = 0;
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}