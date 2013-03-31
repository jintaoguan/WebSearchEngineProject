import index.EnvConfig;
import index.IndexGenerator;
import query.Query;

public class Main
{
	public static void main( String[] argv )
	{
//		String filename = "/home/jintaoguan/Desktop/vol_0_99.tar";
//		String destDir = "/home/jintaoguan/Desktop/INDEX/";
//		GZip gz = new GZip( filename );
//		GZip.readTarFile("/home/jintaoguan/Desktop/1_data");
//		GZip.unTargzFile( filename, destDir);
//		System.out.println("ok");
		
		EnvConfig ec = new EnvConfig();
		ec.setMaxSpace( (int) java.lang.Runtime.getRuntime().maxMemory() );
		System.out.println("maxMemory " + java.lang.Runtime.getRuntime().maxMemory());
		//begin index with data folder
		IndexGenerator index = new IndexGenerator("D:/Work/NZ_data/");
		index.beginIndex();
//		Query query = new Query();
//		query.searchKeyWord();
	}
}