package index;

import java.util.ArrayList;
import java.util.HashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class PageParser
{
	public String PageContent;
	public String m_urlindex;
	private ArrayList<String> m_docIDList;
	
	public PageParser( String content, String urlindex, ArrayList<String> docIDList )
	{
		this.PageContent = content;
		this.m_docIDList = docIDList;
		this.m_urlindex = urlindex; 
	}
	
	public HashMap<String, Integer> parse()
	{
		String[] words = parseWordsFromPage( PageContent );
//		System.out.println("before count words:" + words.length);
		HashMap<String, Integer> map = CountWords( words );
//		System.out.println("after count words:" + map.size());
		String[] idxseg = m_urlindex.split(" ");
		m_docIDList.add(idxseg[0]);
		return map;
	}
	
	
	private String[] parseWordsFromPage(String page){
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
	
	private boolean isLegalWord( String word )
	{
		for( int i = 0; i < word.length(); ++i )
		{
			if( !Character.isLetter(word.charAt(i)) && !Character.isDigit(word.charAt(i)) )
				return false;
		}
		return true;
	}
}