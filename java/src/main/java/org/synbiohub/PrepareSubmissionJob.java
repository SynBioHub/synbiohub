package org.synbiohub;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.codec.digest.DigestUtils;
import org.jdom2.JDOMException;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbolstandard.core2.Annotation;
import org.sbolstandard.core2.Collection;
import org.sbolstandard.core2.Identified;
import org.sbolstandard.core2.IdentifiedVisitor;
import org.sbolstandard.core2.Model;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.TopLevel;
import org.synbiohub.frontend.SynBioHubException;
import org.synbiohub.frontend.SynBioHubFrontend;

import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.CombineArchiveException;
import edu.utah.ece.async.ibiosim.conversion.SBML2SBOL;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

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

	private boolean readCOMBINEArchive(String initialFilename, List<String> sbolFiles, List<String> sbmlFiles, List<String> attachments) {
		CombineArchive combine;
		try {
			combine = new CombineArchive(new File(initialFilename));
		} catch(UnsupportedOperationException | CombineArchiveException | IOException | JDOMException | ParseException e) {
			return false;
		}
		
		for(ArchiveEntry entry : combine.getEntries()) {
			String format = entry.getFormat().toString();
			
			if(format.startsWith("http://identifiers.org/combine.specifications/sbol")) {
				sbolFiles.add("unzipped/" + entry.getFileName());
			} else if(format.startsWith("http://identifiers.org/combine.specifications/sbml")) {
				sbmlFiles.add("unzipped/" + entry.getFileName());
				attachments.add("unzipped/" + entry.getFileName());
			} else {
				attachments.add("unzipped/" + entry.getFileName());
			}
		}
		
		try {
			File cwd = new File("./unzipped");
			combine.extractTo(cwd);
			combine.close();
		} catch(IOException e) {
			return false;
		}
		
		return true;
	}
	
	private boolean readZIPFile(String initialFilename, List<String> sbolFiles, List<String> sbmlFiles, List<String> attachments) {
		ZipFile zip;
		
		try {
			zip = new ZipFile(initialFilename);
		} catch (ZipException e) {
			return false;
		}
		
		if(zip.isValidZipFile()) {
			try {
				List<FileHeader> headers = zip.getFileHeaders();
				
				zip.extractAll("./unzipped");
				
				for(FileHeader header : headers) {
					String filename = "unzipped/" + header.getFileName();
					
					BufferedReader reader = new BufferedReader(new FileReader(filename));
					reader.readLine();
					String firstLine = reader.readLine();
					
					if(firstLine.contains("sbol")) {
						sbolFiles.add(filename);
					} else if(firstLine.contains("sbml")) {
						sbmlFiles.add(filename);
						attachments.add(filename);
					} else {
						attachments.add(filename);
					}
				}				
			} catch (ZipException | IOException e) {
				return false;
			}
		} else {
			return false;
		}
		
		return true;
	}
	
	private boolean getFilenames(String initialFilename, List<String> sbolFiles, List<String> sbmlFiles, List<String> attachments) {
		if(readCOMBINEArchive(initialFilename, sbolFiles, sbmlFiles, attachments)) {
			return true;
		}
		
		if(readZIPFile(initialFilename, sbolFiles, sbmlFiles, attachments)) {
			return false;
		}
		
		sbolFiles.add(initialFilename);
		return false;
	}
	
	public void execute() throws SBOLValidationException, IOException, SBOLConversionException 
	{
		System.err.println("In execute");
		ArrayList<String> filenames = new ArrayList<>();
		ArrayList<String> attachmentFiles = new ArrayList<>();
		ArrayList<String> sbmlFiles = new ArrayList<>();
		String log, errorLog = new String();
		log = "";
		errorLog = "";

		SBOLDocument doc = new SBOLDocument();
		doc.setDefaultURIprefix(uriPrefix);
		
		boolean isCombineArchive = getFilenames(sbolFilename, filenames, sbmlFiles, attachmentFiles);
		ArrayList<String> toConvert = new ArrayList<>(sbmlFiles);

		for(String filename : filenames) {
			ByteArrayOutputStream logOutputStream = new ByteArrayOutputStream();
			ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();

			SBOLDocument individual = SBOLValidate.validate(
					new PrintStream(logOutputStream),
					new PrintStream(errorOutputStream),
					filename,
					"http://dummy.org/",
					requireComplete,
					requireCompliant, 
					enforceBestPractices,
					typesInURI,
					"1",
					keepGoing,
					"",
					"",
					filename,
					topLevelURI,
					false,
					false,
					false,
					null,
					false,
					true,
					false);
			
			String fileLog = new String(logOutputStream.toByteArray(), StandardCharsets.UTF_8);
			errorLog = new String(errorOutputStream.toByteArray(), StandardCharsets.UTF_8);
			log += "[" + filename + " log] \n" + fileLog + "\n";
			
			if(errorLog.startsWith("File is empty")) {
				individual = new SBOLDocument();
				errorLog = "";
			} else if(errorLog.startsWith("sbol-10105") && !isCombineArchive) {
				individual = new SBOLDocument();
				errorLog = "";
				attachmentFiles.add(filename);
				continue;
			} else if(errorLog.length() > 0) {
				finish(new PrepareSubmissionResult(this, false, "", log, errorLog, attachmentFiles, sbmlFiles));
				return;
			}
			
			if (!copy) {

				for(TopLevel topLevel : individual.getTopLevels())
				{	
					if (!submit && topLevel.getIdentity().toString().startsWith(ownedByURI)) continue;
					for (String registry : webOfRegistries.keySet()) {
						if (topLevel.getIdentity().toString().startsWith(registry)) {
							System.err.println("Found and removed:"+topLevel.getIdentity());
							individual.removeTopLevel(topLevel);
							break;
						} 
					}
				}
				
			}
			
			individual.setDefaultURIprefix("http://dummy.org/");
			if(individual.getTopLevels().size() == 0)
			{

				individual.setDefaultURIprefix(uriPrefix);
				
			} else {

				System.err.println("Changing URI prefix: start (" + filename + ")");
				individual = individual.changeURIPrefixVersion(uriPrefix, null, version);
				System.err.println("Changing URI prefix: done (" + filename + ")");
				individual.setDefaultURIprefix(uriPrefix);
				// TODO: this should be done in libSBOLj, but done here for quick fix
				for (Model model : individual.getModels()) {
					if (model.getSource().toString().startsWith(ownedByURI)) {
						String newSource = model.getSource().toString();
						newSource = newSource.replace(ownedByURI, databasePrefix + "public");
						model.setSource(URI.create(newSource));
					}
				}

			}

			doc.createCopy(individual);
		}
		
		System.err.println(toConvert);
		System.err.println(sbmlFiles);
		
		for(String sbmlFilename : toConvert) {
			sbmlFilename = sbmlFilename.replace("unzipped/", "");
			SBOLDocument sbolDoc = new SBOLDocument();
			SBMLDocument sbmlDoc;

			boolean foundIt = false;
			for(Model model : doc.getModels() ) {
				String source = model.getSource().toString();
				System.err.println("Source="+source);
				if (sbmlFilename.equals(source)) {
					model.setSource(URI.create("file:"+source));
					foundIt = true;
					break;
				}
			}
			if (foundIt) continue;

			try {
				SBMLReader reader = new SBMLReader();
				sbmlDoc = reader.readSBMLFromFile("unzipped/"+sbmlFilename);
				System.err.println("Converting to SBOL:"+sbmlFilename);
				SBML2SBOL.convert_SBML2SBOL(sbolDoc, "unzipped", sbmlDoc, sbmlFilename, new HashSet<String>(filenames),
						uriPrefix);
				System.err.println("Finished converting to SBOL:"+sbmlFilename);
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
			
			doc.createCopy(sbolDoc);
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

						addTopLevelToNestedAnnotations(topLevel, identified.getAnnotations());

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
					topLevel.clearWasDerivedFroms();
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
									DigestUtils.sha1Hex("synbiohub_" + DigestUtils.sha1Hex(topLevel.getIdentity().toString()+"/edit") + shareLinkSalt) + 
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
							System.err.println("Looking up:"+topLevel.getIdentity());
							TopLevel tl = tlDoc.getTopLevel(topLevel.getIdentity());
							if (tl != null) {
								if (!topLevel.equals(tl)) {
									if (overwrite_merge.equals("3")) {
										try {
											sbh.removeSBOL(URI.create(topLevelUri));
										}
										catch (SynBioHubException e) {
											System.err.println("Remove fail for:"+topLevel.getIdentity());
											//e.printStackTrace(System.err);
										}
									} else {
										errorLog = "Submission terminated.\nA submission with this id already exists,"
												+ " and it includes an object: " + topLevel.getIdentity()
												+ " that is already in this repository and has different content";
										finish(new PrepareSubmissionResult(this, false, "", log, errorLog, attachmentFiles, sbmlFiles));
										return;
									}
								} else {
									System.err.println("Found and removed:"+topLevel.getIdentity());
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
				descStr = descStr.replaceAll("img src=\\\"/user/[^/]*/[^/]*/", "img src=\"/public/"+newRootCollectionDisplayId.replace("_collection", "")+"/");
				topLevel.removeAnnotation(desc);
				topLevel.createAnnotation(new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "mutableDescription", "sbh"), descStr);
			}
			Annotation notes = topLevel.getAnnotation(new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "mutableNotes", "sbh"));
			if (notes != null && notes.isStringValue()) {
				String notesStr = notes.getStringValue();
				notesStr = notesStr.replaceAll("img src=\\\"/user/[^/]*/[^/]*/", "img src=\"/public/"+newRootCollectionDisplayId.replace("_collection", "")+"/");
				topLevel.removeAnnotation(notes);
				topLevel.createAnnotation(new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "mutableNotes", "sbh"), notesStr);
			}
			Annotation source = topLevel.getAnnotation(new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "mutableProvenance", "sbh"));
			if (source != null && source.isStringValue()) {
				String sourceStr = source.getStringValue();
				sourceStr = sourceStr.replaceAll("img src=\\\"/user/[^/]*/[^/]*/", "img src=\"/public/"+newRootCollectionDisplayId.replace("_collection", "")+"/");
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
							if (collectionChoice.startsWith("http")) {
								topLevel.createAnnotation(
										new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "isMemberOf", "sbh"),
										new URI(collectionChoice));
							}
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

		finish(new PrepareSubmissionResult(this, true, resultFile.getAbsolutePath(), log, errorLog, attachmentFiles, sbmlFiles));

	}
	
	public void addTopLevelToNestedAnnotations(TopLevel topLevel, List<Annotation> annotations) throws SBOLValidationException {
		for (Annotation annotation : annotations) {
			if (annotation.isNestedAnnotations()) {
				List<Annotation> nestedAnnotations = annotation.getAnnotations();
				addTopLevelToNestedAnnotations(topLevel, nestedAnnotations);
				nestedAnnotations.add(new Annotation(
				new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "topLevel", "sbh"),
				topLevel.getIdentity()));
				annotation.setAnnotations(nestedAnnotations);
			}
		}		
	}
}
