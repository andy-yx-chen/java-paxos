public class PrepareResponseMessage extends Message
{
	private Proposal proposal;
	
	public PrepareResponseMessage(Proposal proposal)
	{
		this.proposal = proposal;
	}
	
	public Proposal getProposal()
	{
		return proposal;
	}
}
