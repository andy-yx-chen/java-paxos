public class PrepareResponseMessage extends Message
{
	private int csn;
	private int minPsn;
	private Proposal proposal;
	
	public PrepareResponseMessage(int csn, int minPsn, Proposal proposal)
	{
		this.proposal = proposal;
		this.minPsn = minPsn;
		this.csn = csn;
	}
	
	public Proposal getProposal()
	{
		return proposal;
	}
	
	public int getCsn()
	{
		return csn;
	}
	
	public int getMinPsn()
	{
		return minPsn;
	}
}
