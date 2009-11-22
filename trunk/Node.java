import java.io.*;
import java.net.*;
import java.util.*;

public class Node
{
	private static final boolean isDebugging = true;
	private static final int socketTimeout = 5000;
	private static final int heartbeatDelay = 1000;
	
	private static Integer nextPort = 37100;
	private Set<Node> nodes = new HashSet<Node>();
	
	private NodeListener listener;
	private NodeHeartbeat heartbeat;
	private String host;
	private int port;
	
	public Node(String host, int port)
	{
		this.host = host;
		this.port = port;
	}
	
	public Node()
	{
		this("localhost", nextPort++);
	}
	
	public void setNodeList(Set<Node> s)
	{
		this.nodes = s;
	}
	
	public synchronized void start()
	{
		stop();
		
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
	
	private void broadcast(Message m)
	{
		m.setSender(this);
		Socket socket = null;
		ObjectOutputStream out = null;
		for(Node node : nodes)
		{
			// skip self
			if(this == node)
				continue;
			
			m.setReciever(node);
			
			try
			{
				socket = new Socket(node.host, node.port);
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
	}
	
	private void deliver(Message m)
	{
		if(m instanceof HeartbeatMessage)
		{
			writeDebug("Got Heartbeat from " + m.getSender());
		}
		else
			writeDebug("Unknown Message recieved", true);
	}
	
	private void writeDebug(String s)
	{
		writeDebug(s, false);
	}
	
	public String toString()
	{
		return host + ':' + port;
	}
	
	private synchronized void writeDebug(String s, boolean isError)
	{
		PrintStream out = isError ? System.err : System.out;
		
		if(isDebugging)
		{
			out.print(toString());
			out.print(":\t");
			out.println(s);
		}
	}
	
	private class NodeHeartbeat extends Thread
	{
		private boolean isRunning;
		private long lastHeartbeat;
		
		public NodeHeartbeat()
		{
			isRunning = true;
			lastHeartbeat = System.currentTimeMillis();
		}
		
		public void run()
		{
			while(isRunning)
			{
				if(heartbeatDelay < System.currentTimeMillis() - lastHeartbeat)
				{
					broadcast(new HeartbeatMessage());
					lastHeartbeat = System.currentTimeMillis();
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
				serverSocket = new ServerSocket(port);
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
}
