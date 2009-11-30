import java.io.Serializable;

// For serialization reasons, this class is separate from the Node class
public class NodeLocationData implements Serializable
{
	private String host;
	private int port;
	private int num;
	private boolean isLeader;
	
	public NodeLocationData(String host, int port, int num)
	{
		this.host = host;
		this.port = port;
		this.num = num;
		this.isLeader = false;
	}
	
	public void becomeLeader()
	{
		isLeader = true;
	}
	
	public void becomeNonLeader()
	{
		isLeader = false;
	}
	
	public boolean isLeader()
	{
		return isLeader;
	}
	
	public String getHost()
	{
		return host;
	}
	
	public int getPort()
	{
		return port;
	}
	
	public int getNum()
	{
		return num;
	}
	
	public String toString()
	{
		return ((Integer)num).toString();
	}
}