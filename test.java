import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class test
{
	public static void main( String[] argv )
	{
		test t = new test();
		String page = " <html> 1?ch 00:36:40 daughter's ¹þ¹þ <_>  hbo </html>";
		String[] words = t.parseWordsFromPage( page );
		for( int i = 0; i < words.length; ++i )
		{
			System.out.print(words[i]);
			boolean legal = isLegalWord( words[i]);
			System.out.print(" ");
			System.out.print( legal );
			System.out.print(" ");
		}
	}
	
	private String[] parseWordsFromPage(String page){
		Document doc = Jsoup.parse(page);
		String[] words = doc.text().split(" ");
		// clean the non-letter words
		return words;
	}
	
	private static boolean isLegalWord( String word )
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