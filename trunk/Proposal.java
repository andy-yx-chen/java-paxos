import java.io.Serializable;

public class Proposal implements Serializable
{
	private int csn;
	private int psn;
	private String value;
	
	public Proposal(int csn, int psn, String value)
	{
		this.csn = csn;
		this.psn = psn;
		this.value = value;
	}
	
	public int getCsn()
	{
		return csn;
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
		return "{" + csn + ", " + psn + ", " + value + "}";
	}
}
