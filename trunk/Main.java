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
		Set<Node> s = new HashSet<Node>();
		s.add(n1);
		s.add(n2);
		s.add(n3);
		
		for(Node n : s)
		{
			// give node list to all nodes (statically)
			n.setNodeList(s);
			
			// clear stable storage
			n.clearStableStorage();
		}
		
		// start all nodes
		for(Node n : s)
			n.start();
		
		// propose something
		n1.propose("Test");
	}
}
