package org.synbiohub;

import java.net.URI;
import java.util.HashMap;

import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Identified;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.Location;
import org.sbolstandard.core2.MapsTo;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.Participation;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.SequenceConstraint;
import org.sbolstandard.core2.TopLevel;

public abstract class IdentifiedVisitor
{
	public abstract void visit(Identified identified);
	
	public void visitDocument(SBOLDocument sbolDocument)
	{
		for (TopLevel topLevel : sbolDocument.getTopLevels())
		{		
			visit(topLevel);
			
			if (topLevel instanceof ComponentDefinition) {
				for (Component c : ((ComponentDefinition) topLevel).getComponents()) {
					visit(c);
					for (MapsTo m : c.getMapsTos()) {
						visit(m);
					}
				}
				for (SequenceAnnotation sa : ((ComponentDefinition) topLevel).getSequenceAnnotations()) {
					visit(sa);
					for (Location l : sa.getLocations()) {
						visit(l);
					}
				}
				for (SequenceConstraint sc : ((ComponentDefinition) topLevel).getSequenceConstraints()) {
					visit(sc);
				}
			}
			else if (topLevel instanceof ModuleDefinition) {
				for (FunctionalComponent c : ((ModuleDefinition) topLevel).getFunctionalComponents()) {
					visit(c);
					for (MapsTo m : c.getMapsTos()) {
						visit(m);
					}
				}
				for (Module mod : ((ModuleDefinition) topLevel).getModules()) {
					visit(mod);
					for (MapsTo m : mod.getMapsTos()) {
						visit(m);
					}
				}
				for (Interaction i : ((ModuleDefinition) topLevel).getInteractions()) {
					visit(i);
					for (Participation p : i.getParticipations()) {
						visit(p);
					}
				}
			}
		}
	}


}
