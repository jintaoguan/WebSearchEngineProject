package index;

public class EnvConfig
{
	private int MaxSpace;
	private int AvalaibleSpace;
	private String dataFolder;
	
	private String DataFolderPath;
	
	public void setMaxSpace( int num )
	{
		this.MaxSpace = num;
		this.AvalaibleSpace = (int) (this.MaxSpace * 0.8);
		this.dataFolder = "";
	}
	
	public int getAvalaibleSpace()
	{
		return this.AvalaibleSpace;
	}
	
	public String getDataFolderPath()
	{
		return this.DataFolderPath;
	}
	
	public void setDataFolderPath( String path )
	{
		this.DataFolderPath = path;
	}
}