public class PrepareRequestMessage extends Message
{
	private int psn;
	
	public PrepareRequestMessage(int psn)
	{
		this.psn = psn;
	}
	
	public int getPsn()
	{
		return psn;
	}
}
