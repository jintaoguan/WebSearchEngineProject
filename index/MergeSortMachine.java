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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class MergeSortMachine {
	//buffer of output file cache, 30 * 1000 * 1000
	private static final int BUFFERSIZE =  8 * 1000 * 1000;
	//merge sort heap
	private PriorityQueue<HeapIndexNode> heap;
	//output buffer
//	private StringBuffer buffer;
	
	private HeapIndexNode tmpnode;
	
	private ArrayList<HeapIndexNode> bufferNode;
	//merge files
	private ArrayList<File> mergeBlocks;
	private ArrayList<BufferedReader> mergeBlockReaders;
	//global index of each word index' begin position in final index file
	private long offset;
	//last keyword
	private String lastWord;
	//final index
	private File indexFile;
	//word offset
	private File offsetFile;
	
	private long mergeFileSize;
	
	public MergeSortMachine()
	{
		this.mergeBlockReaders = new ArrayList<BufferedReader>();
		this.mergeBlocks = new ArrayList<File>();
//		this.buffer = new StringBuffer();
		this.bufferNode = new ArrayList<HeapIndexNode>();
		this.tmpnode = null;
		
		this.lastWord = null;
		
		this.mergeFileSize = 0;
		
		this.offset = 0;
		
		String filepath = "D:/Work/NZ_data/index/final_idx.txt";
		this.indexFile = new File(filepath);
		String filepath2 = "D:/Work/NZ_data/index/WordOffset.txt";
		this.offsetFile = new File(filepath2);
		
		File folder = new File( "D:/Work/NZ_data/merge_files" );
		
		findAllMergeBlocks( folder );
		System.out.println( this.mergeBlocks.size() );
		
		generateHeap();

		System.out.println( "Found " + this.mergeBlockReaders.size() + " merge_files." );
	}

	
	//***********************************************************************
	//                           merge sort
	//***********************************************************************
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
		
		//first read
		this.tmpnode = this.heap.poll();
		int filenum = this.tmpnode.getFromFile();
		this.lastWord = this.tmpnode.getWord();
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
		System.out.println( this.mergeFileSize );
		
		int cnt = 0;
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
				this.tmpnode.addPairs(node);
				continue;
			}
			else if( !this.lastWord.equals( node.getWord()) )
			{
//				this.buffer.append( '\n' + this.bufferNode.nodeToString() );
				if( this.tmpnode.getIndex().doc_num == 0 )
				{
					this.tmpnode = node;
					this.lastWord = node.getWord();
				}
				else if( this.tmpnode.getIndex().doc_num != 0 )
				{
					this.bufferNode.add( this.tmpnode );
//					this.tmpnode.clear();
					this.tmpnode = node;
					this.lastWord = node.getWord();
//					System.out.println( this.lastWord );
//					System.out.println( "--------->" + getSize(this.bufferNode) );
				}
			}
			//************************************************************************
			int size = getSize( this.bufferNode );
			if( size > this.BUFFERSIZE )
			{
				String nextWord = this.heap.peek().getWord();
				if( !this.tmpnode.getWord().equals( nextWord ) )
				{
					writeBufferToFile();
//					this.buffer.delete( 0, this.buffer.length() - 1 );
					this.bufferNode.clear();
					this.tmpnode.clear();
				}
			}
			long indexFileSize = this.indexFile.length(); 
			if( indexFileSize > 3 * 1000 * 1000 * 1000 * 1000 )
				break;
		}
		writeBufferToFile();
//		this.buffer.delete( 0 , this.buffer.length() - 1 );
		this.bufferNode.clear();
	}

	
	//**************************************************************************
	//                      write binary code to file
	//**************************************************************************
	private void writeBufferToFile()
	{
		System.out.println( "Write to index file ..." );
		try {
			//*********************************************************
			
			FileOutputStream fos_index = new FileOutputStream( this.indexFile, true );
			FileWriter fw_offset = new FileWriter( this.offsetFile, true );
			
//			long offset = 0;
			for( int i = 0; i < this.bufferNode.size(); ++i )
			{
				HeapIndexNode node = this.bufferNode.get(i);
				String word = node.getWord();
				LinkedList<DocFreqPair> list = node.getIndex().indexList;
				Iterator<DocFreqPair> iter = list.iterator();
				int length = 0;
				while( iter.hasNext() )
				{
					DocFreqPair pair = iter.next();
					
					byte[] b_doc_id = getCompressedBytes(pair.getDocID());
					fos_index.write(b_doc_id);
					byte[] b_doc_freq = getCompressedBytes(pair.getFreq());
					fos_index.write(b_doc_freq);
					
					length += b_doc_freq.length;
					length += b_doc_id.length;
				}
				fw_offset.write( word + " "); 
				fw_offset.write( String.valueOf( this.offset ) + " " );
				fw_offset.write( String.valueOf( length ) + '\n' );
				
				this.offset += length;
			}
			System.out.println( this.bufferNode.get(this.bufferNode.size()-1).getWord() );
			
			fos_index.close();
			fw_offset.close();
			
			//*********************************************************
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Have not fount final index file: " + this.indexFile.getAbsolutePath());
		}
		System.out.println( "Write completed ..." );
	}
	
	//generate the heap of processing merge sort
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
	}
	
	//var-byte compression, convert integer to byte[]
	private byte[] getCompressedBytes(int number)
	{
		//int 5 [0000 0101] => byte [0 000 0101]
		//int 200 [0000 0001] [1111 0100] => byte[1 000 0011] [0 111 0100]
		ArrayList<Byte> list = new ArrayList<>();
		do{
			byte thisByte = (byte) (number % 128);
			thisByte = (byte)  (thisByte | 0x80);
			list.add(thisByte);
			number = number / 128;
		}while(number > 0);
		byte result[] = new byte[list.size()];
		for(int i = 0; i < list.size(); i++){
			result[i] = list.get(list.size() - 1 - i);
		}
		result[list.size() - 1] = (byte) (result[list.size() - 1] & 0x7F);
		return result;
	}
	
	//find all of the merge block files
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
	
	private int getSize( ArrayList<HeapIndexNode> nodes )
	{
		int sum = 0;
		for( int i = 0; i < nodes.size(); ++i )
			sum += nodes.get(i).getIndex().doc_num;
		return sum;
	}
}

