import java.io.Serializable;
import java.util.Map;

//For serialization reasons, this class is separate from the Node class
public class NodeStableStorage implements Serializable
{
	public Map<Integer, Integer> minPsns;
	public Map<Integer, Proposal> maxAcceptedProposals;

}