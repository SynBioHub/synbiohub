package org.synbiohub;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
		
		SBOLDocument doc = SBOLValidate.validate(
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
				false,
				null,
				false,
				true,
				false);
		
		String log = new String(logOutputStream.toByteArray(), StandardCharsets.UTF_8);
		String errorLog = new String(errorOutputStream.toByteArray(), StandardCharsets.UTF_8);

		if(errorLog.length() > 0)
		{
			System.err.println("ERROR: " + errorLog);
			finish(new RDFToSBOLResult(this, false, "", log, errorLog));
			return;
		}
		
		for (String registry : webOfRegistries.keySet()) {
			doc.addRegistry(webOfRegistries.get(registry),registry);
		}
		//completeDocument(doc);
		for (TopLevel topLevel : doc.getTopLevels()) {
			if (topLevel instanceof Collection) {
				((Collection) topLevel).getMembers();
			}
		}
		
		doc.clearRegistries();
		// Restores nested annotations
		for (TopLevel topLevel : doc.getTopLevels()) {
			inlineNestedAnnotations(doc,topLevel,topLevel.getAnnotations());
		}

		File resultFile = File.createTempFile("sbh_rdf_to_sbol", ".xml");

		SBOLWriter.write(doc, resultFile);

		finish(new RDFToSBOLResult(this, true, resultFile.getAbsolutePath(), log, errorLog));

	}
	
	private static void inlineNestedAnnotations(SBOLDocument doc, TopLevel topLevel, List<Annotation> annotations) throws SBOLValidationException {
		for (Annotation annotation : annotations) {
			if (annotation.isURIValue()) {
				URI genericTopLevelURI = annotation.getURIValue();
				GenericTopLevel genericTopLevel = doc.getGenericTopLevel(genericTopLevelURI);
				if (genericTopLevel != null && !genericTopLevelURI.equals(topLevel.getIdentity())) {
					URI topLevelURI = null;
					for (Annotation annotation2 : genericTopLevel.getAnnotations()) {
						if (annotation2.getQName().getNamespaceURI().equals("http://wiki.synbiohub.org/wiki/Terms/synbiohub#") &&
								annotation2.getQName().getLocalPart().equals("topLevel")) {
							topLevelURI = annotation2.getURIValue();
							break;
						}
					}
					if (topLevelURI!=null && topLevelURI.equals(topLevel.getIdentity())) {
						annotation.setAnnotations(genericTopLevel.getAnnotations());
						annotation.setNestedIdentity(genericTopLevel.getIdentity());
						annotation.setNestedQName(genericTopLevel.getRDFType());
						doc.removeGenericTopLevel(genericTopLevel);
						inlineNestedAnnotations(doc,topLevel,annotation.getAnnotations());
					}
				}
			}
		}	
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
		if (topLevel instanceof GenericTopLevel || topLevel instanceof Sequence || topLevel instanceof Model
				|| topLevel instanceof Plan || topLevel instanceof Agent || topLevel instanceof Attachment) {
			// Do nothing
		} else if (topLevel instanceof Implementation) {
			if (((Implementation)topLevel).getBuilt()!=null) {
				completeDocument(document,((Implementation)topLevel).getBuilt());
			}
		} else if (topLevel instanceof Activity) {
			Activity activity = (Activity)topLevel;
			for (Activity wasInformedBy : activity.getWasInformedBys()) {
				completeDocument(document,wasInformedBy);
			}
			for (Association association : activity.getAssociations()) {
				if (association.getPlan()!=null) {
					completeDocument(document,association.getPlan());
				}
				if (association.getAgent()!=null) {
					completeDocument(document,association.getAgent());
				}
			}
		} else if (topLevel instanceof CombinatorialDerivation) {
			CombinatorialDerivation combinatorialDerivation = (CombinatorialDerivation)topLevel;
			if (combinatorialDerivation.getTemplate()!=null) {
				completeDocument(document,combinatorialDerivation.getTemplate());
			}
			for (VariableComponent variableComponent : combinatorialDerivation.getVariableComponents()) {
				for (ComponentDefinition cd : variableComponent.getVariants()) {
					completeDocument(document,cd);
				}
				for (Collection c : variableComponent.getVariantCollections()) {
					completeDocument(document,c);
				}
				for (CombinatorialDerivation d : variableComponent.getVariantDerivations()) {
					completeDocument(document,d);
				}
			}
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
			for (org.sbolstandard.core2.Module module : ((ModuleDefinition)topLevel).getModules()) {
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
				if (tl != null && !tl.getIdentity().equals(topLevel.getIdentity())) {
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
