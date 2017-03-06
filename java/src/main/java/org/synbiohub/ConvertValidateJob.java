package org.synbiohub;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.sbolstandard.core2.*;

public class ConvertValidateJob extends Job
{
	public String sbolFilename;
	public String uriPrefix;
	public boolean requireComplete;
	public boolean requireCompliant;
	public boolean enforceBestPractices;
	public boolean typesInURI;
	public String version;
	public boolean keepGoing;
	public String topLevelURI;
	
	public void execute()
	{
		File resultFile;
		
		try {
			resultFile = File.createTempFile("sbh_convert_validate", ".xml");
		} catch (IOException e) {
			finish(new ErrorResult(this, e));
			return;
		}
		
		String resultFilename = resultFile.getName();
		resultFile.delete();

		ByteArrayOutputStream logOutputStream = new ByteArrayOutputStream();
		ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();
		
		SBOLValidateSilent.validate(
				new PrintStream(logOutputStream),
				new PrintStream(errorOutputStream),
				sbolFilename,
				uriPrefix,
				requireComplete,
				requireCompliant, 
				enforceBestPractices,
				typesInURI,
				version,
				keepGoing,
				null,
				null, 
				sbolFilename,
				topLevelURI,
				false,
				false,
				false,
				resultFilename,
				false,
				true);
		
		String log = new String(logOutputStream.toByteArray(), StandardCharsets.UTF_8);
		String errorLog = new String(errorOutputStream.toByteArray(), StandardCharsets.UTF_8);
		
		finish(new ConvertValidateResult(this, true, resultFilename, log, errorLog));
	}
}
