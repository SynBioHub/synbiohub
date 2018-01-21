package org.synbiohub;

import com.google.gson.*;

public class Job
{
	private static final Gson gson = new Gson();

	private transient boolean finished = false;
	
	public int id;	
	public String type;
	
	public static Job fromJSON(String json) throws UnknownJobTypeException
	{
		Job job = gson.fromJson(json, Job.class);

		if(job.type.equals("initialize"))
		{
			return gson.fromJson(json, InitializeJob.class);
		}
		else if(job.type.equals("shutdown"))
		{
			return gson.fromJson(json, ShutdownJob.class);
		}
		else if(job.type.equals("prepareSubmission"))
		{
			return gson.fromJson(json, PrepareSubmissionJob.class);
		}
		else if(job.type.equals("changeURIPrefix"))
		{
			return gson.fromJson(json, ChangeURIPrefixJob.class);
		}
		else if(job.type.equals("prepareSnapshot"))
		{
			return gson.fromJson(json, PrepareSnapshotJob.class);
		}
		else if(job.type.equals("rdfToSBOL"))
		{
			return gson.fromJson(json, RDFToSBOLJob.class);
		}
		else if(job.type.equals("convertToGenBank"))
		{
			return gson.fromJson(json, ConvertToGenBankJob.class);
		}
		else if(job.type.equals("cloneSubmission"))
		{
			return gson.fromJson(json, CloneSubmissionJob.class);
		}
		else if(job.type.equals("buildCombineArchive"))
		{
			return gson.fromJson(json, BuildCombineArchiveJob.class);
		}
		else
		{
			throw new UnknownJobTypeException();
		}
	}
	
	protected void finish(Result result)
	{
		assert(!finished);		
		finished = true;
		
		ProcessBridge.sendResult(result);
	}
	
	public void execute() throws Exception
	{
	}
}
