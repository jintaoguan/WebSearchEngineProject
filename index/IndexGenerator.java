package index;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class IndexGenerator 
{
	//HTML pages files which will be cleaned
	private ArrayList<File> originalDataFiles;
	//HTML pages files' according indexes
	private ArrayList<File> originalIndexFiles;
	//mergeFiles
	private ArrayList<File> mergeFiles;
	//
	private File docIDFile;
	//the directory of data
	private String dataFolder;
	
	private int lastDocID;
	
//	private EnvConfig config;
	
	//the most important thing, index map, need to be cleaned after flushing
	private TreeMap<String, IndexEntry> indexMap;
	
	//list of URL address, need to be cleaned after flushing
	private ArrayList<PageInfo> docIDList;
	
	//50000
	private final int MAX_PAGES_PER_MERGESORT_BLOCK = 50000;
	
	private final int BUFFER_SIZE = 10 * 1000 * 1000;

	private File wordOffsetFile;
	private File indexFile;
	
	public IndexGenerator( String dataFolder )
	{
		//working folder setting
		this.dataFolder = dataFolder;
		//data files list
		this.originalDataFiles = new ArrayList<>();
		//index files list
		this.originalIndexFiles = new ArrayList<>();
		//index map< String, IndexEntry >
		this.indexMap = new TreeMap<String, IndexEntry>();
		//docIDList is an array that stores url in it 
		this.docIDList = new ArrayList<PageInfo>();
		//merge block files
		this.mergeFiles = new ArrayList<File>();
		//2.5 million url
		this.docIDFile = new File("D:/Work/NZ_data/docID/docID.txt");

		this.lastDocID = 0;
		//offset of key word in indexing file
		this.wordOffsetFile = new File( "D:/Work/NZ_data/index/WordOffset.txt" );
		//indexing file
		this.indexFile = new File( "D:/Work/NZ_data/index/final_idx.txt" );
		
	}
	
	public void beginIndex()
	{
		Date begin = new Date();
		
		//get original data and index files
		parseFilesName();
		System.out.println( "Find "+ this.getNumOfDataFile() +" Data Files." );
		System.out.println( "Find "+ this.getNumOfIndexFile() +" Index Files." );
		
		//generate intermediate blocks
		generateMergeSortFiles();
		
		//merge sort all of the blocks
//		mergeSort();
		
		//generate index word offset file
//		generateWordOffsetFile();
		
		Date end = new Date();
		System.out.println("used " + (end.getTime() - begin.getTime()) / 1000 + " seconds");
	}
	
	//generate merge blocks from data set
	private void generateMergeSortFiles()
	{
		System.out.println("clean page to mergsort pieces");
		for(int i = 0; i < this.originalDataFiles.size(); i++)
		{
			System.out.println("Processing " + i + "/" + originalDataFiles.size() + " Data File" );
			
			//File Parser
			FileParser fp = new FileParser( originalDataFiles.get(i), originalIndexFiles.get(i), docIDList, lastDocID );
			TreeMap<String,IndexEntry> map = fp.parse();
			
			indexMap = ToolKit.mergeTreeMap( indexMap, map );
			System.out.println( "Current Map Size : " + indexMap.size() );
			System.out.println( "Have indexed " + (this.lastDocID + docIDList.size()) + " web pages." );
//			System.out.println("maxMemory " + java.lang.Runtime.getRuntime().maxMemory());
			
			if( docIDList.size() >  MAX_PAGES_PER_MERGESORT_BLOCK )
				flushMergeFile();
		
		}
		flushMergeFile();
		
		//Have processed all of the data file
		System.out.println("******************************************************");
		System.out.println("     Store last mergeSort block ");
		System.out.println("     Total mergeSort blocks: " + this.mergeFiles.size() );
		System.out.println("******************************************************");
		
	}
	
	private void parseFilesName()
	{
		System.out.println("parse files name");
		File file = new File(this.dataFolder);
		findFiles(file);
	}
	
	private void findFiles(File thisFile)
	{
		if(!thisFile.isDirectory()){
			if(thisFile.getName().contains("data")) 
				this.originalDataFiles.add(thisFile);
			else if(thisFile.getName().contains("index")) 
				this.originalIndexFiles.add(thisFile);
			return;
		}
		String[] subFiles = thisFile.list();
		Arrays.sort(subFiles);
		for(String name : subFiles){
			findFiles(new File(thisFile, name));
		}
	}
	
	private void flushMergeFile()
	{
		WriteMapToFile( mergeFiles.size() );
		WriteDocIDListToFile();
		CleanMemory();
	}
	
	private void CleanMemory()
	{
		this.docIDList.clear();
		this.indexMap.clear();
	}
	
	private void WriteMapToFile( int numOfFile )
	{
		System.out.println("Writing map to file ...");

		try {
			String outputFileName = "D:/Work/NZ_data/merge_files/merge_file_";
			outputFileName = outputFileName + numOfFile + ".txt";
			File f = new File( outputFileName );
			
			if( !f.exists() ) f.createNewFile();
			//add this merge file to mergeFiles arraylist
			this.mergeFiles.add(f);
			
			FileWriter fw = new FileWriter(f);
			StringBuffer sb = new StringBuffer();
			
			Iterator<Entry<String, IndexEntry>> iter = this.indexMap.entrySet().iterator(); 
			while( iter.hasNext() ) 
			{ 
				Map.Entry<String, IndexEntry> entry = (Map.Entry<String, IndexEntry>) iter.next(); 
				String key = entry.getKey(); 
				IndexEntry val = entry.getValue();
				
				sb.append( key + " " );
				sb.append( val.doc_num + " " );
				
				
				Iterator<DocFreqPair> itr = val.indexList.iterator();
				while (itr.hasNext())
				{
					DocFreqPair pair = itr.next();
					sb.append( pair.getDocID() + " " );
					sb.append( pair.getFreq() + " " );
				}
				fw.write(sb.toString());
				fw.write("\n");
				sb.delete( 0, sb.length() - 1 );
			}
			fw.close();
			System.out.println("Writing completed.");
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("WriteMapToFile() Error.");
		}
	}
	
	//write url and info into DocID file
	private void WriteDocIDListToFile()
	{
		this.lastDocID = this.lastDocID + this.docIDList.size();
		try {
			FileWriter fw = new FileWriter( this.docIDFile, true );
			Iterator<PageInfo> iter = docIDList.iterator();
			StringBuffer sb = new StringBuffer();
			while( iter.hasNext() )
			{
				PageInfo info = iter.next();
				sb.append( info.pageInfoToString() );
				sb.append("\n");
			}
			fw.append(sb.toString());
			fw.close();
		}catch (IOException e){
			e.printStackTrace();
			System.out.println("WriteDocIDListToFile() Error.");
		}
	}
	
	private void mergeSort()
	{
		MergeSortMachine mergeSortMachine = new MergeSortMachine();
		mergeSortMachine.merge();
	}
	
	
	//generate the WordOffsetFile
	private void generateWordOffsetFile()
	{
//		File file = new File( "D:/Work/NZ_data/index/tmp_index.txt" );
		FileReader reader = null;
		FileWriter writer = null;
		try {
			reader = new FileReader( this.indexFile );
			BufferedReader br = new BufferedReader(reader);
			
			//show and record the size of the index file
			long fileSize = indexFile.length();
			System.out.println( "Index File Size: " + fileSize );
			
			writer = new FileWriter( this.wordOffsetFile, true );
			StringBuffer sb = new StringBuffer();
 			
			String indexLine = null;
			long CurrentOffset = 1;
			while( (indexLine = br.readLine()) != null ) 
			{
				String word = getKeyWordFromIndexLine( indexLine );
				sb.append( word + " " + CurrentOffset + " " + indexLine.length() + '\n' );
				CurrentOffset = CurrentOffset + indexLine.length() + 1;
				if( sb.length() > this.BUFFER_SIZE )
				{
					System.out.println("Write word offset to file...");
					flushOffsetFile( writer, sb );
					System.out.println("Write complete.");
				}
			}
			
			flushOffsetFile( writer, sb );
			br.close();
			reader.close();
			writer.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println( "WordOffsetFile Not Found." );
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println( "BufferReader Error." );
		}
	}
	
	private void cleanIndexFile()
	{
		String filepath = "D:/Work/NZ_data/index/new_index.txt";
		File newfile = new File( filepath );
	}
	
	private void flushOffsetFile( FileWriter writer, StringBuffer sb )
	{
		try {
			writer.write( sb.toString() );
			sb.delete( 0, sb.length() );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getKeyWordFromIndexLine( String line )
	{
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		for( int i = 0; i < line.length(); ++i )
		{
			char currentChar = line.charAt(i);
			if( (currentChar != ' ') && (found == true) )
				sb.append(currentChar);
			else if( (currentChar != ' ') && (found == false) )
			{
				sb.append(currentChar);
				found = true;
			}
			else if( (currentChar == ' ') && (found == true) )
				break;
		}
		return sb.toString();
	}
	
	
	
	//for test use
	public int getNumOfDataFile()
	{
		return this.originalDataFiles.size();
	}
	public int getNumOfIndexFile()
	{
		return this.originalIndexFiles.size();
	}
}
