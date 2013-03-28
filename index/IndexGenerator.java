package index;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class IndexGenerator {
	//HTML pages files which will be cleaned
	private ArrayList<File> originalDataFiles;
	//HTML pages files' according indexes
	private ArrayList<File> originalIndexFiles;
	//number of pages in each blocks
//	private final int MAX_NUMBER_OF_PAGES_PER_MERGESORT_BLOCK = 20000;
	//the directory of data
	private String dataFolder;
	
//	index map
//	private HashMap<String, HashMap>
	private HashMap<String, Integer> wordMap;
	private ArrayList<String> docIDList;
	private int lastDocID;
	
	
	public IndexGenerator(String dataFolder){
		this.dataFolder = dataFolder;
		originalDataFiles = new ArrayList<>();
		originalIndexFiles = new ArrayList<>();
		wordMap = new HashMap<String, Integer>();
		docIDList = new ArrayList<String>();
		lastDocID = 0;
	}
	
	public void beginIndex(){
		Date begin = new Date();
		
		//get original data and index files
		parseFilesName();
		System.out.println( "Find "+ this.getNumOfDataFile() +" Data Files." );
		System.out.println( "Find "+ this.getNumOfIndexFile() +" Index Files." );
		

		//generate intermediate blocks
		generateMergeSortFiles();
		
		//merge sort intermediate blocks
//		mergeSort();
		Date end = new Date();
		System.out.println("used " + (end.getTime() - begin.getTime()) / 1000 + " seconds");
	}
	
	private void generateMergeSortFiles(){
		int beforePagesCount = 0;
		System.out.println("clean page to mergsort pieces");

		//blocks generator
//		TempIndexGenerator tempIndexGenerator = new TempIndexGenerator();
//		int DataFilessize = this.originalDataFiles.size(); 
		for(int i = 0; i < this.originalDataFiles.size(); i++)
		{
			System.out.println("clean page block " + i + "/" + originalDataFiles.size()
//					+ " memory pages: " + tempIndexGenerator.getPageCount()
					+ " before pages: " + beforePagesCount);
//			//pages generator
//			PageGenerator generatorOfPagesBlock = 
//					new PageGenerator(originalDataFiles.get(i), originalIndexFiles.get(i));
//			PageParser pp = new PageParser( originalDataFiles.get(i), originalIndexFiles.get(i) );
			FileParser fp = new FileParser( originalDataFiles.get(i), originalIndexFiles.get(i), docIDList );
			HashMap<String, Integer> map = fp.parse();
			wordMap = ToolKit.mergeMap( wordMap, map);
			System.out.println( "Current Map Size : " + wordMap.size() );
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
//		System.out.println("store last mergSort block " + tempIndexGenerator.getPersistenceCount());
//		tempIndexGenerator.persist();
	}
	
	private void parseFilesName(){
		System.out.println("parse files name");
		File file = new File(this.dataFolder);
		findFiles(file);
	}
	
	private void findFiles(File thisFile){
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
	
//	private void mergeSort(){
//		MergeSortMachine mergeSortMachine = new MergeSortMachine();
//		mergeSortMachine.merge();
//	}
	public int getNumOfDataFile()
	{
		return this.originalDataFiles.size();
	}
	public int getNumOfIndexFile()
	{
		return this.originalIndexFiles.size();
	}
}
