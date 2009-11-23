import java.io.Serializable;

// For serialization reasons, this class is separate from the Node class
public class NodeLocationData implements Serializable
{
	private String host;
	private int port;
	private String num; // the "number" of the node. it's the seed given to the constructor. only used for debug
	
	public NodeLocationData(String host, int port, int num)
	{
		this.host = host;
		this.port = port;
		this.num = ((Integer)num).toString();
	}
	
	public String getHost()
	{
		return host;
	}
	
	public int getPort()
	{
		return port;
	}
	
	public String toString()
	{
		return num;
	}
}