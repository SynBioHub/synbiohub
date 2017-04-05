package org.synbiohub;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.sbolstandard.core2.*;

import javax.xml.namespace.QName;

public class RDFToSBOLJob extends Job
{
	public String sbolFilename;
	public String uriPrefix;
	public boolean requireComplete;
	public boolean requireCompliant;
	public boolean enforceBestPractices;
	public boolean typesInURI;
	public String version;
	public boolean keepGoing;

	public void execute() throws Exception
	{
		ByteArrayOutputStream logOutputStream = new ByteArrayOutputStream();
		ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();
		
		SBOLDocument doc = SBOLValidateSilent.validate(
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
				"",
				false,
				false,
				false,
				null,
				false,
				true);
		
		String log = new String(logOutputStream.toByteArray(), StandardCharsets.UTF_8);
		String errorLog = new String(errorOutputStream.toByteArray(), StandardCharsets.UTF_8);

		if(errorLog.length() > 0)
		{
			finish(new RDFToSBOLResult(this, false, "", log, errorLog));
			return;
		}

		File resultFile = File.createTempFile("sbh_rdf_to_sbol", ".xml");

		SBOLWriter writer = new SBOLWriter();
		writer.write(doc, resultFile);

		finish(new RDFToSBOLResult(this, true, resultFile.getAbsolutePath(), log, errorLog));

	}
}
