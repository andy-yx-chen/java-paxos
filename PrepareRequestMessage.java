public class PrepareRequestMessage extends Message
{
	private int csn;
	private int psn;
	
	public PrepareRequestMessage(int csn, int psn)
	{
		this.csn = csn;
		this.psn = psn;
	}
	
	public int getPsn()
	{
		return psn;
	}
	
	public int getCsn()
	{
		return csn;
	}
}
