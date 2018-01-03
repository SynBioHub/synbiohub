package org.synbiohub;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.jdom2.JDOMException;

import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.CombineArchiveException;
import de.unirostock.sems.cbarchive.meta.omex.VCard;

public class BuildCombineArchiveJob extends Job {
	public String sbolFilename;
	public List<String> attachments;
	public List<HashMap<String, String>> creatorInfo;
	
	private URI determineFiletype(File file) {
		return null;
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
	
	private void addAttachments(CombineArchive archive) {
		for (String attachmentName : attachments) {
			File attachment = new File(attachmentName);
			URI filetype = determineFiletype(attachment);
			
			// TODO
			// ArchiveEntry entry = archive.addEntry()
		}
	}

	public void execute() {
		List<VCard> creators = createCreators(creatorInfo);
		CombineArchive archive = createCombineArchive();
		
		if(archive == null) {
			return;
		}
		
		addAttachments(archive);
	
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
