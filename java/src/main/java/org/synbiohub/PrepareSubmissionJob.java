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

public class PrepareSubmissionJob extends Job
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


	public String newRootCollectionDisplayId;
	public String ownedByURI;
	public String creatorName;
	public String name;
	public String description;
	public ArrayList<Integer> citationPubmedIDs;
	public ArrayList<String> keywords;

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
				topLevelURI,
				false,
				false,
				false,
				null,
				false,
				true);
		
		String log = new String(logOutputStream.toByteArray(), StandardCharsets.UTF_8);
		String errorLog = new String(errorOutputStream.toByteArray(), StandardCharsets.UTF_8);

		if(doc == null)
		{
			finish(new PrepareSubmissionResult(this, false, "", log, errorLog));
			return;
		}

		Collection submissionCollection = doc.createCollection(newRootCollectionDisplayId);
		System.err.println("New collection: " + submissionCollection.getIdentity().toString());

		submissionCollection.createAnnotation(
				new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "ownedBy", "sbh"),
				new URI(ownedByURI));

		submissionCollection.createAnnotation(
				new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"),
				creatorName);

		submissionCollection.createAnnotation(
				new QName("http://purl.org/dc/terms/", "created", "dcterms"),
				ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));

		submissionCollection.setName(name);
		submissionCollection.setDescription(description);

		for(String keyword : keywords)
		{
			submissionCollection.createAnnotation(
					new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "keyword", "sbh"),
					keyword);
		}

		for(int pubmedID : citationPubmedIDs)
		{
			submissionCollection.createAnnotation(
					new QName("http://purl.obolibrary.org/obo/", "OBI_0001617", "purl"),
					Integer.toString(pubmedID));
		}

		for(TopLevel topLevel : doc.getTopLevels())
		{
			topLevel.createAnnotation(
					new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "ownedBy", "sbh"),
					new URI(ownedByURI));

			if(topLevel != submissionCollection)
				submissionCollection.addMember(topLevel.getIdentity());
		}


		File resultFile = File.createTempFile("sbh_convert_validate", ".xml");

		SBOLWriter writer = new SBOLWriter();
		writer.write(doc, resultFile);

		finish(new PrepareSubmissionResult(this, true, resultFile.getAbsolutePath(), log, errorLog));

	}
}
