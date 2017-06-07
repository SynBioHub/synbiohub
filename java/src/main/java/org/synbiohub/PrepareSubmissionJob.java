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
	public String databasePrefix;
	public String uriPrefix;
	public boolean requireComplete;
	public boolean requireCompliant;
	public boolean enforceBestPractices;
	public boolean typesInURI;
	public String version;
	public boolean keepGoing;
	public String topLevelURI;

	public boolean submit;
	public boolean copy;
	public String rootCollectionIdentity;
	public String newRootCollectionDisplayId;
	public String newRootCollectionVersion;
	public String ownedByURI;
	public String creatorName;
	public String name;
	public String description;
	public ArrayList<String> citationPubmedIDs;
	public ArrayList<String> collectionChoices;
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
				"http://dummy.org/",
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
		
		//doc.write(System.err);
		
		String log = new String(logOutputStream.toByteArray(), StandardCharsets.UTF_8);
		String errorLog = new String(errorOutputStream.toByteArray(), StandardCharsets.UTF_8);

		if(errorLog.startsWith("File is empty")) {
			doc = new SBOLDocument();
			errorLog = "";
		} else if(errorLog.length() > 0)
		{
			finish(new PrepareSubmissionResult(this, false, "", log, errorLog));
			return;
		}

		if (submit && !uriPrefix.contains("/public/")) {

			for(TopLevel topLevel : doc.getTopLevels())
			{	
				for (String registry : webOfRegistries.keySet()) {
					if (topLevel.getIdentity().toString().startsWith(registry)) {
						System.err.println("Found and removed:"+topLevel.getIdentity());
						doc.removeTopLevel(topLevel);
						break;
					} 
				}
			}
		}
		
		if(doc.getTopLevels().size() == 0)
		{
//			errorLog = "Submission terminated.\nThere is nothing new to add to the repository.";
//			finish(new PrepareSubmissionResult(this, false, "", log, errorLog));
//			return;
			doc.setDefaultURIprefix(uriPrefix);
			
		} else {

			System.err.println("Changing URI prefix: start");
			doc = doc.changeURIPrefixVersion(uriPrefix, version);
			System.err.println("Changing URI prefix: done");
			doc.setDefaultURIprefix(uriPrefix);

		}
		
		Collection rootCollection = null;
				
		if (submit || copy) {

			Collection submissionCollection = doc.getCollection(newRootCollectionDisplayId,newRootCollectionVersion);
			if (submissionCollection==null) {
				submissionCollection = doc.createCollection(newRootCollectionDisplayId,newRootCollectionVersion);
				System.err.println("New collection: " + submissionCollection.getIdentity().toString());
			}
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

			(new IdentifiedVisitor() {

				@Override
				public void visit(Identified identified,TopLevel topLevel) {

					try {
						for(String pubmedID : citationPubmedIDs)
						{
							identified.createAnnotation(
									new QName("http://purl.obolibrary.org/obo/", "OBI_0001617", "obo"),
									pubmedID);
						}

						identified.createAnnotation(
								new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "ownedBy", "sbh"),
								new URI(ownedByURI));

						identified.createAnnotation(
								new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "topLevel", "sbh"),
								topLevel.getIdentity());
						
					} catch (SBOLValidationException | URISyntaxException e) {


					}

				}

			}).visitDocument(doc);
		} else {

			Collection submissionCollection = doc.getCollection(URI.create(rootCollectionIdentity));
			if (submissionCollection==null) {
				submissionCollection = doc.createCollection(uriPrefix,newRootCollectionDisplayId,newRootCollectionVersion);
				submissionCollection.setName(name);
				submissionCollection.setDescription(description);
				submissionCollection.createAnnotation(
						new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"),
						creatorName);
				submissionCollection.createAnnotation(
						new QName("http://purl.org/dc/terms/", "created", "dcterms"),
						ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
				for(String pubmedID : citationPubmedIDs)
				{
					submissionCollection.createAnnotation(
							new QName("http://purl.obolibrary.org/obo/", "OBI_0001617", "obo"),
							pubmedID);
				}

				submissionCollection.createAnnotation(
						new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "ownedBy", "sbh"),
						URI.create(ownedByURI));

				submissionCollection.createAnnotation(
						new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "topLevel", "sbh"),
						submissionCollection.getIdentity());
				rootCollection = submissionCollection;
			} 
			//Collection originalRootCollection = doc.getCollection(URI.create(rootCollectionIdentity));
			//doc.createCopy(originalRootCollection, newRootCollectionDisplayId, version);
			//doc.removeCollection(originalRootCollection);
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
					SynBioHubFrontend sbh = new SynBioHubFrontend(webOfRegistries.get(registry),
							registry);
					if (topLevel.getIdentity().toString().startsWith(registry)) {
						String topLevelUri = topLevel.getIdentity().toString();
						if (topLevelUri.startsWith(registry+"/user/")) {
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
										errorLog = "Submission terminated.\nA submission with this id already exists,"
												+ " and it includes an object: " + topLevel.getIdentity()
												+ " that is already in this repository and has different content";
										finish(new PrepareSubmissionResult(this, false, "", log, errorLog));
										return;
									}
								} else {
									System.err.println("Found and removed:"+topLevelUri);
									doc.removeTopLevel(topLevel);
								}	
							}
						}
						break;
					}	
				}
			}
		}
		
		for(TopLevel topLevel : doc.getTopLevels())
		{		
			Annotation desc = topLevel.getAnnotation(new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "mutableDescription", "sbh"));
			if (desc != null && desc.isStringValue()) {
				String descStr = desc.getStringValue();
				descStr = descStr.replaceAll("img src=\\\"/user/[^/]*/", "img src=\"/public/");
				topLevel.removeAnnotation(desc);
				topLevel.createAnnotation(new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "mutableDescription", "sbh"), descStr);
			}
			Annotation notes = topLevel.getAnnotation(new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "mutableNotes", "sbh"));
			if (notes != null && notes.isStringValue()) {
				String notesStr = notes.getStringValue();
				notesStr = notesStr.replaceAll("img src=\\\"/user/[^/]*/", "img src=\"/public/");
				topLevel.removeAnnotation(notes);
				topLevel.createAnnotation(new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "mutableNotes", "sbh"), notesStr);
			}
			Annotation source = topLevel.getAnnotation(new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "mutableProvenance", "sbh"));
			if (source != null && source.isStringValue()) {
				String sourceStr = source.getStringValue();
				sourceStr = sourceStr.replaceAll("img src=\\\"/user/[^/]*/", "img src=\"/public/");
				topLevel.removeAnnotation(source);
				topLevel.createAnnotation(new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "mutableProvenance", "sbh"), sourceStr);
			}
		}

		if (rootCollection != null) {

			for(TopLevel topLevel : doc.getTopLevels())
			{		
				if(topLevel != rootCollection) {
					rootCollection.addMember(topLevel.getIdentity());
					for(String collectionChoice : collectionChoices) {
						try {
							topLevel.createAnnotation(
										new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "isMemberOf", "sbh"),
									new URI(collectionChoice));
						}
						catch (URISyntaxException e) {

						}
					}
				}	
			}

//			if (rootCollection.getMembers().size() == 0) 
//			{
//				errorLog = "Submission terminated.\nThere is nothing new to add to the repository.";
//				finish(new PrepareSubmissionResult(this, false, "", log, errorLog));
//				return;
//			}
			
		}

		File resultFile = File.createTempFile("sbh_convert_validate", ".xml");
		System.err.println("Writing file:"+resultFile.getAbsolutePath());
		SBOLWriter.write(doc, resultFile);

		finish(new PrepareSubmissionResult(this, true, resultFile.getAbsolutePath(), log, errorLog));

	}
}
