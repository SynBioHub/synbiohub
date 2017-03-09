package org.synbiohub;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

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
	public HashMap<String,String> webOfRegistries;

	public void execute() throws Exception
	{
		ByteArrayOutputStream logOutputStream = new ByteArrayOutputStream();
		ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();
		
		SBOLDocument doc = SBOLValidateSilent.validate(
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
				true);
		
		String log = new String(logOutputStream.toByteArray(), StandardCharsets.UTF_8);
		String errorLog = new String(errorOutputStream.toByteArray(), StandardCharsets.UTF_8);

		if(doc == null)
		{
			finish(new PrepareSubmissionResult(this, false, "", log, errorLog));
			return;
		}
		
		for(TopLevel topLevel : doc.getTopLevels())
		{	
			for (String registry : webOfRegistries.keySet()) {
				if (topLevel.getIdentity().toString().startsWith("http://"+registry)) {
					doc.removeTopLevel(topLevel);
					break;
				}	
			}
		}

		if(doc.getTopLevels().size() == 0)
		{
			errorLog = "Submission terminated.  There is nothing new to add to the repository.";
			finish(new PrepareSubmissionResult(this, false, "", log, errorLog));
			return;
		}
		
		doc = doc.changeURIPrefixVersion(uriPrefix, version);

		final Collection submissionCollection = doc.createCollection(newRootCollectionDisplayId,version);
		System.err.println("New collection: " + submissionCollection.getIdentity().toString());

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
		
		for(TopLevel topLevel : doc.getTopLevels())
		{	
			if(topLevel != submissionCollection) {
				submissionCollection.addMember(topLevel.getIdentity());
			}
		}

		(new IdentifiedVisitor() {

			@Override
			public void visit(Identified identified) {

				try {
					for(int pubmedID : citationPubmedIDs)
					{
						identified.createAnnotation(
								new QName("http://purl.obolibrary.org/obo/", "OBI_0001617", "obo"),
								Integer.toString(pubmedID));
					}
					
					identified.createAnnotation(
							new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "ownedBy", "sbh"),
							new URI(ownedByURI));
					
					identified.createAnnotation(
							new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "rootCollection", "sbh"),
							submissionCollection.getIdentity());
				
				} catch (SBOLValidationException | URISyntaxException e) {
					
					
				}
				
			}
			
		}).visitDocument(doc);

		File resultFile = File.createTempFile("sbh_convert_validate", ".xml");

		SBOLWriter.write(doc, resultFile);

		finish(new PrepareSubmissionResult(this, true, resultFile.getAbsolutePath(), log, errorLog));

	}
}
