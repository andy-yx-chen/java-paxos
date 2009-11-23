import java.io.Serializable;

public abstract class Message implements Serializable
{
	protected NodeLocationData sender;
	protected NodeLocationData reciever;
	
	public NodeLocationData getSender()
	{
		return sender;
	}

	public void setSender(NodeLocationData sender)
	{
		this.sender = sender;
	}

	public NodeLocationData getReciever()
	{
		return reciever;
	}
	
	public void setReciever(NodeLocationData reciever)
	{
		this.reciever = reciever;
	}
}
