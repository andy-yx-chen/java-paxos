public class PrepareResponseMessage extends Message
{
	private int minPsn;
	private Proposal proposal;
	
	public PrepareResponseMessage(Proposal proposal, int minPsn)
	{
		this.proposal = proposal;
		this.minPsn = minPsn;
	}
	
	public Proposal getProposal()
	{
		return proposal;
	}
	
	public int getMinPsn()
	{
		return minPsn;
	}
}
