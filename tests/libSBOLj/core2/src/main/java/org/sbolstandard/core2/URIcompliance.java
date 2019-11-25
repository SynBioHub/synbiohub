package org.sbolstandard.core2;

import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides functionality to validate URI compliance. 
 * 
 * @author Chris Myers
 * @author Zhen Zhang
 * @version 2.1
 */

final class URIcompliance {
	
	/**
	 * @param displayId
	 * @param version
	 * @throws SBOLValidationException if either of the following SBOL validation rules was violated: 10204, 10206.
	 */
	private static void validateIdVersion(String displayId, String version) throws SBOLValidationException {
		if (displayId!=null && !isDisplayIdValid(displayId)) {
			throw new SBOLValidationException("sbol-10204");
		}
		if (version!=null && !isVersionValid(version)) {
			throw new SBOLValidationException("sbol-10206");
		}
	}

	/**
	 * @param prefix
	 * @param displayId
	 * @param version
	 * @return
	 * @throws SBOLValidationException if an SBOL validation exception occurred in {@link URIcompliance#validateIdVersion(String, String)}.
	 */
	static URI createCompliantURI(String prefix, String displayId, String version) throws SBOLValidationException {
		if (prefix == null) {
			throw new IllegalArgumentException("The defaultURIprefix is not set. Please set it to a non-null value");
		}
		if (displayId==null) {
			throw new SBOLValidationException("sbol-10204");
		}
		validateIdVersion(displayId, version);
		if (!prefix.endsWith("/") && !prefix.endsWith(":") && !prefix.endsWith("#")) {
			prefix += "/";
		}
		if (version==null || version.equals("")) {
			return URI.create(prefix + displayId);
		}
		return URI.create(prefix + displayId + '/' + version);
	}
	
	/**
	 * @param prefix
	 * @param type
	 * @param displayId
	 * @param version
	 * @param useType
	 * @return
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in either of the following methods:
	 * <ul>
	 * <li>{@link #validateIdVersion(String, String)}, or</li>
	 * <li>{@link #createCompliantURI(String, String, String)}.</li>
	 * </ul> 
	 */
	static URI createCompliantURI(String prefix, String type, String displayId, String version, boolean useType) throws SBOLValidationException {
		if (prefix == null) {
			throw new IllegalArgumentException("The defaultURIprefix is not set. Please set it to a non-null value");
		}
		if (displayId==null) {
			throw new SBOLValidationException("sbol-10204");
		}
		validateIdVersion(displayId, version);
		if (!useType) return createCompliantURI(prefix,displayId,version);
		if (!prefix.endsWith("/") && !prefix.endsWith(":") && !prefix.endsWith("#")) {
			prefix += "/";
		}
		if (version==null || version.equals("")) {
			return URI.create(prefix + type + '/' + displayId);
		}
		return URI.create(prefix + type + '/' + displayId + '/' + version);
	}

	/**
	 * Extract the persistent identity URI from the given URI.
	 * 
	 * @return the extracted persistent identity URI
	 */
	static String extractPersistentId(URI objURI) {
		String URIstr = objURI.toString();
		Matcher m = genericURIpattern1Pat.matcher(URIstr);
		if (m.matches()) {
			return m.group(1);
		}
		else {
			return null;
		}

	}

	/**
	 * Extract the URI prefix from this object's identity URI.
	 * 
	 * @return the extracted URI prefix
	 */
	static String extractURIprefix(URI objURI) {
		String URIstr = objURI.toString();
		Matcher m = genericURIpattern1bPat.matcher(URIstr);
		if (m.matches())
			return m.group(2);
		else
			return null;
	}

	/**
	 * Extract the URI prefix from this object's identity URI.
	 * 
	 * @return the extracted URI prefix
	 */
	static String extractSimpleNamespace(URI objURI) {
		String URIstr = objURI.toString();
		Matcher m = genericURIpattern1Pat.matcher(URIstr);
		if (m.matches())
			return m.group(2);
		else
			return null;
	}
	
	/**
	 * Extract the URI prefix from this object's identity URI.
	 * 
	 * @return the extracted URI prefix
	 */
	static String extractNamespace(URI objURI) {
		String URIstr = objURI.toString();
		Matcher m = namespacePatternPat.matcher(URIstr);
		if (m.matches())
			return m.group(2);
		else
			return null;
	}

	
	/**
	 * Extract the object's display ID from the given object's identity URI.
	 * 
	 * @return the extracted display ID
	 */
	static String extractDisplayId(URI objURI) {
		String URIstr = objURI.toString();
		Matcher m = genericURIpattern1Pat.matcher(URIstr);
		if (m.matches()) {
			return m.group(4);
		}
		else
			return null;
	}

