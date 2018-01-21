package org.synbiohub;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Date;

import javax.xml.transform.TransformerException;

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
	public List<String> attachments;
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

	private void addAttachments(CombineArchive archive) {
		for (String attachmentName : attachments) {
			File attachment = new File(attachmentName);
			URI filetype = determineFiletype(attachment);

			try {
				archive.addEntry(attachment, attachmentName, filetype);
			} catch (IOException e) {
				return;
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
		System.err.println("Beginning build!");
		List<VCard> creators = createCreators(creatorInfo);
		CombineArchive archive = createCombineArchive();
		
		if(archive == null) {
			return;
		}

		System.err.println("Adding files");
		
		addSBOLFile(archive);
		addAttachments(archive);
		addCreators(archive, creators);

		System.err.println("Files added, packing");
	
		try {
			archive.pack();
			archive.close();

			System.err.println("Packed!");
			
			finish(new BuildCombineArchiveResult(this, true, archive.getZipLocation().getAbsolutePath(), ""));
			return;
		} catch (IOException | TransformerException e) {
			finish(new BuildCombineArchiveResult(this, false, "", e.getMessage()));
			return;
		}
	}
}
