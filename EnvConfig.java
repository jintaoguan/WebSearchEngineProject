public class EnvConfig
{
	private int MaxSpace;
	private int AvalaibleSpace;
	
	public void setMaxSpace( int num )
	{
		this.MaxSpace = num;
		this.AvalaibleSpace = (int) (this.MaxSpace * 0.8);
	}
	
	public int getAvalaibleSpace()
	{
		return this.AvalaibleSpace;
	}
}