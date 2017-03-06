package org.synbiohub;

public class ErrorResult extends Result
{
	String message;
	
	ErrorResult(Job job, Exception error)
	{
		super(job);
		
		message = error.toString();
	}

}
