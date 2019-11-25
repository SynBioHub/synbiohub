package org.oboparser.obo;

public class OBOIndividual extends OBOStanza {
	public OBOIndividual() { 
		super("Individual");
	}
	
	@Override
	public Object clone() { 
		return super.clone(OBOIndividual.class);
	}
}