	/**
	 * Extract the version from this object's identity URI.
	 * 
	 * @return the version if the given URI is compliant
	 */
	static String extractVersion(URI objURI) {
		String URIstr = objURI.toString();
		Matcher m = genericURIpattern1Pat.matcher(URIstr);
		if (m.matches() && m.groupCount()>=6)
			return m.group(6);
		else
			return null;
	}

	/**
	 * @param identified
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 10215, 10216, 10218.
	 */
	static final void isURIcompliant(Identified identified) throws SBOLValidationException {
		if (!identified.isSetDisplayId()) {
			throw new SBOLValidationException("sbol-10215");
		}
		if (!identified.isSetPersistentIdentity()) {
			throw new SBOLValidationException("sbol-10216");
		}
		if (!identified.getPersistentIdentity().toString().endsWith("/"+identified.getDisplayId()) &&
			!identified.getPersistentIdentity().toString().endsWith("#"+identified.getDisplayId()) &&
			!identified.getPersistentIdentity().toString().endsWith(":"+identified.getDisplayId())) {
			throw new SBOLValidationException("sbol-10216");
		}
		if (!identified.isSetVersion()) {
			if (!identified.getIdentity().toString().equals(identified.getPersistentIdentity().toString())) {
				throw new SBOLValidationException("sbol-10218");
			}
		} else {
			if (!identified.getIdentity().toString().equals(identified.getPersistentIdentity().toString()+"/"
					+identified.getVersion())) {
				throw new SBOLValidationException("sbol-10218");
			}
		}
	}
	
	// TODO: this method is only checking URIs and not other fields.  It also is only allowing / delimiter.
	// Seems to be needed for addChildSafely method.  Should investigate further.
	static final boolean isChildURIformCompliant(URI parentURI, URI childURI) {
		String parentPersistentId = extractPersistentId(parentURI);
		if (parentPersistentId==null) return false;
		String childDisplayId = extractDisplayId(childURI);
		if (childDisplayId==null) return false;
		String parentVersion = extractVersion(parentURI);
		if (parentVersion == null) {
			return childURI.toString().equals(parentPersistentId+"/"+childDisplayId);
		} else {
			return childURI.toString().equals(parentPersistentId+"/"+childDisplayId+"/"+parentVersion);
		}
	}

