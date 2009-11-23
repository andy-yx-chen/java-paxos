import java.io.Serializable;

public class Proposal implements Serializable
{
	private int psn;
	private String value;
	
	public Proposal(int psn, String value)
	{
		this.psn = psn;
		this.value = value;
	}
	
	public int getPsn()
	{
		return psn;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public String toString()
	{
		return "{" + psn + ", " + value + "}";
	}
}
