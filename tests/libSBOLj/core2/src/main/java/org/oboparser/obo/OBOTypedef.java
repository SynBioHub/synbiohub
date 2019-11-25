package org.oboparser.obo;

public class OBOTypedef extends OBOStanza {

	public OBOTypedef() {
		super("Typedef");
	}

	@Override
	public Object clone() { 
		return super.clone(OBOTypedef.class);
	}

}
