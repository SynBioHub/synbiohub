package org.synbiohub;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorResult extends Result
{
	String error;
	
	public ErrorResult(Job job, Exception e)
	{
		super(job);

		StringWriter stringWriter = new StringWriter();
		e.printStackTrace(new PrintWriter(stringWriter));

		error = stringWriter.toString();
	}

}
