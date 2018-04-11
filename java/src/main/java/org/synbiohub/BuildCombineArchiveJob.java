package org.synbiohub;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.jdom2.JDOMException;
import org.sbolstandard.core2.Attachment;
import org.sbolstandard.core2.GenericTopLevel;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;

import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.CombineArchiveException;
import de.unirostock.sems.cbarchive.meta.OmexMetaDataObject;
import de.unirostock.sems.cbarchive.meta.omex.OmexDescription;
import de.unirostock.sems.cbarchive.meta.omex.VCard;

public class BuildCombineArchiveJob extends Job {
	public String sbolFilename;
	public List<HashMap<String, String>> creatorInfo;

	private class FileInfo {
		public String fileName;
		public URI fileType;

		public FileInfo(String fileName, URI fileType) {
			this.fileName = fileName;
			this.fileType = fileType;
		}
	} 
	
	private List<VCard> createCreators(List<HashMap<String, String>> creatorInfo) {
		List<VCard> creators = new ArrayList<>();
		
		for (HashMap<String, String> creator : creatorInfo) {
			creators.add(new VCard(creator.get("lastName"), 
								   creator.get("firstName"), 
								   creator.get("email"), 
								   creator.get("affiliation")));
		}
		
		return creators;
	}
	
	private CombineArchive createCombineArchive() {
		File archiveFile;
	
		try {
			archiveFile = File.createTempFile("combine-download", ".omex");
			return new CombineArchive(archiveFile);
		} catch (IOException | CombineArchiveException | JDOMException | ParseException e) {
			finish(new BuildCombineArchiveResult(this, false, "", e.getMessage()));
			return null;
		}	
	}

	private void addSBOLFile(CombineArchive archive) {
		File file = new File(this.sbolFilename);
		URI sbolUri;		

		try {
			sbolUri = new URI("http://identifiers.org/combine.specifications/sbol.version-2");
		} catch (URISyntaxException e) {
			return;
		}

		try {
			archive.addEntry(file, "sbol.rdf", sbolUri, true);
		} catch (IOException e) {
			finish(new BuildCombineArchiveResult(this, false, "", e.getMessage()));
			return;
		}
	}

	private HashMap<Path, FileInfo> findAttachmentFiles() {
		HashMap<Path, FileInfo> attachmentPaths = new HashMap<>();
		SBOLDocument document;

		try {
			document = SBOLReader.read(sbolFilename);
		} catch (SBOLValidationException | SBOLConversionException | IOException e) {
			finish(new BuildCombineArchiveResult(this, false, "", e.getMessage()));
			return null;
		}

		for(GenericTopLevel genericTopLevel : document.getGenericTopLevels()) {
			QName rdfType = genericTopLevel.getRDFType();

			if(rdfType.getLocalPart() == "Attachment") {
				String hash = genericTopLevel.getAnnotation(new QName(rdfType.getNamespaceURI(), "attachmentHash")).getStringValue();
				String directory = hash.substring(0, 2);
				String filename = hash.substring(2) + ".gz";
				URI type = genericTopLevel.getAnnotation(new QName(rdfType.getNamespaceURI(), "attachmentType")).getURIValue();
				String actualName = genericTopLevel.getName();

				FileInfo fileInfo = new FileInfo(actualName, type);

				Path filepath = Paths.get(".", "uploads", directory, filename);
				attachmentPaths.put(filepath, fileInfo);
			}
		}

		for(Attachment attachment : document.getAttachments()) {
			String directory = attachment.getHash().substring(0, 2);
			String filename = attachment.getHash().substring(2) + ".gz";
			URI type = attachment.getFormat();
			String name = attachment.getName();

			if(name == null) {
				name = attachment.getDisplayId();
			}

			FileInfo fileInfo = new FileInfo(name, type);
			Path filePath = Paths.get(".", "uploads", directory, filename);

			attachmentPaths.put(filePath, fileInfo);
		}

		return attachmentPaths;
	}

	private void addAttachments(CombineArchive archive, HashMap<Path, FileInfo> attachments, Path tempDir) {
		for (Path attachmentFilePath : attachments.keySet()) {
			FileInfo fileInfo = attachments.get(attachmentFilePath);

			try {
				GZIPInputStream gzipIn = new GZIPInputStream(new FileInputStream(attachmentFilePath.toFile()));
				Path tempFile = Files.createTempFile(tempDir, "unzipped", "");

				Files.copy(gzipIn, tempFile, StandardCopyOption.REPLACE_EXISTING);
				archive.addEntry(tempFile.toFile(), fileInfo.fileName, fileInfo.fileType);
			} catch (IOException e) {
				System.err.print("Error adding file named ");
				e.printStackTrace(System.err);
				System.err.println(attachmentFilePath.getFileName().toString());
				continue;
			}
		}
	}

	private void addCreators(CombineArchive archive, List<VCard> creators) {
		OmexMetaDataObject description = new OmexMetaDataObject(new OmexDescription(creators, new Date()));
		archive.addDescription(description);

		for(ArchiveEntry entry : archive.getEntries()) {
			entry.addDescription(description);
		}
	}

	public void execute() {
		List<VCard> creators = createCreators(creatorInfo);
		CombineArchive archive = createCombineArchive();
		
		if(archive == null) {
			return;
		}

		HashMap<Path, FileInfo> attachments = findAttachmentFiles();
		Path tempDirPath;


		try {
			tempDirPath = Files.createTempDirectory("omex-build");
		} catch (IOException e) {
			finish(new BuildCombineArchiveResult(this, false, "", e.getMessage()));
			return;
		}
		
		addSBOLFile(archive);
		addAttachments(archive, attachments, tempDirPath);
		addCreators(archive, creators);

		try {
			archive.pack();
			archive.close();
			FileUtils.deleteDirectory(tempDirPath.toFile());
			finish(new BuildCombineArchiveResult(this, true, archive.getZipLocation().getAbsolutePath(), ""));
			return;
		} catch (IOException | TransformerException e) {
			finish(new BuildCombineArchiveResult(this, false, "", e.getMessage()));
			return;
		}
	}
}
