package org.sbolstandard.core2.Testing;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sbolstandard.core2.Collection;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;

public class CollectionTest {
	private SBOLDocument doc = null;
	private ComponentDefinition TetR_promoter = null;
	private ComponentDefinition gRNA_b_promoter = null;
	private ComponentDefinition CRa_U6_promoter = null;
	private Collection promoters = null;
	@Before
	public void setUp() throws Exception {
		String prURI="http://partsregistry.org";
		doc = new SBOLDocument();
		doc.setDefaultURIprefix(prURI);
		doc.setComplete(true);
		TetR_promoter = doc.createComponentDefinition("TetR_promoter","", ComponentDefinition.DNA_REGION); 
		gRNA_b_promoter = doc.createComponentDefinition("gRNA_b_promoter","", ComponentDefinition.DNA_REGION); 
		CRa_U6_promoter = doc.createComponentDefinition("CRa_U6","",ComponentDefinition.DNA_REGION);
		promoters = doc.createCollection("promoters");
		promoters.addMember(TetR_promoter.getIdentity());
		promoters.addMember(gRNA_b_promoter.getIdentity());
		promoters.addMember(CRa_U6_promoter.getIdentity());
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void test_removeMember()
	{
		assertTrue(promoters.removeMember(TetR_promoter.getIdentity()));
		assertTrue(promoters.getMembers().size() == 2);
	}
	
	@Test
	public void test_toString()
	{
		assertTrue(promoters.toString().length() != 0);
		assertNotNull(promoters.toString());
		assertTrue(promoters.toString().contains("identity="+promoters.getIdentity()));
		assertTrue(!promoters.toString().contains("description="));
		assertTrue(!promoters.toString().contains("name="));
	}
	
	@Test
	public void test_getCollection()
	{
		assertTrue(doc.getCollection("promoters", "").getMembers().size() == 3);	
	}
	

}
