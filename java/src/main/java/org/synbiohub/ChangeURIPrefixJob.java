package org.synbiohub;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.SBOLWriter;

public class ChangeURIPrefixJob extends Job
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

	public void execute() throws Exception
	{
		ByteArrayOutputStream logOutputStream = new ByteArrayOutputStream();
		ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();
		
		SBOLDocument doc = SBOLValidate.validate(
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
				"",
				"",
				sbolFilename,
				topLevelURI,
				false,
				false,
				false,
				false,
				null,
				false,
				true,
				false);
		
		String log = new String(logOutputStream.toByteArray(), StandardCharsets.UTF_8);
		String errorLog = new String(errorOutputStream.toByteArray(), StandardCharsets.UTF_8);

		if(doc == null)
		{
			finish(new ChangeURIPrefixResult(this, false, "", log, errorLog));
			return;
		}

		File resultFile = File.createTempFile("sbh_convert_validate", ".xml");

		SBOLWriter.write(doc, resultFile);

		finish(new ChangeURIPrefixResult(this, true, resultFile.getAbsolutePath(), log, errorLog));

	}
}
