package org.synbiohub;

public class ShutdownJob extends Job
{
	public void execute() throws Exception
	{
		finish(new ShutdownResult(this));
		
		System.exit(0);
	}

}
