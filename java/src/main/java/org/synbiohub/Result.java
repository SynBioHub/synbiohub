package org.synbiohub;

public class Result
{
	public int jobId;
	
	public Result(Job job)
	{
		jobId = job.id;
	}
}
