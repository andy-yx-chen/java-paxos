import java.util.*;

public class Main
{
	public static void main(String[] args)
	{
		// create all nodes
		Node n1 = new Node();
		Node n2 = new Node();
		Node n3 = new Node();
		
		// create node list
		Set<Node> s = new HashSet<Node>();
		s.add(n1);
		s.add(n2);
		s.add(n3);
		
		// give node list to all nodes (statically)
		for(Node n : s)
			n.setNodeList(s);
		
		// start all nodes
		for(Node n : s)
			n.start();
	}
}
