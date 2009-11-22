import java.io.Serializable;

public abstract class Message implements Serializable
{
	protected String sender;
	protected String reciever;
	
	public String getSender()
	{
		return sender;
	}

	public void setSender(Node sender)
	{
		this.sender = sender.toString();
	}

	public String getReciever()
	{
		return reciever;
	}

	public void setReciever(Node reciever)
	{
		this.reciever = reciever.toString();
	}
}
