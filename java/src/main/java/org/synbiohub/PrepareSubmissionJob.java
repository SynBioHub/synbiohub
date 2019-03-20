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
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Enumeration;
import java.util.zip.*;

import javax.xml.namespace.QName;

import org.apache.commons.codec.digest.DigestUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;

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
	public boolean useSBOLExplorer;
	public String SBOLExplorerEndpoint;

	private boolean readCOMBINEArchive(String initialFilename, Map<String, String> attachments) {
		CombineArchive combine = null;
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
			if (combine != null) {
				try {
					combine.close();
				}
				catch (IOException e1) {
					// TODO Auto-generated catch block
					//e1.printStackTrace();
				}
			}
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
				continue; // a resource fork
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
	
	private static String fixDisplayId(String displayId) {
		displayId = displayId.replaceAll("[^a-zA-Z0-9_]", "_");
		displayId = displayId.replace(" ", "_");
		if (Character.isDigit(displayId.charAt(0))) {
			displayId = "_" + displayId;
		}
		return displayId;
	}

	public void execute() throws SBOLValidationException, IOException, SBOLConversionException {
		System.err.println("In execute");

		HashMap<String, String> attachmentFiles = new HashMap<>();
		ArrayList<String> toConvert = new ArrayList<>();
		ArrayList<String> sbolFiles = new ArrayList<>();
		HashSet<String> explorerUrisToRemove = new HashSet<>();
		HashSet<URI> urisFoundInSynBioHub = new HashSet<>();
		
		String log, errorLog = new String();
		log = "";
		errorLog = "";

		SBOLDocument doc = new SBOLDocument();
		doc.setDefaultURIprefix(uriPrefix);

		// Check if CombineArchive and get files
		boolean isCombineArchive = getFilenames(sbolFilename, attachmentFiles);

		// TODO: Zach: is this obsolete code?
		for (String filename : attachmentFiles.keySet()) {
			if (attachmentFiles.get(filename).startsWith("http://identifiers.org/combine.specifications/sbml"))
				toConvert.add(filename);
		}

		// Process SBOL files
		for (String filename : attachmentFiles.keySet()) {
			if (!attachmentFiles.get(filename).toLowerCase().contains("sbol")) {
				continue;
			}

			sbolFiles.add(filename);
			String defaultDisplayId = filename;
			if (defaultDisplayId.lastIndexOf(".")!=-1) {
				defaultDisplayId = defaultDisplayId.substring(0,defaultDisplayId.lastIndexOf("."));
			}
			if (defaultDisplayId.lastIndexOf("/")!=-1) {
				defaultDisplayId = defaultDisplayId.substring(defaultDisplayId.lastIndexOf("/")+1);
			}
			defaultDisplayId = fixDisplayId(defaultDisplayId);

			ByteArrayOutputStream logOutputStream = new ByteArrayOutputStream();
			ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();

			// Validate and convert file, if necessary
			SBOLDocument individual = SBOLValidate.validate(new PrintStream(logOutputStream),
					new PrintStream(errorOutputStream), filename, "http://dummy.org/", defaultDisplayId, requireComplete,
					requireCompliant, enforceBestPractices, typesInURI, "1", keepGoing, "", "", filename, topLevelURI,
					false, false, false, false, null, false, true, false);

			String fileLog = new String(logOutputStream.toByteArray(), StandardCharsets.UTF_8);
			errorLog = new String(errorOutputStream.toByteArray(), StandardCharsets.UTF_8);
			log += "[" + filename + " log] \n" + fileLog + "\n";

			System.err.println(log);
			System.err.println(errorLog);

			if (errorLog.startsWith("File is empty")) {
				individual = new SBOLDocument();
				errorLog = "";
			} /* else if (errorLog.startsWith("sbol-10105") && !isCombineArchive) {
				individual = new SBOLDocument();
				errorLog = "";
				continue;
			} */ else if (errorLog.length() > 0) {
				finish(new PrepareSubmissionResult(this, false, "", log, "[" + filename + "] " + errorLog,
						attachmentFiles, tempDirPath));
				return;
			}

			// Remove files already stored in SynBioHub repositories
			if (!copy) {
				for (TopLevel topLevel : individual.getTopLevels()) {
					if (!submit && topLevel.getIdentity().toString().startsWith(ownedByURI))
						continue;
					for (String registry : webOfRegistries.keySet()) {
						if (topLevel.getIdentity().toString().startsWith(registry)) {
							System.err.println("Found and removed:" + topLevel.getIdentity());
							urisFoundInSynBioHub.add(topLevel.getIdentity());
							individual.removeTopLevel(topLevel);
							break;
						}
					}
				}
			}

			// Remove index information for private objects being made public
			if (useSBOLExplorer && (!submit && !copy)) {
				for (TopLevel topLevel : individual.getTopLevels()) {
					if (topLevel.getIdentity().toString().startsWith(ownedByURI)) {
						explorerUrisToRemove.add(topLevel.getIdentity().toString());
					}
				}
			}

			// Update URI prefix and version of all objects
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

			// Copy SBOL for individual file into composite document
			doc.createCopy(individual);
		}

		Collection rootCollection = null;

		if (submit || copy) {

			// Create the submission collection
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

			// Update ownedBy, topLevel, and PubMedId annotations
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

			// Update submission collection if being renamed on make public
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
		}

		if (!overwrite_merge.equals("0") && !overwrite_merge.equals("1")) {

			// Merge into an existing collection
			for (TopLevel topLevel : doc.getTopLevels()) {
				
				// Update root collection
				if (topLevel.getIdentity().toString().equals(rootCollectionIdentity)) {
					topLevel.unsetDescription();
					topLevel.unsetName();
					topLevel.clearWasDerivedFroms();
					Annotation annotation = topLevel
							.getAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"));
					topLevel.removeAnnotation(annotation);
					continue;
				}
				
				// Check if the object is already in the collection
				for (String registry : webOfRegistries.keySet()) {
					SynBioHubFrontend sbh = new SynBioHubFrontend(webOfRegistries.get(registry), registry);
					if (topLevel.getIdentity().toString().startsWith(registry)) {
						
						// Fetch the object from SynBioHub
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
								// The object has been found
								if (!topLevel.equals(tl)) {
									// The object is different
									if (overwrite_merge.equals("3")) {
										// Overwrite is selected so remove the old object
										try {
											sbh.replaceSBOL(URI.create(topLevelUri));
										} catch (SynBioHubException e) {
											System.err.println("Remove fail for:" + topLevel.getIdentity());
											//e.printStackTrace(System.err);
										}
									} else {
										// Overwrite is not selected so fail
										errorLog = "Submission terminated.\nA submission with this id already exists,"
												+ " and it includes an object: " + topLevel.getIdentity()
												+ " that is already in this repository and has different content";
										finish(new PrepareSubmissionResult(this, false, "", log, errorLog,
												attachmentFiles,tempDirPath));
										return;
									}
								} else {
									// The object is the same, so do not add again, but keep in collection
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

		// Update mutable annotations
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

		// Add objects as members of the root submission collection
		if (rootCollection != null) {
			for (TopLevel topLevel : doc.getTopLevels()) {
				if (topLevel != rootCollection) {
					rootCollection.addMember(topLevel.getIdentity());
					// TODO: this code is obsolete
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
			for (URI identity : urisFoundInSynBioHub) {
				rootCollection.addMember(identity);
			}
		}

		// Update SBOLExplorer index
		if (useSBOLExplorer) {
			incrementallyUpdateSBOLExplorer(explorerUrisToRemove, doc);
		}

		// Return SBOL document and attachment files
		File resultFile = File.createTempFile("sbh_convert_validate", ".xml");
		System.err.println("Writing file:" + resultFile.getAbsolutePath());
		SBOLWriter.write(doc, resultFile);

		finish(new PrepareSubmissionResult(this, true, resultFile.getAbsolutePath(), log, errorLog, attachmentFiles, tempDirPath));

	}

  class ExplorerTopLevel {
    String subject;
    String displayId;
    String version;
    String name;
    String description;
    String type;
    String graph;
  }

  class ExplorerPost {
    List<String> partsToRemove;

    List<ExplorerTopLevel> partsToAdd;
  }

  public void incrementallyUpdateSBOLExplorer(HashSet<String> explorerUrisToRemove, SBOLDocument topLevelsToAdd) {
    ExplorerPost payload = new ExplorerPost();

    // remove parts
    List<String> partsToRemove = new ArrayList<>();
    if (!submit && !copy) {
      partsToRemove.addAll(explorerUrisToRemove);
    }
    payload.partsToRemove = partsToRemove;

    // add parts
    List<ExplorerTopLevel> partsToAdd = new ArrayList<>();

    for (TopLevel topLevel : topLevelsToAdd.getTopLevels()) {
      ExplorerTopLevel etl = new ExplorerTopLevel();

      etl.subject = topLevel.getIdentity().toString();
      etl.displayId = topLevel.getDisplayId();
      etl.version = topLevel.getVersion();
      etl.name = topLevel.getName();
      etl.description = topLevel.getDescription();
      etl.type = "TODO"; // TODO

      if (!submit && !copy) {
        etl.graph = databasePrefix + "public";
      } else {
        etl.graph = ownedByURI;
      }

      partsToAdd.add(etl);
    }

    payload.partsToAdd = partsToAdd;

    // send request
    try {
      Gson gson = new Gson();
      HttpPost post = new HttpPost(SBOLExplorerEndpoint + "incrementalupdate");
      String json = gson.toJson(payload);
      post.setEntity(new StringEntity(json));
      post.setHeader("Content-type", "application/json");
      System.err.println("SBOLExplorer request: " + post.toString());

      HttpClient httpClient = HttpClientBuilder.create().build();

      HttpResponse response = httpClient.execute(post);
      System.err.println("SBOLExplorer response: " + response.toString());
    } catch (Exception e) {
      System.err.println("SBOLExplorer /incrementalupdate failed");
    } 
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
