import java.util.*;

public class Main
{
	public static void main(String[] args)
	{
		// create all nodes
		Node n1 = new Node(1);
		Node n2 = new Node(2);
		Node n3 = new Node(3);
		
		// create node list
		Set<Node> nodes = new HashSet<Node>();
		nodes.add(n1);
		nodes.add(n2);
		nodes.add(n3);
		
		// create node location list
		Set<NodeLocationData> nodeLocations = new HashSet<NodeLocationData>();
		for(Node node : nodes)
			nodeLocations.add(node.getLocationData());
		
		for(Node node : nodes)
		{
			// give node list to all nodes (statically)
			node.setNodeList(nodeLocations);
			
			// clear stable storage
			node.clearStableStorage();
		}
		
		// start all nodes
		for(Node node : nodes)
			node.start();
		
		// propose something
		n1.propose("Test");
		
		// XXX: Make all test cases
	}
}
