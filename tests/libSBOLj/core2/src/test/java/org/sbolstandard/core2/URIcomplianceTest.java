package org.sbolstandard.core2;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.sbolstandard.core2.URIcompliance.extractPersistentId;
import static org.sbolstandard.core2.URIcompliance.extractURIprefix;
import static org.sbolstandard.core2.URIcompliance.extractVersion;
import static org.sbolstandard.core2.URIcompliance.isDisplayIdValid;

import java.net.URI;

import org.junit.Test;

/**
 * @author Zhen Zhang
 *
 */
public class URIcomplianceTest {
	String uri1Str = "http://www.async.ece.utah.edu/LacI_Inv/1.0";
	String uri2Str = "http://www.AsYNc.eCe.utAH.edu/LacI_Inv/1.0";
	String uri3Str = "http://www.async.ece.utah.edu/LAcI_inV/1.0";
	String uri4Str = "http://www.async.ece.utah.edu/LacI_Inv/1/0";
	String uri5Str = "http://www.async.ece.utah.edu/LacI_Inv/LacIIn/1.0.1-alpha";
	String uri6Str = "http://www.async.ece.utah.edu/LacI_Inv/~LacIIn/1.0.1-alpha";
	String uri7Str = "http://www.async.ece.utah.edu/LacI_Inv/interaction_1/participant1/1.0.1-beta";
	String uri8Str = "http://www.async.ece.utah.edu/LacI_Inv/interaction_1/participant#1/1.0.1";
	String uri9Str = "http://www.async.ece.utah.edu/pLac/ptetlacISeq/multi_range/p2@struct&Annotate_range*/1.0.02-alpha";
	String uri10Str = "http://www.async.ece.utah.edu/pLac/ptetlacISeq/multi_range/_p2_structAnnotate_range/1.0.02-SNAPSHOT";
	String uri11Str = "www.async.ece.utah.edu";
	String uri12Str = "/";
	String uri13Str = "http://www.async.ece.utah.edu/LacI_Inv/";
	
	String uri1PersistIdStr = "http://www.async.ece.utah.edu/LacI_Inv";
	String uri5PersistIdStr = "http://www.async.ece.utah.edu/LacI_Inv/LacIIn";
	String uri7PersistIdStr = "http://www.async.ece.utah.edu/LacI_Inv/interaction_1/participant1";
	String uri10PersistIdStr = "http://www.async.ece.utah.edu/pLac/ptetlacISeq/multi_range/_p2_structAnnotate_range";
	String uri7prefixStr = "http://www.async.ece.utah.edu";
	String uri1toplevelDisplayId = "LacI_Inv";
	String uri5childDisplayId = "LacIIn";
	String uri7grandChildDisplayId = "interaction_1";
	String uri10greatGrandChildDisplayId = "_p2_structAnnotate_range";


	URI uri1 = URI.create(uri1Str);
	URI uri2 = URI.create(uri2Str);
	URI uri3 = URI.create(uri3Str);
	URI uri4 = URI.create(uri4Str);
	URI uri5 = URI.create(uri5Str);
	URI uri6 = URI.create(uri6Str);
	URI uri7 = URI.create(uri7Str);
	URI uri8 = URI.create(uri8Str);
	URI uri9 = URI.create(uri9Str);
	URI uri10 = URI.create(uri10Str);
	URI uri11 = URI.create(uri11Str);
	URI uri12 = URI.create(uri12Str);
	URI uri13 = URI.create(uri13Str);
	
	/**
	 * Test URL case
	 */
	@Test
	public void testURLcase() {
		assertEquals(uri1, uri2);
	}

	/**
	 * Test ID case
	 */
	@Test
	public void testIDcase() {
		assertThat(uri1, is(not(uri3)));
	}


	/**
	 * Test null for top-level URI not compliant in extractPersistentId.
	 */
	@Test
	public void testExtractPersistentId1() {
		if (extractPersistentId(uri4)==null) {
			return;
		}
		assert(false);
	}


	/**
	 * Test the extracted persistent ID string for the compliant top-level URI.
	 */
	@Test
	public void testExtractPersistentId2() {
		String extractedPersistentId = extractPersistentId(uri1);
		//System.out.println(extractedPersistentId);
		assertEquals(uri1PersistIdStr, extractedPersistentId);
	}

	/**
	 * Test null for child URI not compliant in extractPersistentId.
	 */
	@Test
	public void testExtractPersistentId3() {
		if (extractPersistentId(uri6)==null) {
			return;
		}
		assert(false);
	}

