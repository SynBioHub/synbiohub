package org.synbiohub;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.sbolstandard.core2.*;
import org.synbiohub.frontend.SynBioHubException;
import org.synbiohub.frontend.SynBioHubFrontend;

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


	public String rootCollectionIdentity;
	public String newRootCollectionDisplayId;
	public String newRootCollectionVersion;
	public String ownedByURI;
	public String creatorName;
	public String name;
	public String description;
	public ArrayList<Integer> citationPubmedIDs;
	public ArrayList<String> keywords;
	public HashMap<String,String> webOfRegistries;
	public String shareLinkSalt;
	public String overwrite_merge;

	public void execute() throws SBOLValidationException, IOException, SBOLConversionException 
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

		if (!newRootCollectionDisplayId.equals("") && !uriPrefix.contains("/public/")) {
		
			for(TopLevel topLevel : doc.getTopLevels())
			{	
				for (String registry : webOfRegistries.keySet()) {
					if (topLevel.getIdentity().toString().startsWith("http://"+registry)) {
						doc.removeTopLevel(topLevel);
						break;
					}	
				}
			}
		}
		
		if(doc.getTopLevels().size() == 0)
		{
			errorLog = "Submission terminated.\nThere is nothing new to add to the repository.";
			finish(new PrepareSubmissionResult(this, false, "", log, errorLog));
			return;
		}
		
		doc = doc.changeURIPrefixVersion(uriPrefix, version);

		Collection rootCollection = null;
				
		if (!newRootCollectionDisplayId.equals("")) {

			final Collection submissionCollection = doc.createCollection(newRootCollectionDisplayId,newRootCollectionVersion);
			System.err.println("New collection: " + submissionCollection.getIdentity().toString());
			rootCollection = submissionCollection;
			
			submissionCollection.createAnnotation(
					new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"),
					creatorName);

			if (newRootCollectionVersion.equals(version)) {
				submissionCollection.createAnnotation(
						new QName("http://purl.org/dc/terms/", "created", "dcterms"),
						ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));

				submissionCollection.setName(name);
				submissionCollection.setDescription(description);
			}
			
			for(String keyword : keywords)
			{
				submissionCollection.createAnnotation(
						new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "keyword", "sbh"),
						keyword);
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
		}
		
		if (!overwrite_merge.equals("0") && !overwrite_merge.equals("1")) {
			for(TopLevel topLevel : doc.getTopLevels())
			{	
				if(topLevel.getIdentity().toString().equals(rootCollectionIdentity)) {
					topLevel.unsetDescription();
					topLevel.unsetName();
					topLevel.unsetWasDerivedFrom();
					Annotation annotation = topLevel.getAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"));
					topLevel.removeAnnotation(annotation);
					continue;
				}
				for (String registry : webOfRegistries.keySet()) {
					SynBioHubFrontend sbh = new SynBioHubFrontend("http://"+webOfRegistries.get(registry),
							"http://"+registry);
					if (topLevel.getIdentity().toString().startsWith("http://"+registry)) {
						String topLevelUri = topLevel.getIdentity().toString();
						if (topLevelUri.startsWith("http://"+registry+"/user/")) {
							topLevelUri = topLevel.getIdentity().toString() + '/' + 
									DigestUtils.sha1Hex("synbiohub_" + DigestUtils.sha1Hex(topLevel.getIdentity().toString()) + shareLinkSalt) + 
									"/share";
						}
						SBOLDocument tlDoc;
						try {
							tlDoc = sbh.getSBOL(URI.create(topLevelUri));
						}
						catch (SynBioHubException e) {
							tlDoc = null;
						}
						if (tlDoc != null) {
							TopLevel tl = tlDoc.getTopLevel(topLevel.getIdentity());
							if (tl != null) {
								if (!topLevel.equals(tl)) {
									if (overwrite_merge.equals("3")) {
										try {
											sbh.removeSBOL(URI.create(topLevelUri));
										}
										catch (SynBioHubException e) {
											e.printStackTrace();
										}
									} else {
										errorLog = "Submission terminated.\nA submission with this id already exists, "
												+ " and it includes an object:\n" + topLevel.getIdentity() + "\nthat is already "
												+ " in this repository and has different content";
										finish(new PrepareSubmissionResult(this, false, "", log, errorLog));
										return;
									}
								} else {
									doc.removeTopLevel(topLevel);
								}	
							}
						}
						break;
					}	
				}
			}
		}
		
		if (rootCollection != null) {
			for(TopLevel topLevel : doc.getTopLevels())
			{		
				if(topLevel != rootCollection) {
					rootCollection.addMember(topLevel.getIdentity());
				}	
			}

			if (rootCollection.getMembers().size() == 0) 
			{
				errorLog = "Submission terminated.\nThere is nothing new to add to the repository.";
				finish(new PrepareSubmissionResult(this, false, "", log, errorLog));
				return;
			}
		}

		File resultFile = File.createTempFile("sbh_convert_validate", ".xml");

		SBOLWriter.write(doc, resultFile);

		finish(new PrepareSubmissionResult(this, true, resultFile.getAbsolutePath(), log, errorLog));

	}
}
