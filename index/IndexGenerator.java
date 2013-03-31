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
	private ArrayList<String> docIDList;
	//50000
	private final int MAX_PAGES_PER_MERGESORT_BLOCK = 50000;

	private File wordOffsetFile;
	private File indexFile;
	
	public IndexGenerator( String dataFolder )
	{
		this.dataFolder = dataFolder;
		this.originalDataFiles = new ArrayList<>();
		this.originalIndexFiles = new ArrayList<>();
		this.indexMap = new TreeMap<String, IndexEntry>();
		this.docIDList = new ArrayList<String>();
		this.mergeFiles = new ArrayList<File>();

		this.docIDFile = new File("D:/Work/NZ_data/docID/docID.txt");
		this.lastDocID = 0;
		
		this.wordOffsetFile = new File( "D:/Work/NZ_data/index/WordOffset.txt" );
		this.indexFile = new File( "D:/Work/NZ_data/index/index.txt" );
		
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
	
	private void generateMergeSortFiles()
	{
		System.out.println("clean page to mergsort pieces");

		for(int i = 0; i < this.originalDataFiles.size(); i++)
		{
			System.out.println("clean page block " + i + "/" + originalDataFiles.size() );
//					+ " memory pages: " + tempIndexGenerator.getPageCount()
//			
//			PageGenerator generatorOfPagesBlock = 
//					new PageGenerator(originalDataFiles.get(i), originalIndexFiles.get(i));
//			PageParser pp = new PageParser( originalDataFiles.get(i), originalIndexFiles.get(i) );
			
			//File Parser
			FileParser fp = new FileParser( originalDataFiles.get(i), originalIndexFiles.get(i), docIDList, lastDocID );
			TreeMap<String,IndexEntry> map = fp.parse();
			
			indexMap = ToolKit.mergeTreeMap( indexMap, map );
			System.out.println( "Current Map Size : " + indexMap.size() );
			System.out.println( "Have indexed " + (this.lastDocID + docIDList.size()) + " web pages." );
//			System.out.println("maxMemory " + java.lang.Runtime.getRuntime().maxMemory());
			
			if( docIDList.size() >  MAX_PAGES_PER_MERGESORT_BLOCK )
				flushMergeFile();
			
//			System.out.println( " keyword \"the\" : " + wordMap.get("bible") );
			
//			while(generatorOfPagesBlock.parseNext()){
//				tempIndexGenerator.clean(generatorOfPagesBlock.getPage(), generatorOfPagesBlock.getUrl());
//				//persist each block when accumulate to a specific number of pages
//				if(tempIndexGenerator.getPageCount() >= MAX_NUMBER_OF_PAGES_PER_MERGESORT_BLOCK){
//					System.out.println("store mergSort block " + tempIndexGenerator.getPersistenceCount());
//					beforePagesCount += tempIndexGenerator.getPageCount();
//					tempIndexGenerator.persist();
//				}
//			}
		}
		flushMergeFile();
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
	
	private void WriteDocIDListToFile()
	{
		this.lastDocID = this.lastDocID + this.docIDList.size();
		try {
			FileWriter fw = new FileWriter( this.docIDFile, true );
			Iterator<String> iter = docIDList.iterator();
			StringBuffer sb = new StringBuffer();
			while( iter.hasNext() )
			{
				String url = iter.next();
				sb.append( url );
				sb.append("\n");
			}
			fw.append(sb.toString());
			fw.close();
		}catch (IOException e){
			e.printStackTrace();
			System.out.println("WriteDocIDListToFile() Error.");
		}
	}
	
	private void mergeSort(){
//		MergeSortMachine mergeSortMachine = new MergeSortMachine();
//		mergeSortMachine.merge();
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
			int CurrentOffset = 1;
			while( (indexLine = br.readLine()) != null ) 
			{
				String word = getKeyWordFromIndexLine( indexLine );
				sb.append( word + " " + CurrentOffset + " " + indexLine.length() + '\n' );
				CurrentOffset = CurrentOffset + indexLine.length() + 1;
				if( sb.length() > 5 )
					flushOffsetFile( writer, sb );
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
	
	//for test
	public int getNumOfDataFile()
	{
		return this.originalDataFiles.size();
	}
	public int getNumOfIndexFile()
	{
		return this.originalIndexFiles.size();
	}
}
