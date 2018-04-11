package org.synbiohub;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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
import java.util.Map;
import java.util.List;
import java.util.Enumeration;
import java.util.zip.*;

import javax.xml.namespace.QName;

import org.apache.commons.codec.digest.DigestUtils;
import org.jdom2.JDOMException;
import org.sbolstandard.core2.Annotation;
import org.sbolstandard.core2.Attachment;
import org.sbolstandard.core2.Collection;
import org.sbolstandard.core2.Identified;
import org.sbolstandard.core2.IdentifiedVisitor;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.TopLevel;
import org.synbiohub.frontend.SynBioHubException;
import org.synbiohub.frontend.SynBioHubFrontend;

import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.CombineArchiveException;

public class PrepareSubmissionJob extends Job {
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
	public HashMap<String, String> webOfRegistries;
	public String shareLinkSalt;
	public String overwrite_merge;
	public String tempDirPath;

	private boolean readCOMBINEArchive(String initialFilename, Map<String, String> attachments) {
		CombineArchive combine;
		File tempDir;

		try {
			tempDir = Files.createTempDirectory("extract").toFile();
			tempDirPath = tempDir.getAbsolutePath();
		} catch (IOException e) {
			System.err.println("Could not create temporary directory!");
			return false;
		}

		try {
			combine = new CombineArchive(new File(initialFilename));
		} catch (UnsupportedOperationException | CombineArchiveException | IOException | JDOMException
				| ParseException e) {
			return false;
		}

		for (ArchiveEntry entry : combine.getEntries()) {
			Path unzippedLocation = FileSystems.getDefault().getPath(tempDir.toString(), entry.getFileName());
			attachments.put(unzippedLocation.toString(), entry.getFormat().toString());
		}

		try {
			combine.extractTo(tempDir);
			combine.close();
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	private boolean readZIPFile(String initialFilename, Map<String, String> attachments) {
		ZipFile zip;
		Enumeration<? extends ZipEntry> manifest;
		Path extractDir;

		try {
			zip = new ZipFile(initialFilename);
		} catch (IOException e) {
			return false;
		}

		try {
			manifest = zip.entries();
			extractDir = Files.createTempDirectory("extract");
			tempDirPath = extractDir.toFile().getAbsolutePath();
		} catch (IOException e) {
			try {
				zip.close();
			}
			catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return false;
		}

		while (manifest.hasMoreElements()) {
			ZipEntry entry = manifest.nextElement();

			if (entry.isDirectory()) {
				continue;
			}

			if(entry.getName().contains("__MACOSX") || entry.getName().contains(".DS_Store")) {
				continue; // fuck a resource fork
			}

			String filename = entry.getName();
			Path extracted;

			try {
				InputStream entryStream = zip.getInputStream(entry);
				filename = FileSystems.getDefault().getPath(entry.getName()).getFileName().toString();
				extracted = FileSystems.getDefault().getPath(extractDir.toString(), filename);

				Files.copy(entryStream, extracted);
			} catch (IOException e) {
				System.err.println("Error extracting file " + entry.getName());
				continue;
			}

			try {
				String filePath = extracted.toAbsolutePath().toString();
				BufferedReader reader = new BufferedReader(new FileReader(filePath));
				reader.readLine();
				String firstLine = reader.readLine();
				String format = "";
				reader.close();

				if (firstLine != null) {
					if (firstLine.contains("sbol")) {
						format = "http://identifiers.org/combine.specifications/sbol";
					} else if (firstLine.contains("sbml")) {
						format = "http://identifiers.org/combine.specifications/sbml";
					} else if (firstLine.contains("sedml")) {
						format = "http://identifiers.org/combine.specifications/sedml";
					} else if (SBOLReader.isGenBankFile(filePath) || SBOLReader.isFastaFile(filePath)) {
						format = "http://identifiers.org/combine.specifications/sbol";
					}
				}

				attachments.put(extracted.toAbsolutePath().toString(), format);
			} catch (IOException e) {
				System.err.println("Error classifying file " + entry.getName());
				continue;
			}
		}
		try {
			zip.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	private boolean getFilenames(String initialFilename, Map<String, String> attachments) {
		if (readCOMBINEArchive(initialFilename, attachments)) {
			return true;
		}

		if (readZIPFile(initialFilename, attachments)) {
			return false;
		}

		attachments.put(initialFilename, "http://identifiers.org/combine.specifications/sbol");
		return false;
	}

	public void execute() throws SBOLValidationException, IOException, SBOLConversionException {
		System.err.println("In execute");
		HashMap<String, String> attachmentFiles = new HashMap<>();
		String log, errorLog = new String();
		log = "";
		errorLog = "";

		SBOLDocument doc = new SBOLDocument();
		doc.setDefaultURIprefix(uriPrefix);

		boolean isCombineArchive = getFilenames(sbolFilename, attachmentFiles);
		ArrayList<String> toConvert = new ArrayList<>();
		ArrayList<String> sbolFiles = new ArrayList<>();

		for (String filename : attachmentFiles.keySet()) {
			if (attachmentFiles.get(filename).startsWith("http://identifiers.org/combine.specifications/sbml"))
				toConvert.add(filename);
		}

		for (String filename : attachmentFiles.keySet()) {
			if (!attachmentFiles.get(filename).toLowerCase().contains("sbol")) {
				continue;
			}

			sbolFiles.add(filename);

			ByteArrayOutputStream logOutputStream = new ByteArrayOutputStream();
			ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();

			SBOLDocument individual = SBOLValidate.validate(new PrintStream(logOutputStream),
					new PrintStream(errorOutputStream), filename, "http://dummy.org/", requireComplete,
					requireCompliant, enforceBestPractices, typesInURI, "1", keepGoing, "", "", filename, topLevelURI,
					false, false, false, null, false, true, false);

			String fileLog = new String(logOutputStream.toByteArray(), StandardCharsets.UTF_8);
			errorLog = new String(errorOutputStream.toByteArray(), StandardCharsets.UTF_8);
			log += "[" + filename + " log] \n" + fileLog + "\n";

			if (errorLog.startsWith("File is empty")) {
				individual = new SBOLDocument();
				errorLog = "";
			} else if (errorLog.startsWith("sbol-10105") && !isCombineArchive) {
				individual = new SBOLDocument();
				errorLog = "";
				continue;
			} else if (errorLog.length() > 0) {
				finish(new PrepareSubmissionResult(this, false, "", log, "[" + filename + "] " + errorLog,
						attachmentFiles, tempDirPath));
				return;
			}

			if (!copy) {
				for (TopLevel topLevel : individual.getTopLevels()) {
					if (!submit && topLevel.getIdentity().toString().startsWith(ownedByURI))
						continue;
					for (String registry : webOfRegistries.keySet()) {
						if (topLevel.getIdentity().toString().startsWith(registry)) {
							System.err.println("Found and removed:" + topLevel.getIdentity());
							individual.removeTopLevel(topLevel);
							break;
						}
					}
				}
			}

			individual.setDefaultURIprefix("http://dummy.org/");
			if (individual.getTopLevels().size() == 0) {
				individual.setDefaultURIprefix(uriPrefix);
			} else {
				System.err.println("Changing URI prefix: start (" + filename + ")");
				individual = individual.changeURIPrefixVersion(uriPrefix, null, version);
				System.err.println("Changing URI prefix: done (" + filename + ")");
				individual.setDefaultURIprefix(uriPrefix);

				for (Attachment attachment : individual.getAttachments()) {
					if (attachment.getSource().toString().startsWith(databasePrefix) &&
							attachment.getSource().toString().endsWith("/download")) {
						attachment.setSource(URI.create(attachment.getIdentity().toString()+"/download"));
					}
				}
			}

			doc.createCopy(individual);
		}

//		for (String sbmlFilename : toConvert) {
//			String sbmlFile = FileSystems.getDefault().getPath(sbmlFilename).getFileName().toString();
//			String sbmlDirectory = FileSystems.getDefault().getPath(sbmlFilename).getParent().toString();
//
//			SBOLDocument sbolDoc = new SBOLDocument();
//			SBMLDocument sbmlDoc;
//
//			boolean foundIt = false;
//
//			for (Model model : doc.getModels()) {
//				String source = model.getSource().toString();
//				if (sbmlFilename.equals(source)) {
//					model.setSource(URI.create("file:" + source));
//					foundIt = true;
//					break;
//				}
//			}
//			if (foundIt)
//				continue;
//
//			try {
//				SBMLReader reader = new SBMLReader();
//				sbmlDoc = reader.readSBMLFromFile(sbmlFilename);
//				sbolDoc.write(System.err);
//				SBML2SBOL.convert_SBML2SBOL(sbolDoc, sbmlDirectory, sbmlDoc, sbmlFile,
//						new HashSet<String>(sbolFiles), uriPrefix);
//			} catch (XMLStreamException e) {
//				e.printStackTrace();
//			}
//
//			doc.createCopy(sbolDoc);
//		}

		Collection rootCollection = null;

		if (submit || copy) {

			Collection submissionCollection = doc.getCollection(newRootCollectionDisplayId, newRootCollectionVersion);
			if (submissionCollection == null) {
				submissionCollection = doc.createCollection(newRootCollectionDisplayId, newRootCollectionVersion);
				System.err.println("New collection: " + submissionCollection.getIdentity().toString());
			}
			rootCollection = submissionCollection;

			submissionCollection.createAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"),
					creatorName);

			if (newRootCollectionVersion.equals(version)) {
				submissionCollection.createAnnotation(new QName("http://purl.org/dc/terms/", "created", "dcterms"),
						ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));

				submissionCollection.setName(name);
				submissionCollection.setDescription(description);
			}

			(new IdentifiedVisitor() {

				@Override
				public void visit(Identified identified, TopLevel topLevel) {

					try {

						addTopLevelToNestedAnnotations(topLevel, identified.getAnnotations());

						for (String pubmedID : citationPubmedIDs) {
							identified.createAnnotation(
									new QName("http://purl.obolibrary.org/obo/", "OBI_0001617", "obo"), pubmedID);
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
			if (submissionCollection == null) {
				submissionCollection = doc.createCollection(uriPrefix, newRootCollectionDisplayId,
						newRootCollectionVersion);
				submissionCollection.setName(name);
				submissionCollection.setDescription(description);
				submissionCollection.createAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"),
						creatorName);
				submissionCollection.createAnnotation(new QName("http://purl.org/dc/terms/", "created", "dcterms"),
						ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
				for (String pubmedID : citationPubmedIDs) {
					submissionCollection.createAnnotation(
							new QName("http://purl.obolibrary.org/obo/", "OBI_0001617", "obo"), pubmedID);
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

			for (TopLevel topLevel : doc.getTopLevels()) {
				if (topLevel.getIdentity().toString().equals(rootCollectionIdentity)) {
					topLevel.unsetDescription();
					topLevel.unsetName();
					topLevel.clearWasDerivedFroms();
					Annotation annotation = topLevel
							.getAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"));
					topLevel.removeAnnotation(annotation);
					continue;
				}
				for (String registry : webOfRegistries.keySet()) {
					SynBioHubFrontend sbh = new SynBioHubFrontend(webOfRegistries.get(registry), registry);
					if (topLevel.getIdentity().toString().startsWith(registry)) {
						String topLevelUri = topLevel.getIdentity().toString();
						if (topLevelUri.startsWith(registry + "/user/")) {
							topLevelUri = topLevel.getIdentity().toString() + '/' + DigestUtils.sha1Hex("synbiohub_"
									+ DigestUtils.sha1Hex(topLevel.getIdentity().toString() + "/edit") + shareLinkSalt)
									+ "/share";
						}
						SBOLDocument tlDoc;
						try {
							tlDoc = sbh.getSBOL(URI.create(topLevelUri));
						} catch (SynBioHubException e) {
							tlDoc = null;
						}
						if (tlDoc != null) {
							System.err.println("Looking up:" + topLevel.getIdentity());
							TopLevel tl = tlDoc.getTopLevel(topLevel.getIdentity());
							if (tl != null) {
								if (!topLevel.equals(tl)) {
									if (overwrite_merge.equals("3")) {
										try {
											sbh.removeSBOL(URI.create(topLevelUri));
										} catch (SynBioHubException e) {
											System.err.println("Remove fail for:" + topLevel.getIdentity());
											//e.printStackTrace(System.err);
										}
									} else {
										errorLog = "Submission terminated.\nA submission with this id already exists,"
												+ " and it includes an object: " + topLevel.getIdentity()
												+ " that is already in this repository and has different content";
										finish(new PrepareSubmissionResult(this, false, "", log, errorLog,
												attachmentFiles,tempDirPath));
										return;
									}
								} else {
									System.err.println("Found and removed:" + topLevel.getIdentity());
									doc.removeTopLevel(topLevel);
									if (rootCollection != null) {
										rootCollection.addMember(topLevel.getIdentity());
									}
								}
							}
						}
						break;
					}
				}
			}
		}

		for (TopLevel topLevel : doc.getTopLevels()) {
			Annotation desc = topLevel.getAnnotation(
					new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "mutableDescription", "sbh"));
			if (desc != null && desc.isStringValue()) {
				String descStr = desc.getStringValue();
				descStr = descStr.replaceAll("img src=\\\"/user/[^/]*/[^/]*/",
						"img src=\"/public/" + newRootCollectionDisplayId.replace("_collection", "") + "/");
				topLevel.removeAnnotation(desc);
				topLevel.createAnnotation(
						new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "mutableDescription", "sbh"),
						descStr);
			}
			Annotation notes = topLevel
					.getAnnotation(new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "mutableNotes", "sbh"));
			if (notes != null && notes.isStringValue()) {
				String notesStr = notes.getStringValue();
				notesStr = notesStr.replaceAll("img src=\\\"/user/[^/]*/[^/]*/",
						"img src=\"/public/" + newRootCollectionDisplayId.replace("_collection", "") + "/");
				topLevel.removeAnnotation(notes);
				topLevel.createAnnotation(
						new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "mutableNotes", "sbh"), notesStr);
			}
			Annotation source = topLevel.getAnnotation(
					new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "mutableProvenance", "sbh"));
			if (source != null && source.isStringValue()) {
				String sourceStr = source.getStringValue();
				sourceStr = sourceStr.replaceAll("img src=\\\"/user/[^/]*/[^/]*/",
						"img src=\"/public/" + newRootCollectionDisplayId.replace("_collection", "") + "/");
				topLevel.removeAnnotation(source);
				topLevel.createAnnotation(
						new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "mutableProvenance", "sbh"),
						sourceStr);
			}
		}

		if (rootCollection != null) {

			for (TopLevel topLevel : doc.getTopLevels()) {
				if (topLevel != rootCollection) {
					rootCollection.addMember(topLevel.getIdentity());
					for (String collectionChoice : collectionChoices) {
						try {
							if (collectionChoice.startsWith("http")) {
								topLevel.createAnnotation(new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#",
										"isMemberOf", "sbh"), new URI(collectionChoice));
							}
						} catch (URISyntaxException e) {

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
		System.err.println("Writing file:" + resultFile.getAbsolutePath());
		SBOLWriter.write(doc, resultFile);

		finish(new PrepareSubmissionResult(this, true, resultFile.getAbsolutePath(), log, errorLog, attachmentFiles, tempDirPath));

	}

	public void addTopLevelToNestedAnnotations(TopLevel topLevel, List<Annotation> annotations)
			throws SBOLValidationException {
		for (Annotation annotation : annotations) {
			if (annotation.isNestedAnnotations()) {
				List<Annotation> nestedAnnotations = annotation.getAnnotations();
				addTopLevelToNestedAnnotations(topLevel, nestedAnnotations);
				nestedAnnotations.add(
						new Annotation(new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "topLevel", "sbh"),
								topLevel.getIdentity()));
				annotation.setAnnotations(nestedAnnotations);
			}
		}
	}
}