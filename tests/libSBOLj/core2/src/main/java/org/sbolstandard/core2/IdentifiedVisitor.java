package org.sbolstandard.core2;

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
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.SequenceConstraint;
import org.sbolstandard.core2.TopLevel;

/**
 * A visitor for every identified object in an SBOLDocument
 * 
 * @author Chris Myers
 * @author James McLaughlin
 * @version 2.3.0
 */

public abstract class IdentifiedVisitor
{
	/**
	 * Visit an identified object
	 * @param identified identified object to visit
	 * @param topLevel topLevel that this object is a contained in.
	 */
	public abstract void visit(Identified identified, TopLevel topLevel);
	
	/**
	 * Visits all identified objects within an SBOL document
	 * @param sbolDocument SBOL document to visit
	 */
	public void visitDocument(SBOLDocument sbolDocument)
	{
		for (TopLevel topLevel : sbolDocument.getTopLevels())
		{		
			visit(topLevel, topLevel);
			
			if (topLevel instanceof ComponentDefinition) {
				for (Component c : ((ComponentDefinition) topLevel).getComponents()) {
					visit(c, topLevel);
					for (MapsTo m : c.getMapsTos()) {
						visit(m, topLevel);
					}
					for (Location l : c.getLocations()) {
						visit(l, topLevel);
					}
					for (Location s : c.getSourceLocations()) {
						visit(s, topLevel);
					}
					for (Measure m : c.getMeasures()) {
						visit(m, topLevel);
					}
				}
				for (SequenceAnnotation sa : ((ComponentDefinition) topLevel).getSequenceAnnotations()) {
					visit(sa,topLevel);
					for (Location l : sa.getLocations()) {
						visit(l,topLevel);
					}
				}
				for (SequenceConstraint sc : ((ComponentDefinition) topLevel).getSequenceConstraints()) {
					visit(sc,topLevel);
				}
			}
			else if (topLevel instanceof ModuleDefinition) {
				for (FunctionalComponent c : ((ModuleDefinition) topLevel).getFunctionalComponents()) {
					visit(c,topLevel);
					for (MapsTo m : c.getMapsTos()) {
						visit(m,topLevel);
					}
					for (Measure m : c.getMeasures()) {
						visit(m, topLevel);
					}
				}
				for (Module mod : ((ModuleDefinition) topLevel).getModules()) {
					visit(mod,topLevel);
					for (MapsTo m : mod.getMapsTos()) {
						visit(m,topLevel);
					}
					for (Measure m : mod.getMeasures()) {
						visit(m, topLevel);
					}
				}
				for (Interaction i : ((ModuleDefinition) topLevel).getInteractions()) {
					visit(i,topLevel);
					for (Participation p : i.getParticipations()) {
						for (Measure m : p.getMeasures()) {
							visit(m, topLevel);
						}
						visit(p,topLevel);
					}
					for (Measure m : i.getMeasures()) {
						visit(m, topLevel);
					}
				}
			} else if (topLevel instanceof Activity) {
				for (Association a : ((Activity) topLevel).getAssociations()) {
					visit(a,topLevel);
				}
				for (Usage u : ((Activity)topLevel).getUsages()) {
					visit(u,topLevel);
				}
			} else if (topLevel instanceof CombinatorialDerivation) {
				for (VariableComponent v : ((CombinatorialDerivation)topLevel).getVariableComponents()) {
					visit(v,topLevel);
				}
			}
		}
	}

}
