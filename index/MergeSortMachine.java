package index;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class MergeSortMachine {
	//buffer of output file cache, 100 * 1000 * 1000
	private static final int BUFFERSIZE = 30 * 1000 * 1000;
	//merge sort heap
	private PriorityQueue<HeapIndexNode> heap;
	//output buffer
	private StringBuffer buffer;
	
	private HeapIndexNode bufferNode;
	//merge files
	private ArrayList<File> mergeBlocks;
	private ArrayList<BufferedReader> mergeBlockReaders;
	//global index of each word index' begin position in final index file
	private long posCount;
	//last keyword
	private String lastWord;
	//total index
	private File indexFile;
	
	private long mergeFileSize;
	
	public MergeSortMachine()
	{
		this.mergeBlockReaders = new ArrayList<BufferedReader>();
		this.mergeBlocks = new ArrayList<File>();
		this.buffer = new StringBuffer();
		this.bufferNode = null;
		
		this.lastWord = null;
		
		this.mergeFileSize = 0;
		String filepath = "D:/Work/NZ_data/index/final_idx.txt";
		this.indexFile = new File(filepath);
		
		File folder = new File( "D:/Work/NZ_data/merge_files" );
		
		
		findAllMergeBlocks( folder );
		
		
		
		//generate heap using intermediate blocks
		generateHeap();

//		HeapIndexNode hin1 = new HeapIndexNode("a 2 1 1 3 1");
//		HeapIndexNode hin2 = new HeapIndexNode("a 2 4 1 5 1");
//		HeapIndexNode hin3 = new HeapIndexNode("b 2 2 1 3 1");
//		this.heap.add(hin1);
//		this.heap.add(hin2);
//		this.heap.add(hin3);
//		while( !this.heap.isEmpty())
//		{
//			System.out.println(this.heap.poll());
//		}
		System.out.println( "Found " + this.mergeBlockReaders.size() + " merge_files." );
	}

	private void findAllMergeBlocks( File thisFile )
	{
		if( !thisFile.isDirectory() )
		{
			if( thisFile.getName().contains("merge_file_") )
			{
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new FileReader(thisFile));
					this.mergeBlockReaders.add(reader);
					this.mergeBlocks.add(thisFile);
					this.mergeFileSize = this.mergeFileSize + thisFile.length();
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					System.out.println("Have not found mergeFile: " + thisFile.getAbsolutePath() );
				}
				
			}
			return;
		}
		String[] subFiles = thisFile.list();
		Arrays.sort(subFiles);
		for( String name : subFiles )
			findAllMergeBlocks(new File(thisFile, name));
	}
	public void merge()
	{
		System.out.println("Process MergeSort ...");
		
		//initialize the heap
		for( int i = 0; i < this.mergeBlockReaders.size(); ++i )
		{
//			System.out.println( this.mergeBlocks.get(i).getAbsolutePath() );
			String str = null;
			try {
				str = this.mergeBlockReaders.get(i).readLine();
				if( str != null )
				{
					HeapIndexNode node = new HeapIndexNode( str, i );
					this.heap.add( node );
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Have not found merge file.");
			}
		}
//		while( !this.heap.isEmpty())
//			System.out.println( this.mergeBlocks.get(this.heap.poll().getFromFile()).getAbsolutePath() );
		
		//first read
		this.bufferNode = this.heap.poll();
		int filenum = this.bufferNode.getFromFile();
		this.lastWord = this.bufferNode.getWord();
//		this.buffer.append( firstNode.nodeToString() );
		
		try {
			String nextIndexLine = this.mergeBlockReaders.get(filenum).readLine();
			HeapIndexNode nextNode = null;
			if( nextIndexLine != null )
			{
				nextNode = new HeapIndexNode( nextIndexLine, filenum );
				this.heap.add( nextNode );
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Read Index Line Error");
		}
		
		int cnt = 0;
		int wordCount = 0;
		long hasProcessed = 0;
		int rate = 0;
		int old_rate = 0;
		System.out.println( this.mergeFileSize );
		while( true )
		{
			HeapIndexNode node = this.heap.poll();
			HeapIndexNode nextNode = null;
			int from = node.getFromFile();
			try {
				String nextIndexLine = this.mergeBlockReaders.get(from).readLine();
				if( nextIndexLine != null )
				{
					nextNode = new HeapIndexNode( nextIndexLine, from );
					this.heap.add( nextNode );
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Read Index Line Error");
			}
			
			//************************************************************************
			if( this.lastWord.equals( node.getWord()) )
			{
//				this.buffer.append( node.pairToString() );
				this.bufferNode.addPairs(node);
			}
			else if( !this.lastWord.equals( node.getWord()) )
			{
				this.buffer.append( '\n' + this.bufferNode.nodeToString() );
				this.bufferNode.clear();
				this.bufferNode = node;
				this.lastWord = node.getWord();
				wordCount++;
			}
			//************************************************************************
			
			if( this.buffer.length() > this.BUFFERSIZE )
			{
				writeBufferToFile();
				hasProcessed += this.BUFFERSIZE;
				this.buffer.delete( 0 , this.buffer.length() - 1 );
				this.bufferNode.clear();
//				rate = (int)(hasProcessed / this.mergeFileSize);
				System.out.println( "Have processed " + (hasProcessed/1000000) + "/" 
							+ (this.mergeFileSize/1000000) );
				if( hasProcessed/1000000 > 4000 )
					break;
			}
			
		}
//		writeBufferToFile();
		this.buffer.delete( 0 , this.buffer.length() - 1 );
	}

	private void writeBufferToFile()
	{
		System.out.println( "Write to index file ..." );
		try {
			FileWriter writer = new FileWriter( this.indexFile, true );
			writer.write( this.buffer.toString() );
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Have not fount final index file: " + this.indexFile.getAbsolutePath());
		}
		System.out.println( "Write completed ..." );
	}
	
	private void generateHeap(){
		this.heap = new PriorityQueue<HeapIndexNode>( this.mergeBlockReaders.size(), new Comparator<HeapIndexNode>(){
			//compare word alphabetic order and first docID
			public int compare( HeapIndexNode a, HeapIndexNode b )
			{
				//[Word, DocFrequency, [DocIDs, Frequencies], [DocIDs, Frequencies], [DocIDs, Frequencies]...]
				String wordA = a.getWord();
				String wordB = b.getWord();
				
				if( !wordA.equals(wordB) ) 
					return wordA.compareTo(wordB);
				
				int firstDocID_A = a.getFirstDocID();
				int firstDocID_B = b.getFirstDocID();
				if( firstDocID_A != firstDocID_B )
					return firstDocID_A - firstDocID_B;
				else return -1;
			}
		});
//		for(int i = 0; i < this.mergeBlockReaders.size(); i++){
//			System.out.println("generator MergeSortNode " + i );
//			this.heap.offer(new MergeSortNode(new File(folder, files[i])));
//		}
	}
	

}

