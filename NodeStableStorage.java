import java.io.Serializable;

//For serialization reasons, this class is separate from the Node class
public class NodeStableStorage implements Serializable
{
	public int minPsn;
	public Proposal maxAcceptedProposal; 
}