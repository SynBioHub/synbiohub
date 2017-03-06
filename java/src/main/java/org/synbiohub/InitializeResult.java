package org.synbiohub;

public class InitializeResult extends Result
{
	String version;
	
	public InitializeResult(InitializeJob job, String version)
	{
		super(job);
		
		this.version = version;
	}

}
