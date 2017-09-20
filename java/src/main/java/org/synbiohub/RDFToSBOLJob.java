package org.synbiohub;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.sbolstandard.core2.*;

import javax.xml.namespace.QName;

public class RDFToSBOLJob extends Job
{
	public String sbolFilename;
	public String uriPrefix;
	public boolean requireComplete;
	public boolean requireCompliant;
	public boolean enforceBestPractices;
	public boolean typesInURI;
	public String version;
	public boolean keepGoing;
	public HashMap<String,String> webOfRegistries;

	public void execute() throws Exception
	{
		ByteArrayOutputStream logOutputStream = new ByteArrayOutputStream();
		ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();
		
		SBOLDocument doc = SBOLValidateSilent.validate(
				new PrintStream(logOutputStream),
				new PrintStream(errorOutputStream),
				sbolFilename,
				uriPrefix,
				requireComplete,
				requireCompliant, 
				enforceBestPractices,
				typesInURI,
				version,
				keepGoing,
				"",
				"",
				sbolFilename,
				"",
				false,
				false,
				false,
				null,
				false,
				true);
		
		for (String registry : webOfRegistries.keySet()) {
			doc.addRegistry(webOfRegistries.get(registry),registry);
		}
		completeDocument(doc);
		
		String log = new String(logOutputStream.toByteArray(), StandardCharsets.UTF_8);
		String errorLog = new String(errorOutputStream.toByteArray(), StandardCharsets.UTF_8);

		if(errorLog.length() > 0)
		{
			finish(new RDFToSBOLResult(this, false, "", log, errorLog));
			return;
		}

		File resultFile = File.createTempFile("sbh_rdf_to_sbol", ".xml");

		SBOLWriter.write(doc, resultFile);

		finish(new RDFToSBOLResult(this, true, resultFile.getAbsolutePath(), log, errorLog));

	}
	
	private Set<URI> completed;
	
	private void completeDocument(SBOLDocument document) throws SBOLValidationException {
		completed = new HashSet<URI>();
		int size = document.getTopLevels().size();
		int count = 0;
		for (TopLevel topLevel : document.getTopLevels()) {
			completeDocument(document,topLevel);
			count++;
			System.err.println(count + " out of " + size);
		}	
	}
	
	private void completeDocument(SBOLDocument document, Annotation annotation) throws SBOLValidationException {
		if (annotation.isURIValue()) {
			TopLevel gtl = document.getTopLevel(annotation.getURIValue());
			if (gtl != null) 
				completeDocument(document,gtl);
		} else if (annotation.isNestedAnnotations()) {
			for (Annotation nestedAnnotation : annotation.getAnnotations()) {
				completeDocument(document,nestedAnnotation);
			}
		}
	}
	
	/**
	 * @param document
	 * @param topLevel
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link SBOLDocument#createCopy(TopLevel)}.
	 */
	private void completeDocument(SBOLDocument document, TopLevel topLevel) throws SBOLValidationException {
	    if (topLevel==null) return;
		if (completed.contains(topLevel.getIdentity())) return;
		System.err.println("Completing:"+topLevel.getIdentity());
		completed.add(topLevel.getIdentity());
		if (topLevel instanceof GenericTopLevel || topLevel instanceof Sequence || topLevel instanceof Model) {
			// Do nothing
		} else if (topLevel instanceof Collection) {
			for (TopLevel member : ((Collection)topLevel).getMembers()) {
				completeDocument(document,member);
			}
		} else if (topLevel instanceof ComponentDefinition) {
			for (Component component : ((ComponentDefinition)topLevel).getComponents()) {
				if (component.getDefinition()!=null) {
					completeDocument(document,component.getDefinition());
				}
			}
			for (TopLevel sequence : ((ComponentDefinition)topLevel).getSequences()) {
				completeDocument(document,sequence);
			}
		} else if (topLevel instanceof ModuleDefinition) {
			for (FunctionalComponent functionalComponent : ((ModuleDefinition)topLevel).getFunctionalComponents()) {
				if (functionalComponent.getDefinition()!=null) {
					completeDocument(document,functionalComponent.getDefinition());
				}
			}
			for (Module module : ((ModuleDefinition)topLevel).getModules()) {
				if (module.getDefinition()!=null) {
					completeDocument(document,module.getDefinition());
				}
			}
			for (Model model : ((ModuleDefinition)topLevel).getModels()) {
				completeDocument(document,model);
			}
		}
		for (Annotation annotation : topLevel.getAnnotations()) {
			if (annotation.isURIValue()) {
				TopLevel tl = document.getTopLevel(annotation.getURIValue());
				if (tl != null) {
					completeDocument(document,tl);
					if (tl instanceof GenericTopLevel) {
						GenericTopLevel gtl = (GenericTopLevel)tl;
						Annotation topLevelAnnot = gtl.getAnnotation(new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#","topLevel","sbh"));
						if (topLevelAnnot!=null && topLevelAnnot.isURIValue() && 
								topLevelAnnot.getURIValue().equals(topLevel.getIdentity())) {
							annotation.setNestedQName(gtl.getRDFType());
							annotation.setNestedIdentity(gtl.getIdentity());
							annotation.setAnnotations(gtl.getAnnotations());
							document.removeGenericTopLevel(gtl);
						}
					}
				}
			} else if (annotation.isNestedAnnotations()) {
				for (Annotation nestedAnnotation : annotation.getAnnotations()) {
					completeDocument(document,nestedAnnotation);
				}
			}
		}
	}
}
