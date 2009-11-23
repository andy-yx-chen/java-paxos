import java.io.*;
import java.net.*;
import java.util.*;

public class Node
{
	private static final boolean isDebugging = true;
	private static final int socketTimeout = 5000;
	
	// if a proposer doesn't hear back from a majority of acceptors, try again
	private static final int proposeTimeout = 10000;
	
	// this is a range so that all heartbeats won't happen simultaneously
	private static final int heartbeatDelayMin = 1000;
	private static final int heartbeatDelayMax = 2000;
	
	private static Integer nextPort = 37100;
	private Set<NodeLocationData> nodes = new HashSet<NodeLocationData>();
	private NodeLocationData locationData;
	private NodeListener listener;
	private NodeHeartbeat heartbeat;
	
	// Proposer Variables
	private int psn;
	private Proposal proposal;
	private int numAcceptRequests;
	private boolean hasProposed;
	private NodeReProposer reProposer;
	
	// Acceptor Variables
	private int minPsn;
	private Proposal maxAcceptedProposal;
	
	public Node(String host, int port, int psnSeed)
	{
		this.psn = psnSeed; // when used properly, this ensures unique PSNs.
		this.minPsn = -1; // haven't accepted anything yet
		this.locationData = new NodeLocationData(host, port, psnSeed);
	}
	
	public Node(int psnSeed)
	{
		this("localhost", nextPort++, psnSeed);
	}
	
	public void setNodeList(Set<NodeLocationData> s)
	{
		this.nodes = s;
	}
	
	public synchronized void start()
	{
		recoverStableStorage();
		
		listener = new NodeListener();
		listener.start();
		
		heartbeat = new NodeHeartbeat();
		heartbeat.start();
		
		writeDebug("Started");
	}

	public synchronized void stop()
	{
		if(listener != null)
			listener.kill();
		listener = null;
		
		if(heartbeat != null)
			heartbeat.kill();
		heartbeat = null;

		writeDebug("Stopped");
	}
	
	public void propose(String value)
	{
		if(reProposer != null)
			reProposer.kill();
		proposal = new Proposal(psn, value);
		numAcceptRequests = 0;
		hasProposed = false;
		broadcast(new PrepareRequestMessage(psn));
		psn += nodes.size();
		reProposer = new NodeReProposer();
	}
	
	private void broadcast(Message m)
	{
		m.setSender(locationData);
		for(NodeLocationData node : nodes)
		{
			// immediately deliver to self
			if(this.locationData == node)
				deliver(m);

			// send message
			else
				unicast(node, m);
		}
	}
	
	private void unicast(NodeLocationData node, Message m)
	{
		Socket socket = null;
		ObjectOutputStream out = null;
		m.setReciever(node);
		
		try
		{
			socket = new Socket(node.getHost(), node.getPort());
			socket.setSoTimeout(socketTimeout);
			out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(m);
			out.flush();
		}
		catch(SocketTimeoutException e)
		{
			// XXX: Implement what to do when Node crash detected.
		}
		catch(IOException e)
		{
			e.printStackTrace();
			writeDebug("IOException while trying to send message!", true);
		}
		finally
		{
			try
			{
				if(out != null)
					out.close();
				if(socket != null)
					socket.close();
			}
			catch(IOException e){}
		}
	}
	
	private synchronized void deliver(Message m)
	{
		if(m instanceof HeartbeatMessage)
		{
			writeDebug("Got Heartbeat from " + m.getSender());
		}
		else if(m instanceof PrepareRequestMessage) // Acceptor
		{
			PrepareRequestMessage prepareRequest = (PrepareRequestMessage)m;
			
			writeDebug("Got Prepare Request from " + prepareRequest.getSender() + ": " + prepareRequest.getPsn());

			// new minPsn
			minPsn = Math.max(prepareRequest.getPsn(), minPsn);
			
			// respond
			PrepareResponseMessage prepareResponse = new PrepareResponseMessage(maxAcceptedProposal, minPsn);
			prepareResponse.setSender(locationData);
			unicast(prepareRequest.getSender(), prepareResponse);
			
			updateStableStorage();
		}
		else if(m instanceof PrepareResponseMessage) // Proposer
		{
			PrepareResponseMessage prepareResponse = (PrepareResponseMessage)m;
			Proposal acceptedProposal = prepareResponse.getProposal();
			
			writeDebug("Got Prepare Response from " + prepareResponse.getSender() + ": " + (acceptedProposal == null ? "None" : acceptedProposal.toString()) + ", " + prepareResponse.getMinPsn());

			if(hasProposed) // ignore if already heard from a majority
				return;
						
			// if acceptors already accepted something higher, use it instead
			if(acceptedProposal != null && acceptedProposal.getPsn() > proposal.getPsn())
				proposal = acceptedProposal;
			
			// if acceptors already promised something higher, use higher psn
			if(prepareResponse.getMinPsn() > proposal.getPsn())
			{
				while(psn < prepareResponse.getMinPsn())
					psn += nodes.size();
				propose(proposal.getValue());
				return;
			}
			
			numAcceptRequests++;
			if(numAcceptRequests > (nodes.size() / 2)) // has heard from majority?
			{
				hasProposed = true;
				if(reProposer != null)
					reProposer.kill();
				AcceptRequestMessage acceptRequest = new AcceptRequestMessage(proposal);
				acceptRequest.setSender(locationData);
				broadcast(acceptRequest);
				// XXX: reProposer = new NodeReProposer(); // wait for AcceptResponseMessage (is one needed?)
			}
		}
		else if(m instanceof AcceptRequestMessage) // Acceptor
		{
			AcceptRequestMessage acceptRequest = (AcceptRequestMessage)m;
			Proposal requestedProposal = acceptRequest.getProposal();

			writeDebug("Got Accept Request from " + acceptRequest.getSender() + ": " + requestedProposal.toString());
			
			if(requestedProposal.getPsn() < minPsn)
				return; // ignore
			
			// "accept" the proposal
			minPsn = Math.max(requestedProposal.getPsn(), minPsn);
			maxAcceptedProposal = requestedProposal;
			
			// XXX: notify Learners?
			
			updateStableStorage();
		}
		else
			writeDebug("Unknown Message recieved", true);
	}
	