	/**
	 * @param parent
	 * @param child
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 10216, 10217, 10219.
	 */
	static final void isChildURIcompliant(Identified parent, Identified child) throws SBOLValidationException {
		try {
			isURIcompliant(child);
		} catch (SBOLValidationException e) {
			if (e.getMessage().contains("sbol-10216")) {
				throw new SBOLValidationException("sbol-10217");
			} else {
				throw new SBOLValidationException(e.getMessage());
			}
		}
		if (!child.getPersistentIdentity().toString().equals(parent.getPersistentIdentity()+"/"+child.getDisplayId()) &&
				!child.getPersistentIdentity().toString().equals(parent.getPersistentIdentity()+"#"+child.getDisplayId()) &&
				!child.getPersistentIdentity().toString().equals(parent.getPersistentIdentity()+":"+child.getDisplayId())) {
			throw new SBOLValidationException("sbol-10217");
		}
		if (parent.isSetVersion()) {
			if (!child.isSetVersion()||!child.getVersion().equals(parent.getVersion())) {
				throw new SBOLValidationException("sbol-10219");
			}
		} else if (child.isSetVersion()) {
			throw new SBOLValidationException("sbol-10219");
		}
	}
	
//	/**
//	 * Test if the given object's identity URI is compliant with the form {@code ⟨prefix⟩/(⟨displayId⟩/)}{1,3}⟨version⟩.
//	 * The prefix is established by the owner of this object. The number of displayIds can range from 1 to 4, depending on
//	 * the level of the given object.
//	 * 
//	 * @param objURI
//	 * @throws SBOLValidationException if any the following SBOL validation rule was violated: 10201.
//	 */
//	static final void isTopLevelURIformCompliant(URI topLevelURI) throws SBOLValidationException {
//		Pattern r;
//		String URIstr = topLevelURI.toString();		
//		r = Pattern.compile(toplevelURIpattern);
//		Matcher m = r.matcher(URIstr);
//		if (!m.matches()) {
//			throw new SBOLValidationException("sbol-10201");
//		}
//	}
	
//	static final boolean isURIcompliantTemp(URI objURI, String URIprefix, String version, String ... displayIds) {
//		if (displayIds.length == 0 || displayIds.length > 4) {
//			// Wrong number of display IDs.
//			return false;
//		}
//		if (objURI == null) {
//			return false;
//		}
//		String URIstr = objURI.toString();
//		if (URIstr.isEmpty()) {
//			return false;
//		}
//		String[] extractedURIpieces = URIstr.split("/");
//		if (extractedURIpieces.length < 4) { // minimal number of "/" should be 4, such as "http://www.async.ece.utah.edu/LacI_Inv/".
//			return false;
//		}
//		int curIndex = extractedURIpieces.length - 1;
//		String curExtractedStr = extractedURIpieces[curIndex];
//		if (URIstr.endsWith("/")) { // version is empty. The last string extracted from the URI should be the display ID.
//			if (isDisplayIdCompliant(curExtractedStr) && curExtractedStr.equals(displayIds[0])) {
//				String extractedURIprefix = "";
//				String parentDisplayId = null; 				// codereview: assignment to null is redundant - this is set in all code-paths, right?
//				String grandparentDisplayId = null;			// codereview: assignment to null is redundant - this is set in all code-paths, right?
//				String greatGrandparentDisplayId = null;	// codereview: assignment to null is redundant - this is set in all code-paths, right?
//				switch (displayIds.length) {				
//				case 1: // Only one display ID is provided. Should be a top-level object.
//					break;
//				case 2: // Two display IDs are provided. Should be a child of a top-level object.
//					parentDisplayId = displayIds[1];
//					curIndex = curIndex - 1;
//					curExtractedStr = extractedURIpieces[curIndex]; // parent display ID extracted from URI.
//					if (isDisplayIdCompliant(parentDisplayId) && parentDisplayId.equals(curExtractedStr)) {
//						break;
//					}
//					else {
//						// Parent display ID not compliant
//						return false;
//					}
//				case 3: // Three display IDs are provided. Should be a grandchild of a top-level object.
//					parentDisplayId = displayIds[1];
//					curIndex = curIndex - 1;
//					curExtractedStr = extractedURIpieces[curIndex]; // parent display ID extracted from URI.
//					if (isDisplayIdCompliant(parentDisplayId) && parentDisplayId.equals(curExtractedStr)) {
//						grandparentDisplayId = displayIds[2];
//						curIndex = curIndex - 1;
//						curExtractedStr = extractedURIpieces[curIndex]; // grandparent display ID extracted from URI.
//						if (isDisplayIdCompliant(grandparentDisplayId) 
//								&& grandparentDisplayId.equals(curExtractedStr)) {
//							break;
//						}
//						else {
//							// Grandparent display ID not compliant
//							return false;
//						}
//					}
//					else {
//						// Parent display ID not compliant
//						return false;
//					}
//				case 4: // Four display IDs are provided. Should be a great grandchild of a top-level object.
//					parentDisplayId = displayIds[1];
//					curIndex = curIndex - 1;
//					curExtractedStr = extractedURIpieces[curIndex]; // parent display ID extracted from URI.
//					if (isDisplayIdCompliant(parentDisplayId) && parentDisplayId.equals(curExtractedStr)) {
//						grandparentDisplayId = displayIds[2];
//						curIndex = curIndex - 1;
//						curExtractedStr = extractedURIpieces[curIndex]; // grandparent display ID extracted from URI.
//						if (isDisplayIdCompliant(grandparentDisplayId) 
//								&& grandparentDisplayId.equals(curExtractedStr)) {
//							greatGrandparentDisplayId = displayIds[3];
//							curIndex = curIndex - 1;
//							curExtractedStr = extractedURIpieces[curIndex]; // great grandparent display ID extracted from URI.
//							if (isDisplayIdCompliant(greatGrandparentDisplayId) 
//								&& greatGrandparentDisplayId.equals(curExtractedStr)) {
//								break;
//							}
//							else {
//								// Great grandparent display ID not compliant
//								return false;
//							}
//						}
//						else {
//							// Grandparent display ID not compliant
//							return false;
//						}
//					}
//					else {
//						// Parent display ID not compliant
//						return false;
//					}
//				default:
//					return false;				
//				}
//				for (int i = 0; i < curIndex; i++) {
//					extractedURIprefix = extractedURIprefix + extractedURIpieces[i];
//				}
//				// URI prefix not compliant
//				return isURIprefixCompliant(extractedURIprefix) && extractedURIprefix.equals(URIprefix);
//			}
//			else { // displayId not compliant
//				return false;
//			}
//		}
//		else { // version is not empty
//			//String displayId = extractedURIpieces[versionIndex];
//// version not compliant
//			return isVersionCompliant(curExtractedStr);
//		}
//	}

