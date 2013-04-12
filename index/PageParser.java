package index;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class PageParser
{
	private File m_DataFile;
	
	public String m_PageContent;
	public String m_urlindex;
	private ArrayList<PageInfo> m_docIDList;
	
	private int m_CurrentOffset;
	
	private String m_DataFileID;
	
	public PageParser( String content, String urlindex, int Offset, File data_file, ArrayList<PageInfo> docIDList )
	{
		this.m_PageContent = content;
		this.m_docIDList = docIDList;
		this.m_urlindex = urlindex;
		this.m_DataFile = data_file;
		this.m_CurrentOffset = Offset;
		this.m_DataFileID = getDataFileID( data_file.getName() );
	}
	
	public HashMap<String, Integer> parse()
	{
		String[] words = parseWordsFromPage( m_PageContent );
//		System.out.println("before count words:" + words.length);
		HashMap<String, Integer> map = CountWords( words );
//		System.out.println("after count words:" + map.size());
//		String[] idxseg = m_urlindex.split(" ");
		
		PageInfo info = new PageInfo( m_urlindex );
		info.setPageOffset( this.m_CurrentOffset );
		info.setFileID( this.m_DataFileID);
		info.setPageLength( this.m_PageContent.length() );
		int word_count = words.length;
		info.setWordCount(word_count);
		
		m_docIDList.add(info);
		return map;
	}
	
	
	private String[] parseWordsFromPage(String page){
//		page = page.toLowerCase();
		Document doc = Jsoup.parse(page);
		String[] words = doc.text().split(" ");
		// clean the non-letter words
		return words;
	}
	
	private HashMap<String, Integer> CountWords( String[] words )
	{
		HashMap<String,Integer> map = new HashMap<String, Integer>();
		for( int i = 0; i < words.length; ++i )
		{
			if( map.containsKey(words[i]) )
				map.put(words[i], map.get(words[i])+1);
			else if( isLegalWord(words[i]) )
				map.put(words[i], 1);
		}
		return map;
	}
	
	private String getDataFileID( String filename )
	{
		StringBuffer sb = new StringBuffer();
		for( int i = 0; i < filename.length() - 1; ++i )
		{
			if( filename.charAt(i) >= '0' && filename.charAt(i) <= '9' )
				sb.append( filename.charAt(i) );
		}
		return sb.toString();
	}
	
	private boolean isLegalWord( String word )
	{
		for( int i = 0; i < word.length(); ++i )
		{
			int asc = (int)(word.charAt(i));
			if( asc >= 97 && asc <= 122 )
				continue;
			else if( asc >= 65 && asc <= 90 )
				continue;
			else if( asc >= 48 && asc <= 57 )
				continue;
			else 
				return false;
		}
		return true;
	}
}