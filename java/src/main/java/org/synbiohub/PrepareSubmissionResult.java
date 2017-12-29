package org.synbiohub;
import java.util.ArrayList;

public class PrepareSubmissionResult extends Result
{
	private boolean success;
	private String log;
	private String errorLog;
	private String resultFilename;
	private ArrayList<String> attachmentFiles;
	private ArrayList<String> sbmlFiles;
	
	public PrepareSubmissionResult(
			PrepareSubmissionJob job,
			boolean success,
			String resultFilename,
			String log,
			String errorLog,
			ArrayList<String> attachmentFiles,
			ArrayList<String> sbmlFiles)
	{
		super(job);
		
		this.success = success;
		this.resultFilename = resultFilename;
		this.log = log;
		this.errorLog = errorLog;
		this.attachmentFiles = attachmentFiles;
		this.sbmlFiles = sbmlFiles;
	}
}