	/**
	 * Test the extracted persistent ID string for the compliant child URI.
	 */
	@Test
	public void testExtractPersistentId4() {
		String extractedPersistentId = extractPersistentId(uri5);
		//System.out.println(extractedPersistentId);
		assertEquals(uri5PersistIdStr, extractedPersistentId);
	}

	/**
	 * Test null for grand child URI not compliant in extractPersistentId.
	 */
	@Test
	public void testExtractPersistentId5() {
		if (extractPersistentId(uri8)==null) {
			return;
		}
		assert(false);
	}

	/**
	 * Test the extracted persistent ID string for the compliant grand child URI.
	 */
	@Test
	public void testExtractPersistentId6() {
		String extractedPersistentId = extractPersistentId(uri7);
		//System.out.println(extractedPersistentId);
		assertEquals(uri7PersistIdStr, extractedPersistentId);
	}

	/**
	 * Test null for great grand child URI not compliant in extractPersistentId.
	 */
	@Test
	public void testExtractPersistentId7() {
		if (extractPersistentId(uri9)==null) {
			return;
		}
		assert(false);
	}

	/**
	 * Test the extracted persistent ID string for the compliant great grand child URI.
	 */
	@Test
	public void testExtractPersistentId8() {
		String extractedPersistentId = extractPersistentId(uri10);
		//System.out.println(extractedPersistentId);
		assertEquals(uri10PersistIdStr, extractedPersistentId);
	}

	/**
	 * Test null for grand child URI not compliant in extractPersistentId.
	 */
	@Test
	public void testExtractURIprefix1() {
		String extractedPrefix = extractURIprefix(uri8);
		//System.out.println(extractedPersistentId);
		assertNull(extractedPrefix);
	}

	/**
	 * Test null for extractVersion for the compliant top-level URI.
	 */
	@Test
	public void testExtractVersion1() {
		String extractedVersion = extractVersion(uri4);
		//System.out.println(extractedVersion);
		assertNull(extractedVersion);
	}

	/**
	 * Test null for extractVersion for the compliant child URI.
	 */
	@Test
	public void testExtractVersion2() {
		String extractedVersion = extractVersion(uri6);
		//System.out.println(extractedVersion);
		assertNull(extractedVersion);
	}

	/**
	 * Test null for extractVersion for the compliant grand child URI.
	 */
	@Test
	public void testExtractVersion3() {
		String extractedVersion = extractVersion(uri8);
		//System.out.println(extractedVersion);
		assertNull(extractedVersion);

	}

	/**
	 * Test null for extractVersion for the compliant great grand child URI.
	 */
	@Test
	public void testExtractVersion4() {
		String extractedVersion = extractVersion(uri9);
		//System.out.println(extractedVersion);
		assertNull(extractedVersion);
	}

	/**
	 * Test the extracted version string for extractVersion for the compliant top-level URI.
	 */
	@Test
	public void testExtractVersion5() {
		String extractedVersion = extractVersion(uri1);
		//System.out.println(extractedVersion);
		assertEquals("1.0", extractedVersion);
	}

	/**
	 * Test the extracted version string for extractVersion for the compliant child URI.
	 */
	@Test
	public void testExtractVersion6() {
		String extractedVersion = extractVersion(uri5);
		//System.out.println(extractedVersion);
		assertEquals("1.0.1-alpha", extractedVersion);
	}

	/**
	 * Test the extracted version string for extractVersion for the compliant grand child URI.
	 */
	@Test
	public void testExtractVersion7() {
		String extractedVersion = extractVersion(uri7);
		//System.out.println(extractedVersion);
		assertEquals("1.0.1-beta", extractedVersion);
	}

	/**
	 * Test the extracted version string for extractVersion for the compliant great grand child URI.
	 */
	@Test
	public void testExtractVersion8() {
		String extractedVersion = extractVersion(uri10);
		//System.out.println(extractedVersion);
		assertEquals("1.0.02-SNAPSHOT", extractedVersion);
	}

	/**
	 * Test is displayId compliant
	 */
	@Test
	public void testIsDisplayIdCompliant1() {
		assertFalse(isDisplayIdValid("1/0"));
	}

	/**
	 * Test is displayId compliant
	 */
	@Test
	public void testIsDisplayIdCompliant2() {
		assertFalse(isDisplayIdValid("+asYnc513+"));
	}

	/**
	 * Test is displayId compliant
	 */
	@Test
	public void testIsDisplayIdCompliant3() {
		assertTrue(isDisplayIdValid("_l2I3DDv"));
	}
}
