import java.io.*;
import java.util.*;

public class Main
{
	public static final boolean isDebugging = true;
	
	private static Set<Node> nodes;
	private static Set<NodeLocationData> nodeLocations;
	private static ArrayList<String> proposeBuffer;
	private static boolean isRunning;
	
	// Test case variables
	private static final int testTime = 50;
	public static boolean leaderFailureFlag = false;
	public static boolean casadingLeaderFailureFlag = false;
	public static boolean partialQuorumGatheringFlag = false;
	public static boolean slotSkippingFlag = false;
	
	public static void main(String[] args) throws IOException
	{
		writeDebug("Type 'help' for a list of commands");
		
		isRunning = false;
		nodes = new HashSet<Node>();
		proposeBuffer = new ArrayList<String>();
		nodeLocations = new HashSet<NodeLocationData>();
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while(true)
		{
			String[] s = in.readLine().split(" ", 2);
			String cmd = s[0];
			String arg = s.length > 1 ? s[1] : null;
			
			if(cmd.equalsIgnoreCase("init"))
				createNodes(Integer.parseInt(arg));
			
			else if(cmd.equalsIgnoreCase("start"))
			{
				if(arg == null)
					startAll();
				else
					start(Integer.parseInt(arg));
			}
			
			else if(cmd.equalsIgnoreCase("stop"))
			{
				if(arg == null)
					stopAll();
				else
					stop(Integer.parseInt(arg));
			}
			
			else if(cmd.equalsIgnoreCase("clear"))
				clearStableStorage();

			else if(cmd.equalsIgnoreCase("print"))
			{
				if(arg == null)
					printAll();
				else
					print(Integer.parseInt(arg));
			}
			
			else if(cmd.equalsIgnoreCase("test"))
			{
				if(arg == null)
					writeDebug("You must specify a test case. Type 'help' for a list of commands and allowed values.", true);
				else
				{
					if(arg.equalsIgnoreCase("leaderFail"))
						testLeaderFail();
					else if(arg.equalsIgnoreCase("cascadingLeaderFail"))
						testCascadingLeaderFail();
					else if(arg.equalsIgnoreCase("simultaneousFail"))
						testSimultaneousFail();
					else if(arg.equalsIgnoreCase("partialQuorumGathering"))
						testPartialQuorumGathering();
					else if(arg.equalsIgnoreCase("slotSkipping"))
						testSlotSkipping();
					else
						writeDebug("Unrecognized test case. Type 'help' for a list of commands and allowed values.", true);
				}
			}

			
			else if(cmd.equalsIgnoreCase("propose"))
				if(isRunning)
					propose(arg);
				else
					bufferPropose(arg);
			
			else if(cmd.equalsIgnoreCase("exit"))
				exit();
			
			else if(cmd.equalsIgnoreCase("help"))
			{
				String m = "";
				m += "List of valid commands:";
				m += "\n\tinit <num> - creates <num> nodes";
				m += "\n\tstart [<num>] - starts the node with the number <num>. If no number specified, all will start";
				m += "\n\tstop [<num>] - stops (or 'crashes') the node with the number <num>. If no number specified, all will stop";
				m += "\n\tprint [<num>] - prints the learned values from the node with the number <num>. If no number specified, all will printed";
				m += "\n\tclear - clears all nodes' stable storage";
				m += "\n\tpropose <value> - the current leader will propose <value>";
				m += "\n\ttest <value> - tests against a particular condition. Allowed values are 'leaderFail', 'cascadingLeaderFail', 'simultaneousFail', 'partialQuorumGathering', and 'slotSkipping'.";
				m += "\n\texit - stops all nodes and exits";
				m += "\n\thelp - displays this list";
				writeDebug("\n" + m + "\n");
			}
			
			else
				writeDebug("Unrecognized Command. Type 'help' for a list of commands", true);
		}
	}
	