	static boolean isDisplayIdValid(String newDisplayId) {
		if (newDisplayId==null) return false;
		Matcher m = displayIDpatternPat.matcher(newDisplayId);
		return m.matches();
	}

	static boolean isVersionValid(String version) {
		if (version.equals("")) return true;
		Matcher m = versionPatternPat.matcher(version);
		return m.matches();
	}

	static boolean isURIprefixCompliant(String URIprefix) {
		Matcher m = URIprefixPatternPat.matcher(URIprefix);
		return m.matches();
	}
	
	// TODO: this is the proper URI matcher, but it is very open, should we use it?
	static boolean isValidURI(String URIstr) {
		Pattern r = Pattern.compile(URI_REFERENCE_REGEX);
		Matcher m = r.matcher(URIstr);
		if (m.matches()) {
			return true;
		} else {
			return false;
		}
	}

	// (?:...) is a non-capturing group
	//static final String URIprefixPattern = "\\b(?:https?|ftp|file)://[-a-zA-Z0-9+&@#%?=~_|!:,.;]*[-a-zA-Z0-9+&@#%=~_|]";
	
	private static final String delimiter = "[/|#|:]";
	
	private static final String protocol = "(?:https?|ftp|file)://";

	private static final String URIprefixPattern = "\\b(?:"+protocol+")?[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

	private static final Pattern URIprefixPatternPat = Pattern.compile(URIprefixPattern + delimiter);

	private static final String displayIDpattern = "[a-zA-Z_]+[a-zA-Z0-9_]*";//"[a-zA-Z0-9_]+";

	private static final Pattern displayIDpatternPat = Pattern.compile(displayIDpattern);

	private static final String versionPattern = "[0-9]+[a-zA-Z0-9_\\.-]*"; // ^ and $ are the beginning and end of the string anchors respectively. 
															// | is used to denote alternates. 

	private static final Pattern versionPatternPat = Pattern.compile(versionPattern);

	// A URI can have up to 4 display IDs. The one with 4 display IDs can be ComponentDefinition -> SequenceAnnotation -> (Location) MultiRange -> Range.
	// group 1: persistent ID
	// group 2: URI prefix
	// group 3: version
	private static final String genericURIpattern1 = "((" + URIprefixPattern + ")(" + delimiter+"(" + displayIDpattern + ")))(/(" + versionPattern + "))?";

	private static final Pattern genericURIpattern1Pat = Pattern.compile(genericURIpattern1);
	
	private static final String genericURIpattern1b = "((" + URIprefixPattern + delimiter+")(" + displayIDpattern + "))(/(" + versionPattern + "))?";

	private static final Pattern genericURIpattern1bPat = Pattern.compile(genericURIpattern1b);

	private static final String namespacePattern = "((" + URIprefixPattern + delimiter + ")(" + displayIDpattern + "))(/(" + versionPattern + "))?";

	private static final Pattern namespacePatternPat = Pattern.compile(namespacePattern);

	// A URI can have up to 4 display IDs. The one with 4 display IDs can be ComponentDefinition -> SequenceAnnotation -> (Location) MultiRange -> Range.
	// group 1: top-level display ID
	// group 2: top-level's child display ID
	// group 3: top-level's grand child display ID
	// group 4: top-level's grand grand child display ID
	//static final String genericURIpattern2 = URIprefixPattern + delimiter + "((" + displayIDpattern + "/){1,3})" + versionPattern;

	//private static final String toplevelURIpattern = URIprefixPattern + delimiter + displayIDpattern + "(/" + versionPattern + ")?";
	