	public NodeLocationData getLocationData()
	{
		return locationData;
	}
		
	public String toString()
	{
		return locationData.toString();
	}
	
	private void writeDebug(String s)
	{
		writeDebug(s, false);
	}
	
	private synchronized void writeDebug(String s, boolean isError)
	{
		if(!isDebugging)
			return;
			
		PrintStream out = isError ? System.err : System.out;
		out.print(toString());
		out.print(": ");
		out.println(s);
	}
	
	private synchronized void recoverStableStorage()
	{
		
		ObjectInputStream in = null;
		try
		{
			File f = new File("stableStorage/" + toString() + ".bak"); 
			if(!f.exists())
			{
				writeDebug("No stable storage found");
				return;
			}
			in = new ObjectInputStream(new FileInputStream(f));
			NodeStableStorage stableStorage = (NodeStableStorage)in.readObject();
			minPsn = stableStorage.minPsn;
			maxAcceptedProposal = stableStorage.maxAcceptedProposal;
		}
		catch (IOException e)
		{
			writeDebug("Problem reading from stable storage!", true);
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			writeDebug("ClassNotFoundException while reading from stable storage!", true);
		}
		finally
		{
			try
			{
				if(in != null)
					in.close();
			}
			catch(IOException e){}
		}		
	}
	
	private synchronized void updateStableStorage()
	{
		NodeStableStorage stableStorage = new NodeStableStorage();
		stableStorage.minPsn = minPsn;
		stableStorage.maxAcceptedProposal = maxAcceptedProposal;
		
		ObjectOutputStream out = null;
		try
		{
			File dir = new File("stableStorage"); 
			if(!dir.exists())
				dir.mkdir();

			out = new ObjectOutputStream(new FileOutputStream("stableStorage/" + toString() + ".bak"));
			out.writeObject(stableStorage);
			out.flush();
		}
		catch (IOException e)
		{
			writeDebug("Problem writing to stable storage!", true);
		}
		finally
		{
			try
			{
				if(out != null)
					out.close();
			}
			catch(IOException e){}
		}
	}

	public synchronized void clearStableStorage()
	{
		File f = new File("stableStorage/" + toString() + ".bak"); 
		if(f.exists())
			f.delete();
	}
	
	private class NodeHeartbeat extends Thread
	{
		private boolean isRunning;
		private long lastHeartbeat;
		private Random rand;
		
		public NodeHeartbeat()
		{
			isRunning = true;
			lastHeartbeat = System.currentTimeMillis();
			rand = new Random();
		}
		
		public void run()
		{
			int heartbeatDelay = rand.nextInt(heartbeatDelayMax - heartbeatDelayMin) + heartbeatDelayMin;
			while(isRunning)
			{
				if(heartbeatDelay < System.currentTimeMillis() - lastHeartbeat)
				{
					broadcast(new HeartbeatMessage());
					lastHeartbeat = System.currentTimeMillis();
					heartbeatDelay = rand.nextInt(heartbeatDelayMax - heartbeatDelayMin) + heartbeatDelayMin;
				}
				yield(); // so the while loop doesn't spin too much
			}
		}
		
		public void kill()
		{
			isRunning = false;
		}
	}
	
	private class NodeListener extends Thread
	{
		private boolean isRunning;
		private ServerSocket serverSocket;
		
		public NodeListener()
		{
			isRunning = true;
			try
			{
				serverSocket = new ServerSocket(locationData.getPort());
			}
			catch(IOException e)
			{
				writeDebug("IOException while trying to listen!", true);
			}
		}
		
		public void run()
		{
			Socket socket;
			ObjectInputStream in;
			while(isRunning)
			{
				try
				{
					socket = serverSocket.accept();
					in = new ObjectInputStream(socket.getInputStream());
					deliver((Message)in.readObject());
				}
				catch(IOException e)
				{
					writeDebug("IOException while trying to accept connection!", true);
				}
				catch(ClassNotFoundException e)
				{
					writeDebug("ClassNotFoundException while trying to read Object!", true);
				}
			}
		}
		
		public void kill()
		{
			isRunning = false;
		}
	}
	
	private class NodeReProposer extends Thread
	{
		private boolean isRunning;
		private long expireTime;
		
		public NodeReProposer()
		{
			isRunning = true;
		}
		
		public void run()
		{
			expireTime = System.currentTimeMillis() + proposeTimeout;
			while(isRunning)
			{
				if(expireTime < System.currentTimeMillis())
				{
					propose(proposal.getValue());
					kill();
				}
				yield(); // so the while loop doesn't spin too much
			}
		}
		
		public void kill()
		{
			isRunning = false;
		}
		
	}
}