	private static void propose(String s)
	{
		writeDebug("Proposing: " + s);
		for(Node node : nodes)
			if(node.isLeader())
			{
				node.propose(s);
				break;
			}
	}
	
	private static void bufferPropose(String s)
	{
		writeDebug("Buffering Proposal: " + s);
		proposeBuffer.add(s);
	}
	
	private static void startAll()
	{
		writeDebug("Starting all nodes...");

		for(Node node : nodes)
			node.start();
		
		while(proposeBuffer.size() > 0)
			propose(proposeBuffer.remove(0));
			
		isRunning = true;
		
		writeDebug("All nodes started");
	}
	
	private static void start(int n)
	{
		writeDebug("Starting node " + n);
		for(Node node : nodes)
			if(node.getLocationData().getNum() == n)
			{
				node.start();
				break;
			}
	}
	
	private static void stopAll()
	{
		writeDebug("Stopping all nodes...");

		for(Node node : nodes)
			node.stop();
		nodes.clear();
		nodeLocations.clear();
		isRunning = false;

		writeDebug("All nodes stopped");
	}
	
	private static void stop(int n)
	{
		writeDebug("Stopping node " + n);
		for(Node node : nodes)
			if(node.getLocationData().getNum() == n)
			{
				node.stop();
				break;
			}
	}
	
	private static void createNodes(int n)
	{
		stopAll();
		
		for(int i = 0; i < n; i++)
		{
			Node node = new Node(i);
			if(i == 0) // make 0 leader
				node.becomeLeader();
			nodes.add(node);
			nodeLocations.add(node.getLocationData());
		}
		
		// give node list to all nodes (statically)
		for(Node node : nodes)
			node.setNodeList(nodeLocations);
		
		writeDebug(n + " nodes created");
	}
	
	private static void clearStableStorage()
	{
		for(Node node : nodes)
			node.clearStableStorage();
		writeDebug("Stable Storage Cleared");
	}
	
	private static void printAll()
	{
		for(Node node : nodes)
			print(node.getLocationData().getNum());
	}
	
	private static void print(int n)
	{
		for(Node node : nodes)
			if(node.getLocationData().getNum() == n)
			{

				Map<Integer, String> values = node.getValues();
				
				String m = "List of values learned by " + node.getLocationData().getNum() + ": ";
				Iterator<Integer> iter = values.keySet().iterator();
				while(iter.hasNext())
				{
					int i = iter.next();
					m += "\n\t" + i + ": " + values.get(i);
				}

				writeDebug("\n" + m + "\n");				
				break;
			}
	}
	
	private static void testLeaderFail()
	{
		new Thread()
		{
			public void run()
			{
				// don't start timer until it's running
				while(!Main.isRunning)
					yield(); // so the while loop doesn't spin too much
				
				// fail timer
				long expireTime = System.currentTimeMillis() + testTime;
				boolean isRunning = true;
				while(isRunning)
				{
					if(expireTime < System.currentTimeMillis())
					{
						Main.stop(0);
						isRunning = false;
					}
					yield(); // so the while loop doesn't spin too much
				}
			}
		}.start();
	}
	
	private static void testCascadingLeaderFail()
	{
		// XXX: Implement
	}
	
	private static void testSimultaneousFail()
	{
		// XXX: Implement
	}
	
	private static void testPartialQuorumGathering()
	{
		// XXX: Implement
	}
	
	private static void testSlotSkipping()
	{
		// XXX: Implement
	}
	
	private static void exit()
	{
		stopAll();
		writeDebug("Exiting");
		System.exit(0);
	}
	
	private static void writeDebug(String s)
	{
		writeDebug(s, false);
	}
	
	private static void writeDebug(String s, boolean isError)
	{
		if(!isDebugging)
			return;
			
		PrintStream out = isError ? System.err : System.out;
		out.print("*** ");
		out.print(s);
		out.println(" ***");
	}
}
