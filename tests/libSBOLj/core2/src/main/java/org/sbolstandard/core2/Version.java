package org.sbolstandard.core2;

/**
 * This class provides methods for version comparisons, following the Maven version scheme.
 * @author Zhen Zhang
 * @version 2.1
 */

final class Version {
	
	
	/**
	 * Compare the given two versions based on the Maven version scheme.
	 * @return <code>true</code> if the <code>version1</code> is newer than <code>version2</code>, <code>false</code> otherwise.
	 */
	static final boolean isFirstVersionNewer(String version1, String version2) {
		if (version1==null) return true;
		if (version2==null) return false;
		ComparableVersion v1 = new ComparableVersion(version1);
		ComparableVersion v2 = new ComparableVersion(version2);
		return v1.compareTo(v2) > 0;
	}
}
