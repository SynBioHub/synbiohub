package org.synbiohub;

public class BuildCombineArchiveResult extends Result {
	private boolean success;
	private String errorLog;
	private String resultFilename;
	
	public BuildCombineArchiveResult(
			BuildCombineArchiveJob job,
			boolean success,
			String resultFilename,
			String errorLog)
	{
		super(job);
		
		this.success = success;
		this.resultFilename = resultFilename;
		this.errorLog = errorLog;
	}
}
