public class AcceptNotificationMessage extends Message
{
	private Proposal proposal;
	
	public AcceptNotificationMessage(Proposal proposal)
	{
		this.proposal = proposal;
	}
	
	public Proposal getProposal()
	{
		return proposal;
	}
}
