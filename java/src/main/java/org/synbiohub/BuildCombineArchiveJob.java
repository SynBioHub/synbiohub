package org.synbiohub;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Date;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.GenericTopLevel;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLConversionException;
import org.jdom2.JDOMException;

import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchiveException;
import de.unirostock.sems.cbarchive.meta.OmexMetaDataObject;
import de.unirostock.sems.cbarchive.meta.omex.OmexDescription;
import de.unirostock.sems.cbarchive.meta.omex.VCard;
import de.unirostock.sems.cbext.Formatizer;

public class BuildCombineArchiveJob extends Job {
	public String sbolFilename;
	public List<HashMap<String, String>> creatorInfo;
	
	private URI determineFiletype(File file) {
		return Formatizer.guessFormat(file);
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
			archive.addEntry(file, this.sbolFilename, sbolUri, true);
		} catch (IOException e) {
			finish(new BuildCombineArchiveResult(this, false, "", e.getMessage()));
			return;
		}
	}

	private List<Path> findAttachmentFiles() {
		ArrayList<Path> attachmentPaths = new ArrayList<>();
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

				Path filepath = Paths.get(".", "uploads", directory, filename);
				attachmentPaths.add(filepath);
			}
		}

		return attachmentPaths;
	}

	private void addAttachments(CombineArchive archive, List<Path> attachmentFilePaths) {
		for (Path attachmentFilePath : attachmentFilePaths) {
			File attachment = attachmentFilePath.toFile(); 
			URI filetype = determineFiletype(attachment);

			try {
				archive.addEntry(attachment, attachmentFilePath.getFileName().toString(), filetype);
			} catch (IOException e) {
				System.err.print("Error adding file named ");
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

		List<Path> attachmentFilenames = findAttachmentFiles();
		System.err.println(attachmentFilenames);

		addSBOLFile(archive);
		addAttachments(archive, attachmentFilenames);
		addCreators(archive, creators);

		try {
			archive.pack();
			archive.close();

			finish(new BuildCombineArchiveResult(this, true, archive.getZipLocation().getAbsolutePath(), ""));
			return;
		} catch (IOException | TransformerException e) {
			finish(new BuildCombineArchiveResult(this, false, "", e.getMessage()));
			return;
		}
	}
}
