package org.synbiohub;

public class InitializeJob extends Job
{
	public void execute()
	{
		finish(new InitializeResult(this, System.getProperty("java.version")));
	}

}