	private static final String URI_REFERENCE_REGEX = "(([a-zA-Z][a-zA-Z0-9\\+\\-\\.]*:((((//((((([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\);:\\&=\\+$,]|(%[a-fA-F0-9]{2}))*)\\@)?((((([a-zA-Z0-9](([a-zA-Z0-9\\-])*[a-zA-Z0-9])?)\\.)*([a-zA-Z](([a-zA-Z0-9\\-])*[a-zA-Z0-9])?)(\\.)?)|([0-9]+((\\.[0-9]+){3})))(:[0-9]*)?))?|([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\)$,;:\\@\\&=\\+]|(%[a-fA-F0-9]{2}))+)(/(([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\):\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*(;([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\):\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*)*)(/(([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\):\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*(;([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\):\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*)*))*)?)|(/(([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\):\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*(;([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\):\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*)*)(/(([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\):\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*(;([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\):\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*)*))*))(\\?([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\);/\\?:\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*)?)|(([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\);\\?:\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\);/\\?:\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*)))|(((//((((([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\);:\\&=\\+$,]|(%[a-fA-F0-9]{2}))*)\\@)?((((([a-zA-Z0-9](([a-zA-Z0-9\\-])*[a-zA-Z0-9])?)\\.)*([a-zA-Z](([a-zA-Z0-9\\-])*[a-zA-Z0-9])?)(\\.)?)|([0-9]+((\\.[0-9]+){3})))(:[0-9]*)?))?|([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\)$,;:\\@\\&=\\+]|(%[a-fA-F0-9]{2}))+)(/(([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\):\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*(;([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\):\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*)*)(/(([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\):\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*(;([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\):\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*)*))*)?)|(/(([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\):\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*(;([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\):\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*)*)(/(([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\):\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*(;([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\):\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*)*))*)|(([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\);\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))+(/(([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\):\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*(;([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\):\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*)*)(/(([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\):\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*(;([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\):\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*)*))*)?))(\\?([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\);/\\?:\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*)?))?(\\#([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\);/\\?:\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*)?";

	
	//static final String childURIpattern = URIprefixPattern + delimiter + "(?:" + displayIDpattern + "/){2}" + versionPattern;

	//static final String grandchildURIpattern = URIprefixPattern + delimiter + "(?:" + displayIDpattern + "/){3}" + versionPattern;

	//static final String greatGrandchildURIpattern = URIprefixPattern + delimiter + "(?:" + displayIDpattern + "/){4}" + versionPattern;

	@SafeVarargs
	static boolean keyExistsInAnyMap(URI key, Map<URI, ?>... maps) {
		for(Map<URI, ?> map : maps) {
			if(map.keySet().contains(key))
				return true;
		}

		return false;
	}

	/**
	 * Check the given {@code URIprefix} to make sure it is not {@code null} and is compliant,
	 * and if URIprefix does not end with one of the following delimiters: "/", ":", or "#", then
	 * "/" is appended to the end of the given {@code URIprefix}.
	 *
	 * @param URIprefix
	 * @return URIprefix
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 10201. 
	 */
	static String checkURIprefix(String URIprefix) throws SBOLValidationException {
		if (URIprefix==null) {
			// TODO: not really the right exception here, this is our pattern and not the specs one
			throw new SBOLValidationException("sbol-10201");
		}
		if (!URIprefix.endsWith("/") && !URIprefix.endsWith(":") && !URIprefix.endsWith("#")) {
			URIprefix += "/";
		}
		if (!isURIprefixCompliant(URIprefix)) {
			// TODO: not really the right exception here, this is our pattern and not the specs one
			throw new SBOLValidationException("sbol-10201");
		}
		return URIprefix;
	}

	static String fixDisplayId(String displayId) {
		displayId = displayId.replaceAll("[^a-zA-Z0-9_]", "_");
		displayId = displayId.replace(" ", "_");
		if (Character.isDigit(displayId.charAt(0))) {
			displayId = "_" + displayId;
		}
		return displayId;
	}
	
	static String findDisplayId(Identified identified) {
		String displayId = extractDisplayId(identified.getIdentity());
		if (displayId!=null) return displayId;
		if (identified.isSetDisplayId()) return identified.getDisplayId();
		return findDisplayId(identified.getIdentity().toString());
	}

	static String findDisplayId(String topLevelIdentity) {
		String displayId = null;
	
		topLevelIdentity = topLevelIdentity.trim();
		while (topLevelIdentity.endsWith("/")||
				topLevelIdentity.endsWith("#")||
				topLevelIdentity.endsWith(":")) {
			topLevelIdentity = topLevelIdentity.replaceAll("/$","");
			topLevelIdentity = topLevelIdentity.replaceAll("#$","");
			topLevelIdentity = topLevelIdentity.replaceAll(":$","");
		}
		int slash = topLevelIdentity.lastIndexOf('/');
		int pound = topLevelIdentity.lastIndexOf('#');
		int colon = topLevelIdentity.lastIndexOf(':');
	
		if (slash!=-1 /*&& slash > pound && slash > colon*/) {
			displayId = topLevelIdentity.substring(slash + 1);
		} else if (pound!=-1 && pound > colon) {
			displayId = topLevelIdentity.substring(pound + 1);
		} else if (colon!=-1) {
			displayId = topLevelIdentity.substring(colon + 1);
		} else {
			displayId = topLevelIdentity.toString();
		}
		displayId = fixDisplayId(displayId);
		return displayId;
	}
}
