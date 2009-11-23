public class AcceptRequestMessage extends Message
{
	private Proposal proposal;
	
	public AcceptRequestMessage(Proposal proposal)
	{
		this.proposal = proposal;
	}
	
	public Proposal getProposal()
	{
		return proposal;
	}
}
