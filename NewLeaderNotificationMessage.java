public class NewLeaderNotificationMessage extends Message
{
	private int num;
	
	public NewLeaderNotificationMessage(int num)
	{
		this.num = num;
	}
	
	public int getNum()
	{
		return num;
	}
}
