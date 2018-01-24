package org.synbiohub;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.sbolstandard.core2.*;

import javax.xml.namespace.QName;

public class PrepareSnapshotJob extends Job
{
	public String sbolFilename;
	public boolean requireComplete;
	public boolean requireCompliant;
	public boolean enforceBestPractices;
	public boolean typesInURI;
	public String uriPrefix;
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
				"",
				requireComplete,
				requireCompliant, 
				enforceBestPractices,
				typesInURI,
				null,
				keepGoing,
				"",
				"",
				sbolFilename,
				topLevelURI,
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
			finish(new PrepareSnapshotResult(this, false, "", log, errorLog));
			return;
		}

        doc = doc.changeURIPrefixVersion(uriPrefix, null, version);

        (new IdentifiedVisitor() {

            @Override
            public void visit(Identified identified,TopLevel topLevel) {

                try {

                    identified.createAnnotation(
                            new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "snapshotOf", "sbh"),
                            new URI(identified.getPersistentIdentity().toString() + "/current"));

                } catch (SBOLValidationException | URISyntaxException e) {


                }

            }

        }).visitDocument(doc);

		File resultFile = File.createTempFile("sbh_convert_validate", ".xml");

		SBOLWriter.write(doc, resultFile);

		finish(new PrepareSnapshotResult(this, true, resultFile.getAbsolutePath(), log, errorLog));

	}
}
