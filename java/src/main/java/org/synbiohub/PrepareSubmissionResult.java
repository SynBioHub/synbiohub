package org.synbiohub;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PrepareSubmissionResult extends Result
{
	private boolean success;
	private String log;
	private String errorLog;
	private String resultFilename;
	private HashMap<String, String> attachmentFiles;
	private String extractDirPath;
	
	public PrepareSubmissionResult(
			PrepareSubmissionJob job,
			boolean success,
			String resultFilename,
			String log,
			String errorLog,
			HashMap<String, String> attachmentFiles,
			String extractDirPath)
	{
		super(job);
		
		this.success = success;
		this.resultFilename = resultFilename;
		this.log = log;
		this.errorLog = errorLog;
		this.attachmentFiles = attachmentFiles;
		this.extractDirPath = extractDirPath;
	}
}
