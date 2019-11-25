package org.sbolstandard.core2;

import static org.sbolstandard.core2.URIcompliance.createCompliantURI;
import static org.sbolstandard.core.datatree.Datatree.NamespaceBinding;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonReader;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import org.sbolstandard.core.io.turtle.TurtleIo;
import org.sbolstandard.core.datatree.Datatree;
import org.sbolstandard.core.datatree.DocumentRoot;
import org.sbolstandard.core.datatree.IdentifiableDocument;
import org.sbolstandard.core.datatree.Literal;
import org.sbolstandard.core.datatree.NamedProperty;
import org.sbolstandard.core.datatree.NamespaceBinding;
import org.sbolstandard.core.datatree.NestedDocument;
import org.sbolstandard.core.datatree.PropertyValue;
import org.sbolstandard.core.datatree.TopLevelDocument;
import org.sbolstandard.core.io.CoreIoException;
import org.sbolstandard.core.io.IoReader;
import org.sbolstandard.core.io.json.JsonIo;
import org.sbolstandard.core.io.json.StringifyQName;
import org.sbolstandard.core.io.rdf.RdfIo;

/**
 * Provides methods to read input SBOL files. 
 *
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author Zhen Zhang
 * @author Matthew Pocock
 * @author Goksel Misirli
 * @version 2.1
 */

public class SBOLReader
{

	/**
	 * Constant representing SBOL version 1.1
	 */
	public static final String SBOLVERSION1 = "v1";
	
	/**
	 * Constant representing SBOL version 2.0
	 */
	public static final String SBOLVERSION2 = "v2";
	
		
	/**
	 * A {@code true} value of the {@code keepGoing} flag tells the SBOL reader
	 * to continue reading an SBOL input file, after it encounters an SBOL validation exception;
	 * a {@code false} value forces the reader to stop reading after it encounters
	 * an SBOL validation exception.
	 */
	public static boolean keepGoing = false;
	
	private static List<String> errors = new ArrayList<String>();

	/**
	 * Checks if reading should continue after encountering an SBOL validation exception.
	 * 
	 * @return {@code true} if it should continue, {@code false} otherwise
	 */
	public static boolean isKeepGoing() {
		return keepGoing;
	}

	/**
	 * Sets the value for the keepGoing flag to the given boolean value.
	 * <p>
	 * A {@code true} value means that reading will keep going after encountering an SBOL validation exception, 
	 * and a {@code false} value means otherwise.
	 * 
	 * @param keepGoing the boolean value for the keepGoing flag 
	 */
	public static void setKeepGoing(boolean keepGoing) {
		SBOLReader.keepGoing = keepGoing;
	}

	/**
	 * Sets the error list that is used to store SBOL validation exceptions 
	 * during reading to empty. 
	 */
	public static void clearErrors() {
		errors = new ArrayList<String>();
	}

	/**
	 * Returns the error list that is used to store SBOL validation exceptions.
	 * 
	 * @return the error list that is used to store SBOL validation exceptions
	 */
	public static List<String> getErrors() {
		return errors;
	}

	/**
	 * Returns the number of errors in the error list. 
	 * 
	 * @return the number of errors in the error list
	 */
	public static int getNumErrors() {
		return errors.size();
	}

	static class SBOLPair
	{
		private URI left;
		private URI right;

		public SBOLPair(URI left, URI right)
		{
			this.left = left;
			this.right = right;
		}

		public URI getLeft() {
			return left;
		}

		public void setLeft(URI left) {
			this.left = left;
		}

		public URI getRight() {
			return right;
		}

		public void setRight(URI right) {
			this.right = right;
		}
	} //end of SBOLPair class

	private static String URIPrefix	= null;
	private static String defaultDisplayId = "";
	private static String defaultVersion = "";
	private static boolean typesInURI = false;
	private static boolean dropObjectsWithDuplicateURIs = false;
	private static boolean compliant = true;
	private static URI defaultSequenceEncoding = Sequence.IUPAC_DNA;

	/**
	 * Check if document is to be read as being compliant.
	 *
	 * @return {@code true} if document is to be read as being compliant, {@code false} otherwise
	 */
	public static boolean isCompliant() {
		return compliant;
	}

	/**
	 * Sets the compliant flag to the given value.
	 * <p>
	 * A {@code true} value means that the SBOL document is to be read as compliant, 
	 * and a {@code false} value means otherwise.
	 *
	 * @param compliant the boolean value for the compliant flag
	 */
	public static void setCompliant(boolean compliant) {
		SBOLReader.compliant = compliant;
	}
	
	/**
	 * Returns the specified authority prefix.
	 * 
	 * @return the specified authority prefix.
	 */
	public static String getURIPrefix()
	{
		return URIPrefix;
	}

	/**
	 * Sets the specified authority as the prefix.
	 *
	 *  @param URIprefix the given URI prefix
	 */
	public static void setURIPrefix(String URIprefix)
	{
		if (URIprefix!=null && !URIprefix.endsWith("/") && !URIprefix.endsWith(":") && !URIprefix.endsWith("#")) {
			URIprefix += "/";
		}
		SBOLReader.URIPrefix = URIprefix;
	}

	/**
	 * Sets the URI prefix for this reader to {@code null}.
	 */
	public static void unsetURIPrefix()
	{
		SBOLReader.URIPrefix = null;
	}
	
	/**
	 * Get the SBOL default displayId for this reader. 
	 * 
	 * @return the SBOL default displayId for this reader.
	 */
	public static String getDisplayId()
	{
		return defaultDisplayId;
	}

	/**
	 * Sets the SBOL default displayId for this reader. 
	 *
	 * @param displayId the given displayId
	 */
	public static void setDisplayId(String displayId)
	{
		SBOLReader.defaultDisplayId = displayId;
	}
	
	/**
	 * Get the SBOL default version for this reader. 
	 *
	 * @return the SBOL default version for this reader.
	 */
	public static String getVersion()
	{
		return defaultVersion;
	}

	/**
	 * Sets the SBOL default version for this reader. 
	 *
	 * @param version the given version
	 */
	public static void setVersion(String version)
	{
		SBOLReader.defaultVersion = version;
	}

	/**
	 * Sets the value for the typesInURI flag.
	 * <p>
	 * A {@code true} value means that types are to be inserted into each top-level URI when it is created,
	 * and a {@code false} value means otherwise.
	 *
	 * @param typesInURI the boolean value for the typesInURI flag
	 */
	public static void setTypesInURI(boolean typesInURI)
	{
		SBOLReader.typesInURI = typesInURI;
	}

	/**
	 * Check if objects with duplicate URIs should be dropped.
	 *
	 * @return {@code true} if objects with duplicate URIs should be dropped, {@code false} otherwise
	 */
	public static boolean isDropObjectsWithDuplicateURIs() {
		return dropObjectsWithDuplicateURIs;
	}

	/**
	 * Sets the value of the dropObjectsWithDuplicateURIs flag.
	 * <p>
	 * A {@code true} value means that instances with duplicate URIs should be dropped, 
	 * and a {@code false} value means otherwise.
	 *
	 * @param dropObjectsWithDuplicateURIs the boolean value for the dropObjectsWithDuplicateURIs flag
	 */
	public static void setDropObjectsWithDuplicateURIs(boolean dropObjectsWithDuplicateURIs) {
		SBOLReader.dropObjectsWithDuplicateURIs = dropObjectsWithDuplicateURIs;
	}

	/**
	 * Sets the default sequence encoding for FASTA conversion.
	 * 
	 * @return the defaultSequenceEncoding
	 */
	public static URI getDefaultSequenceEncoding() {
		return defaultSequenceEncoding;
	}

	/**
	 * Sets the defaultsequenceEndocding flag to the given value.
	 * 
	 * @param defaultSequenceEncoding the given defaultSequenceEncoding URI
	 */
	public static void setDefaultSequenceEncoding(URI defaultSequenceEncoding) {
		SBOLReader.defaultSequenceEncoding = defaultSequenceEncoding;
	}

	/**
	 * @param document
	 * @return
	 * @throws SBOLValidationException if either of the following SBOL validation rules was violated:
	 * 10101, 10102.
	 */
	private static String getSBOLVersion(DocumentRoot<QName> document) throws SBOLValidationException
	{
		boolean foundRDF = false;
		boolean foundSBOL1 = false;
		boolean foundSBOL2 = false;
		for (NamespaceBinding n : document.getNamespaceBindings())
		{
			if (n.getNamespaceURI().equals(Sbol1Terms.rdf.getNamespaceURI())) foundRDF = true;
			if (n.getNamespaceURI().equals(Sbol1Terms.sbol1.getNamespaceURI()))	foundSBOL1 = true;
			if (n.getNamespaceURI().equals(Sbol2Terms.sbol2.getNamespaceURI()))	foundSBOL2 = true;
		}
		if (foundSBOL2) {
			if (!foundRDF) {
				throw new SBOLValidationException("sbol-10102");
			}
			return SBOLVERSION2;
		} else if (foundSBOL1) {
			if (!foundRDF) {
				throw new SBOLValidationException("sbol-10102");
			}
			return SBOLVERSION1;
		} else {
			throw new SBOLValidationException("sbol-10101");
		}
	}

	/**
	 * Takes in a given RDF file name and returns the SBOL version of the file.
	 *
	 * @param fileName a given RDF file name
	 * @return the SBOL version of the file
	 * @throws FileNotFoundException if file was not found.
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 10101, 10102, 10105, 10201.
	 * @throws SBOLConversionException if file is empty
	 */
	public static String getSBOLVersion(String fileName) throws FileNotFoundException, SBOLValidationException, SBOLConversionException
	{
		return getSBOLVersion(fileName,SBOLDocument.RDF);
	}

	/**
	 * Takes in a given file name and file type, and returns the SBOL version of the file.
	 *
	 * @param fileName the given file name
	 * @return the SBOL version of the file.
	 * @throws FileNotFoundException if file was not found.
	 * @throws SBOLValidationException if if an SBOL validation rule violation occurred in the following method:
	 * {@link #getSBOLVersion(InputStream, String)}.
	 * @throws SBOLConversionException if file is empty
	 */
	private static String getSBOLVersion(String fileName, String fileType) throws FileNotFoundException, SBOLValidationException, SBOLConversionException
	{
		FileInputStream stream     = new FileInputStream(new File(fileName));
		BufferedInputStream buffer = new BufferedInputStream(stream);
		return getSBOLVersion(buffer,fileType);
	}

	/**
	 * Takes in the given RDF filename and converts the file to an SBOLDocument.
	 *
	 * @param fileName the name of the given RDF file
	 * @return the converted SBOLDocument
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 10101, 10102, 10105, 
	 * 10201, 10202, 10203, 10204, 10206, 10208, 10212, 10213, 10220, 
	 * 10303, 10304, 10305, 
	 * 10401, 10402, 10403, 10405, 
	 * 10501, 10502, 10503, 10504, 10507, 10508, 10512, 10513, 10519, 10522, 10526, 
	 * 10602, 10603, 10604, 10605, 10606, 10607, 
	 * 10701, 
	 * 10801, 10802, 10803, 10804, 10805, 10806, 10807, 10808, 10809, 10810, 10811, 
	 * 10901, 10902, 10904, 10905, 
	 * 11002, 11101, 11102, 11103, 11104, 
	 * 11201, 11202, 
	 * 11301, 
	 * 11401, 11402, 11403, 11404, 11405, 11406, 11407, 11412, 
	 * 11501, 11502, 11504, 11508, 
	 * 11601, 11602, 11604, 11605, 11606, 11607, 11608, 11609, 
	 * 11701, 11702, 11703, 11704, 11705, 11706, 
	 * 11801, 11802, 
	 * 11901, 11902, 11906, 
	 * 12001, 12002, 12003, 12004, 
	 * 12101, 12102, 12103, 
	 * 12301, 12302.
	 * @throws SBOLConversionException see {@link SBOLConversionException#SBOLConversionException}
	 * @throws IOException see {@link IOException}
	 */
	public static SBOLDocument read(String fileName) throws SBOLValidationException, IOException, SBOLConversionException
	{
		if (isSnapGeneFile(fileName)) {
			return read(fileName,SBOLDocument.SNAPGENE);
		} else if (fileName.endsWith(".json")) {
			return read(fileName,SBOLDocument.JSON);
		} else if (fileName.endsWith(".ttl")) {
			return read(fileName,SBOLDocument.TURTLE);
		} 
		return read(fileName,SBOLDocument.RDF);
	}

	/**
	 * Takes in the given filename and fileType, and converts the file to an SBOLDocument.
	 *
	 * @param fileName the name of the given file
	 * @param fileType the file type of the given file 
	 * @return the converted SBOLDocument
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #read(File, String)}.
	 * @throws SBOLConversionException if conversion fails
	 * @throws IOException see {@link IOException} 
	 */
	private static SBOLDocument read(String fileName,String fileType) throws SBOLValidationException, IOException, SBOLConversionException
	{
		return read(new File(fileName),fileType);
	}

	/**
	 * Takes in a given RDF file and returns its SBOL version.
	 *
	 * @param file the given RDF file
	 * @return the SBOL version of the file
	 * @throws FileNotFoundException if file was not found.
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 10101, 10102, 10105, 10201.
	 * @throws SBOLConversionException if file is empty
	 */
	public static String getSBOLVersion(File file) throws FileNotFoundException, SBOLValidationException, SBOLConversionException
	{
		return getSBOLVersion(file,SBOLDocument.RDF);
	}

	/**
	 * Parses the given RDF file and stores its contents in an SBOLDocument object.
	 *
	 * @param file the given RDF file
	 * @return an SBOLDocument object that stores the RDF file information
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 10101, 10102, 10105, 
	 * 10201, 10202, 10203, 10204, 10206, 10208, 10212, 10213, 10220, 10221, 10222
	 * 10303, 10304, 10305, 
	 * 10401, 10402, 10403, 10405, 
	 * 10501, 10502, 10503, 10504, 10507, 10508, 10512, 10513, 10519, 10522, 10526, 
	 * 10602, 10603, 10604, 10605, 10606, 10607, 
	 * 10701, 
	 * 10801, 10802, 10803, 10804, 10805, 10806, 10807, 10808, 10809, 10810, 10811, 
	 * 10901, 10902, 10904, 10905, 
	 * 11002, 11101, 11102, 11103, 11104, 
	 * 11201, 11202, 
	 * 11301, 
	 * 11401, 11402, 11403, 11404, 11405, 11406, 11407, 11412, 
	 * 11501, 11502, 11504, 11508, 
	 * 11601, 11602, 11604, 11605, 11606, 11607, 11608, 11609, 
	 * 11701, 11702, 11703, 11704, 11705, 11706, 
	 * 11801, 11802, 
	 * 11901, 11902, 11906, 
	 * 12001, 12002, 12003, 12004, 
	 * 12101, 12102, 12103, 
	 * 12301, 12302,
	 * 12401, 12402, 12403, 12404, 12405, 12406, 12407
	 * 12501, 12502, 12503, 
	 * 12601, 12602, 12603, 12604, 12605, 12606,
	 * 12701,
	 * 12801.
	 * @throws SBOLConversionException see {@link SBOLConversionException}
	 * @throws IOException see {@link IOException}
	 */
	public static SBOLDocument read(File file) throws SBOLValidationException, IOException, SBOLConversionException
	{
		return read(file,SBOLDocument.RDF);
	}

	/**
	 * Takes in the given file and fileType, and convert the file to an SBOLDocument.
	 *
	 * @param file
	 * @param fileType a given file type
	 * @return the converted SBOLDocument instance
	 * @throws FactoryConfigurationError
	 * @throws XMLStreamException
	 * @throws CoreIoException
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #read(InputStream, String)}.
	 * @throws SBOLConversionException if file is empty
	 * @throws IOException see {@link IOException} 
	 */
	private static SBOLDocument read(File file,String fileType) throws SBOLValidationException, IOException, SBOLConversionException
	{
		FileInputStream stream     = new FileInputStream(file);
		BufferedInputStream buffer = new BufferedInputStream(stream);
		return read(buffer,fileType);
	}

	/**
	 * Takes in a given file and file type, and returns the SBOL version of the file.
	 *
	 * @param file the given file
	 * @return the SBOL version of the file
	 * @throws FileNotFoundException if file was not found.
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #getSBOLVersion(InputStream, String)}.
	 * @throws SBOLConversionException 
	 */
	private static String getSBOLVersion(File file,String fileType) throws FileNotFoundException, SBOLValidationException, SBOLConversionException
	{
		FileInputStream stream     = new FileInputStream(file);
		BufferedInputStream buffer = new BufferedInputStream(stream);
		return getSBOLVersion(buffer,fileType);
	}

	/**
	 * Takes in a given input stream and fie type, and returns the SBOL version of the file.
	 *
	 * @param in the given input stream
	 * @param fileType the given file type
	 * @return the SBOL version of the given file.
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in any
	 * of the following methods:
	 * <ul>
	 * <li>{@link #readJSON(Reader)},</li>
	 * <li>{@link #readTurtle(Reader)},</li>
	 * <li>{@link #readRDF(Reader)}, or</li>
	 * <li>{@link #getSBOLVersion(DocumentRoot)}.</li>
	 * </ul>
	 * @throws SBOLConversionException if file is empty
	 */
	private static String getSBOLVersion(InputStream in,String fileType) throws SBOLValidationException, SBOLConversionException
	{
		Scanner scanner = new Scanner(in, "UTF-8");
		String inputStreamString;
		try {
			inputStreamString = scanner.useDelimiter("\\A").next();
		} catch (NoSuchElementException e) {
			scanner.close();
			throw new SBOLConversionException("File is empty.");
		}
		DocumentRoot<QName> document = null;
		if (fileType.equals(SBOLDocument.JSON)) {
			document = readJSON(new StringReader(inputStreamString));
		} else if (fileType.equals(SBOLDocument.TURTLE)) {
			document = readTurtle(new StringReader(inputStreamString));
		} else {
			document = readRDF(new StringReader(inputStreamString));
		}
		scanner.close();
		return getSBOLVersion(document);
	}

	/**
	 * Takes in a given RDF InputStream and converts the file to an SBOLDocument.
	 *
	 * @param in a given RDF InputStream
	 * @return the converted SBOLDocument instance
	 * @throws SBOLConversionException see {@link SBOLConversionException}
	 * @throws IOException see {@link IOException}
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 10101, 10102, 10105, 
	 * 10201, 10202, 10203, 10204, 10206, 10208, 10212, 10213, 10220, 
	 * 10303, 10304, 10305, 
	 * 10401, 10402, 10403, 10405, 
	 * 10501, 10502, 10503, 10504, 10507, 10508, 10512, 10513, 10519, 10522, 10526, 
	 * 10602, 10603, 10604, 10605, 10606, 10607, 
	 * 10701, 
	 * 10801, 10802, 10803, 10804, 10805, 10806, 10807, 10808, 10809, 10810, 10811, 
	 * 10901, 10902, 10904, 10905, 
	 * 11002, 
	 * 11101, 11102, 11103, 11104, 
	 * 11201, 11202, 
	 * 11301, 
	 * 11401, 11402, 11403, 11404, 11405, 11406, 11407, 11412, 
	 * 11501, 11502, 11504, 11508, 
	 * 11601, 11602, 11604, 11605, 11606, 11607, 11608, 11609, 
	 * 11701, 11702, 11703, 11704, 11705, 11706, 
	 * 11801, 11802, 
	 * 11901, 11902, 11906, 
	 * 12001, 12002, 12003, 12004, 
	 * 12101, 12102, 12103, 
	 * 12301, 12302.
	 */
	public static SBOLDocument read(InputStream in) throws SBOLValidationException, IOException, SBOLConversionException
	{
		SBOLDocument SBOLDoc     = new SBOLDocument();
		SBOLDoc.setCompliant(compliant);
		read(SBOLDoc,in,SBOLDocument.RDF);
		return SBOLDoc;
	}

	/**
	 * Takes in a given InputStream and fileType, and convert the file to an SBOLDocument.
	 *
	 * @param in a given InputStream
	 * @param fileType a given file type
	 * @return the converted SBOLDocument instance
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #read(SBOLDocument, InputStream, String)}.
	 * @throws SBOLConversionException if file is empty
	 * @throws IOException see {@link IOException} 
	 */
	private static SBOLDocument read(InputStream in,String fileType) throws SBOLValidationException, IOException, SBOLConversionException
	{
		SBOLDocument SBOLDoc     = new SBOLDocument();
		SBOLDoc.setCompliant(compliant);
		if (URIPrefix!=null) {
			SBOLDoc.setDefaultURIprefix(URIPrefix);
		}
		read(SBOLDoc,in,fileType);
		return SBOLDoc;
	}

	/**
	 * @param SBOLDoc
	 * @param in
	 * @param fileType
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>If {@link #keepGoing} was set to {@code false}, and an SBOL validation rule violation occurred in
	 * any of the following methods:
	 * 	<ul>
	 * 		<li>{@link FASTA#read(SBOLDocument, String, String, String, URI)},</li>
	 * 		<li>{@link GenBank#read(SBOLDocument, String, String)},</li>
	 * 		<li>{@link #readJSON(Reader)}, </li>
	 * 		<li>{@link #readRDF(Reader)}, </li>
	 * 		<li>{@link #readTurtle(Reader)}, </li>
	 * 		<li>{@link #getSBOLVersion(DocumentRoot)}, or</li>
	 * 		<li>{@link #readV1(SBOLDocument, DocumentRoot)}; or</li>
	 * 	</ul></li>
	 * <li>an SBOL validation rule violation occurred in {@link #readTopLevelDocs(SBOLDocument, DocumentRoot)}.</li>
	 * </ul>
	 * @throws IOException see {@link IOException}
	 * @throws SBOLConversionException
	 */
	static void read(SBOLDocument SBOLDoc,InputStream in,String fileType) throws SBOLValidationException, IOException, SBOLConversionException
	{
		if (fileType.equals(SBOLDocument.SNAPGENE)) {
			SnapGene.read(SBOLDoc, in, URIPrefix, defaultDisplayId, defaultVersion);
			return;
		} 
		compliant = SBOLDoc.isCompliant();
		Scanner scanner = new Scanner(in, "UTF-8");
		String inputStreamString;
		try {
			inputStreamString = scanner.useDelimiter("\\A").next();
		} catch (NoSuchElementException e) {
			scanner.close();
			throw new SBOLConversionException("File is empty.");
		}
		clearErrors();

		DocumentRoot<QName> document = null;
		try {
			if (SBOLReader.isFastaString(inputStreamString)) {
				SBOLDoc.setCreateDefaults(true);
				SBOLDoc.setCompliant(true);
				if (URIPrefix==null) {
					scanner.close();
					throw new SBOLConversionException("No URI prefix has been provided.");
				}
				SBOLDoc.setDefaultURIprefix(URIPrefix);
				FASTA.read(SBOLDoc, inputStreamString, URIPrefix, defaultDisplayId, defaultVersion, defaultSequenceEncoding);
				scanner.close();
				return;
			} else if (SBOLReader.isGFF3String(inputStreamString)) {
				SBOLDoc.setCreateDefaults(true);
				SBOLDoc.setCompliant(true);
				if (URIPrefix==null) {
					scanner.close();
					throw new SBOLConversionException("No URI prefix has been provided.");
				}
				SBOLDoc.setDefaultURIprefix(URIPrefix);
				GFF3.read(SBOLDoc, inputStreamString, URIPrefix, defaultVersion, defaultSequenceEncoding);
				scanner.close();
				return;
			} else if (SBOLReader.isGenBankString(inputStreamString)) {
				SBOLDoc.setCreateDefaults(true);
				SBOLDoc.setCompliant(true);
				if (URIPrefix==null) {
					scanner.close();
					throw new SBOLConversionException("No URI prefix has been provided.");
				}
				SBOLDoc.setDefaultURIprefix(URIPrefix);
				GenBank.read(SBOLDoc, inputStreamString, URIPrefix, defaultDisplayId, defaultVersion);
				scanner.close();
				return;
			} else if (fileType.equals(SBOLDocument.JSON)) {
				document = readJSON(new StringReader(inputStreamString));
			} else if (fileType.equals(SBOLDocument.TURTLE)){
				document = readTurtle(new StringReader(inputStreamString));
			} else {
				document = readRDF(new StringReader(inputStreamString));
			}
			if (getSBOLVersion(document).equals(SBOLVERSION1))
			{
				scanner.close();
				readV1(SBOLDoc,document);
				return;
			}

			for (NamespaceBinding n : document.getNamespaceBindings())

			{
				if (SBOLDoc.getNamespace(URI.create(n.getNamespaceURI()))==null) {
					if (n.getPrefix()==null) {
						SBOLDoc.addNamespaceBinding(NamespaceBinding(n.getNamespaceURI(), ""));
					} else {
						SBOLDoc.addNamespaceBinding(NamespaceBinding(n.getNamespaceURI(), n.getPrefix()));
					}
				}

			}
		} catch (SBOLValidationException e) {
			if (keepGoing) {
				errors.add(e.getMessage());
				return;
			} else {
				throw new SBOLValidationException(e);
			}
		}

		readTopLevelDocs(SBOLDoc, document);
		scanner.close();
		SBOLValidate.clearErrors();
		SBOLValidate.validateCompliance(SBOLDoc);
		if (SBOLValidate.getNumErrors()>0) {
			SBOLDoc.setCompliant(false);
		}
	}

	/**
	 * Takes in a given RDF input stream and returns the SBOL version of the file.
	 *
	 * @param in a given RDF input stream
	 * @return the SBOL version of the file.
	 * @throws SBOLValidationException if any of the following SBOL validation rules was violated:
	 * 10101, 10102, 10105, 10201.
	 * @throws SBOLConversionException if file is empty
	 */
	public static String getSBOLVersion(InputStream in) throws SBOLValidationException, SBOLConversionException
	{
		return getSBOLVersion(in,SBOLDocument.RDF);
	}

	/**
	 * @param SBOLDoc
	 * @param document
	 * @return
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in {@link #readTopLevelDocsV1(SBOLDocument, DocumentRoot)}.
	 * @throws SBOLConversionException
	 */
	private static SBOLDocument readV1(SBOLDocument SBOLDoc, DocumentRoot<QName> document) throws SBOLValidationException, SBOLConversionException
	{
		for (NamespaceBinding n : document.getNamespaceBindings())
		{
			if (n.getNamespaceURI().equals(Sbol1Terms.sbol1.getNamespaceURI()))
			{
				SBOLDoc.addNamespaceBinding(NamespaceBinding(Sbol2Terms.sbol2.getNamespaceURI(),
						Sbol2Terms.sbol2.getPrefix()));
			}
			else
			{
				if (SBOLDoc.getNamespace(URI.create(n.getNamespaceURI()))==null) {
					SBOLDoc.addNamespaceBinding(
							NamespaceBinding(n.getNamespaceURI(), n.getPrefix()));
				}
			}
		}
		SBOLDoc.addNamespaceBinding(NamespaceBinding(Sbol2Terms.prov.getNamespaceURI(),
				Sbol2Terms.prov.getPrefix()));
		readTopLevelDocsV1(SBOLDoc, document);
		SBOLValidate.clearErrors();
		SBOLValidate.validateCompliance(SBOLDoc);
		if (SBOLValidate.getNumErrors()>0) {
			SBOLDoc.setCompliant(false);
		}
		return SBOLDoc;
	}

	/**
	 * @param stream
	 * @return
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 10105.
	 */
	private static DocumentRoot<QName> readJSON(Reader stream) throws SBOLValidationException
	{
		JsonReader reader 		  = Json.createReaderFactory(Collections.<String, Object> emptyMap()).createReader(stream);
		JsonIo jsonIo 	  		  = new JsonIo();
		IoReader<String> ioReader = jsonIo.createIoReader(reader.read());
		DocumentRoot<String> root;
		try {
			root = ioReader.read();
		}
		catch (CoreIoException e) {
			throw new SBOLValidationException("sbol-10105",e);
		}
		return StringifyQName.string2qname.mapDR(root);
	}

	/**
	 * @param reader
	 * @return
	 * @throws SBOLValidationException if either of the following SBOL validation rules was violated: 10105, 10201.
	 */
	private static DocumentRoot<QName> readRDF(Reader reader) throws SBOLValidationException
	{
		try {
			XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(reader);
			RdfIo rdfIo 			  = new RdfIo();
			return rdfIo.createIoReader(xmlReader).read();
		}
		catch (FactoryConfigurationError e) {
			throw new SBOLValidationException("sbol-10105",e);
		}
		catch (XMLStreamException e) {
			throw new SBOLValidationException("sbol-10105",e);
		}
		catch (CoreIoException e) {
			throw new SBOLValidationException("sbol-10105",e);
		}
		catch (ClassCastException e) {
			if (e.getMessage().contains("IdentifiableDocument")) {
				throw new SBOLValidationException("sbol-10201",e);
			}
			throw new SBOLValidationException("sbol-10105",e);
		}
		catch (IllegalArgumentException e) {
			if (e.getCause() instanceof URISyntaxException) {
				throw new SBOLValidationException("sbol-10201",e);
			}
			throw new SBOLValidationException("sbol-10105",e);
		}
	}

	/**
	 * @param reader
	 * @return
	 * @throws SBOLValidationException if the following SBOL validation rule was violated: 10105.
	 */
	private static DocumentRoot<QName> readTurtle(Reader reader) throws SBOLValidationException
	{
		TurtleIo turtleIo = new TurtleIo();
		try {
			return turtleIo.createIoReader(reader).read();
		}
		catch (CoreIoException e) {
			throw new SBOLValidationException("sbol-10105",e);
		}
	}

	/**
	 * @param SBOLDoc
	 * @param document
	 * @throws SBOLValidationException If {@link #keepGoing} was set to {@code false}, and an SBOL validation rule violation occurred in
	 * any of the following methods:
	 * <ul>
	 * <li>{@link #parseDnaComponentV1(SBOLDocument, IdentifiableDocument)},</li>
	 * <li>{@link #parseDnaSequenceV1(SBOLDocument, IdentifiableDocument)},</li>
	 * <li>{@link #parseCollectionV1(SBOLDocument, IdentifiableDocument)}, or</li>
	 * <li>{@link #parseGenericTopLevel(SBOLDocument, TopLevelDocument)}.</li>
	 * </ul>
	 * @throws SBOLConversionException
	 */
	private static void readTopLevelDocsV1(SBOLDocument SBOLDoc, DocumentRoot<QName> document) throws SBOLValidationException, SBOLConversionException
	{
		clearErrors();
		for (TopLevelDocument<QName> topLevel : document.getTopLevelDocuments())
		{
			try {
				if (topLevel.getType().equals(Sbol1Terms.DNAComponent.DNAComponent))
					parseDnaComponentV1(SBOLDoc, topLevel);
				else if (topLevel.getType().equals(Sbol1Terms.DNASequence.DNASequence))
					parseDnaSequenceV1(SBOLDoc, topLevel);
				else if (topLevel.getType().equals(Sbol1Terms.Collection.Collection))
					parseCollectionV1(SBOLDoc, topLevel);
				else
				{
					parseGenericTopLevel(SBOLDoc, topLevel);
				}
			} catch (SBOLValidationException e) {
				if (keepGoing) {
					errors.add(e.getMessage());
				} else {
					throw new SBOLValidationException(e);
				}
			}
		}
	}

	/**
	 *
	 * @param SBOLDoc
	 * @param document
	 * @throws SBOLValidationException if {@link #keepGoing} was set to {@code false}, and either of the following conditions is satisfied:
	 * <ul> 
	 * 	<li>the following SBOL validation rule was violated: 12302; or</li>
	 * 	<li>an SBOL validation rule violation occurred in any of the following methods:
	 * 		<ul>
	 * 			<li>{@link #parseCollection(SBOLDocument, TopLevelDocument)},</li>
	 * 			<li>{@link #parseModuleDefinition(SBOLDocument, TopLevelDocument, Map)},</li>
	 * 			<li>{@link #parseModel(SBOLDocument, TopLevelDocument)},</li>
	 * 			<li>{@link #parseSequence(SBOLDocument, TopLevelDocument)}, </li>
	 * 			<li>{@link #parseComponentDefinition(SBOLDocument, TopLevelDocument, Map)}, or</li>
	 * 			<li>{@link #parseGenericTopLevel(SBOLDocument, TopLevelDocument)}.</li>
	 * 		</ul>
	 * 	</li>
	 * </ul>
	 */
	private static void readTopLevelDocs(SBOLDocument SBOLDoc, DocumentRoot<QName> document) throws SBOLValidationException
	{
		Map<URI, NestedDocument<QName>> nested = new HashMap<URI, NestedDocument<QName>>();
		List<TopLevelDocument<QName>> topLevels = new ArrayList<TopLevelDocument<QName>>();
		clearErrors();

		for (TopLevelDocument<QName> topLevel : document.getTopLevelDocuments()) {

			if (topLevel.getType().equals(Sbol2Terms.Description.Description)) {
				if (topLevel.getPropertyValues(Sbol2Terms.Description.type).isEmpty()) {
					if (keepGoing) {
						errors.add(new SBOLValidationException("sbol-12302",topLevel.getIdentity()).getMessage());
					} else {
						throw new SBOLValidationException("sbol-12302",topLevel.getIdentity());
					}
				}
				int sbolCount = 0;
				int provCount = 0;
				int sbol1Count = 0;
				int omCount = 0;
				for (PropertyValue<QName> value : topLevel.getPropertyValues(Sbol2Terms.Description.type)) {
					String type = ((Literal<QName>) value).getValue().toString();
					if (type.startsWith(Sbol2Terms.prov.getNamespaceURI())) provCount++;
					else if (type.startsWith(Sbol2Terms.sbol2.getNamespaceURI())) sbolCount++;
					else if (type.startsWith(Sbol1Terms.sbol1.getNamespaceURI())) sbol1Count++;
					else if (type.startsWith(Sbol2Terms.om.getNamespaceURI())) omCount++;
				}
				if (sbolCount > 1 || provCount > 1 || sbol1Count > 1 || omCount > 1) {
					if (keepGoing) {
						errors.add(new SBOLValidationException("sbol-10228",topLevel.getIdentity()).getMessage());
					} else {
						throw new SBOLValidationException("sbol-10228",topLevel.getIdentity());
					}
				} else if (sbolCount==0 && provCount==0 && sbol1Count==0 && omCount == 0 &&
						topLevel.getPropertyValues(Sbol2Terms.Description.type).size() > 1) {
					if (keepGoing) {
						errors.add(new SBOLValidationException("sbol-12302",topLevel.getIdentity()).getMessage());
					} else {
						throw new SBOLValidationException("sbol-12302",topLevel.getIdentity());
					}
				} else if (sbolCount==0 && provCount==0 && omCount==0 && topLevel.getPropertyValues(Sbol2Terms.Description.type).size()==1) {
					topLevels.add(topLevel);
				} else if (sbolCount==1 || provCount==1 || omCount==1) {
					for (PropertyValue<QName> value : topLevel.getPropertyValues(Sbol2Terms.Description.type)) {
						String typeStr = ((Literal<QName>) value).getValue().toString();
						QName type = null;
						if (typeStr.startsWith(Sbol2Terms.prov.getNamespaceURI())) {
							String localPart = typeStr.replace(Sbol2Terms.prov.getNamespaceURI(), "");
							type = Sbol2Terms.prov.withLocalPart(localPart);
						} else if (provCount==0 && typeStr.startsWith(Sbol2Terms.sbol2.getNamespaceURI())) {
							String localPart = typeStr.replace(Sbol2Terms.sbol2.getNamespaceURI(), "");
							type = Sbol2Terms.sbol2.withLocalPart(localPart);
						} else if (provCount==0 && typeStr.startsWith(Sbol2Terms.om.getNamespaceURI())) {
							String localPart = typeStr.replace(Sbol2Terms.om.getNamespaceURI(), "");
							type = Sbol2Terms.om.withLocalPart(localPart);
						} else {
							continue;
						}
						if (type.equals(Sbol2Terms.Component.Component)
								|| type.equals(Sbol2Terms.Cut.Cut)
								|| type.equals(Sbol2Terms.FunctionalComponent.FunctionalComponent)
								|| type.equals(Sbol2Terms.GenericLocation.GenericLocation)
								|| type.equals(Sbol2Terms.Interaction.Interaction)
								|| type.equals(Sbol2Terms.Location.Location)
								|| type.equals(Sbol2Terms.MapsTo.MapsTo)
								|| type.equals(Sbol2Terms.Measure.Measure)
								|| type.equals(Sbol2Terms.Module.Module)
								|| type.equals(Sbol2Terms.Participation.Participation)
								|| type.equals(Sbol2Terms.Range.Range)
								|| type.equals(Sbol2Terms.SequenceAnnotation.SequenceAnnotation)
								|| type.equals(Sbol2Terms.SequenceConstraint.SequenceConstraint)
								|| type.equals(Sbol2Terms.VariableComponent.VariableComponent)
								|| type.equals(Sbol2Terms.Association.Association)
								|| type.equals(Sbol2Terms.Usage.Usage)) {
							nested.put(topLevel.getIdentity(),
									Datatree.NestedDocument(Datatree.NamespaceBindings(topLevel.getNamespaceBindings()),
											type, topLevel.getIdentity(),
											Datatree.NamedProperties(topLevel.getProperties())));
						} else {
							topLevels.add(Datatree.TopLevelDocument(Datatree.NamespaceBindings(topLevel.getNamespaceBindings()),
									type, topLevel.getIdentity(),
									Datatree.NamedProperties(topLevel.getProperties())));
						}
					}
				} else if (sbol1Count == 1) {
					for (PropertyValue<QName> value : topLevel.getPropertyValues(Sbol2Terms.Description.type)) {
						String typeStr = ((Literal<QName>) value).getValue().toString();
						QName type = null;
						if (typeStr.startsWith(Sbol1Terms.sbol1.getNamespaceURI())) {
							String localPart = typeStr.replace(Sbol1Terms.sbol1.getNamespaceURI(), "");
							type = Sbol1Terms.sbol1.withLocalPart(localPart);
						} else {
							continue;
						}
						if (type.equals(Sbol1Terms.SequenceAnnotations.SequenceAnnotation)) {
							nested.put(topLevel.getIdentity(),
									Datatree.NestedDocument(Datatree.NamespaceBindings(topLevel.getNamespaceBindings()),
											type, topLevel.getIdentity(),
											Datatree.NamedProperties(topLevel.getProperties())));
						} else {
							topLevels.add(Datatree.TopLevelDocument(Datatree.NamespaceBindings(topLevel.getNamespaceBindings()),
									type, topLevel.getIdentity(),
									Datatree.NamedProperties(topLevel.getProperties())));
						}
					}					
				}
			} else if (topLevel.getType().equals(Sbol2Terms.Component.Component)
					|| topLevel.getType().equals(Sbol2Terms.Cut.Cut)
					|| topLevel.getType().equals(Sbol2Terms.FunctionalComponent.FunctionalComponent)
					|| topLevel.getType().equals(Sbol2Terms.GenericLocation.GenericLocation)
					|| topLevel.getType().equals(Sbol2Terms.Interaction.Interaction)
					|| topLevel.getType().equals(Sbol2Terms.Location.Location)
					|| topLevel.getType().equals(Sbol2Terms.MapsTo.MapsTo)
					|| topLevel.getType().equals(Sbol2Terms.Module.Module)
					|| topLevel.getType().equals(Sbol2Terms.Measure.Measure)
					|| topLevel.getType().equals(Sbol2Terms.Participation.Participation)
					|| topLevel.getType().equals(Sbol2Terms.Range.Range)
					|| topLevel.getType().equals(Sbol2Terms.SequenceAnnotation.SequenceAnnotation)
					|| topLevel.getType().equals(Sbol2Terms.SequenceConstraint.SequenceConstraint)
					|| topLevel.getType().equals(Sbol2Terms.VariableComponent.VariableComponent)
					|| topLevel.getType().equals(Sbol2Terms.Association.Association)
					|| topLevel.getType().equals(Sbol2Terms.Usage.Usage)) {
				nested.put(topLevel.getIdentity(),
						Datatree.NestedDocument(Datatree.NamespaceBindings(topLevel.getNamespaceBindings()),
								topLevel.getType(), topLevel.getIdentity(),
								Datatree.NamedProperties(topLevel.getProperties())));
			} else {
				topLevels.add(topLevel);
			}
		}

		for (TopLevelDocument<QName> topLevel : topLevels) {
			try {
				if (topLevel.getType().equals(Sbol2Terms.Collection.Collection))
					parseCollection(SBOLDoc, topLevel, nested);
				else if (topLevel.getType().equals(Sbol2Terms.Experiment.Experiment))
					parseExperiment(SBOLDoc, topLevel, nested);
				else if (topLevel.getType().equals(Sbol2Terms.ExperimentalData.ExperimentalData))
					parseExperimentalData(SBOLDoc, topLevel, nested);
				else if (topLevel.getType().equals(Sbol2Terms.ModuleDefinition.ModuleDefinition))
					parseModuleDefinition(SBOLDoc, topLevel, nested);
				else if (topLevel.getType().equals(Sbol2Terms.Model.Model))
					parseModel(SBOLDoc, topLevel);
				else if (topLevel.getType().equals(Sbol2Terms.Sequence.Sequence))
					parseSequence(SBOLDoc, topLevel);
				else if (topLevel.getType().equals(Sbol2Terms.ComponentDefinition.ComponentDefinition))
					parseComponentDefinition(SBOLDoc, topLevel, nested);
				else if (topLevel.getType().equals(Sbol2Terms.CombinatorialDerivation.CombinatorialDerivation))
					parseCombinatorialDerivation(SBOLDoc, topLevel, nested);
				else if (topLevel.getType().equals(Sbol2Terms.Implementation.Implementation))
					parseImplementation(SBOLDoc, topLevel, nested);
				else if (topLevel.getType().equals(Sbol2Terms.Attachment.Attachment))
					parseAttachment(SBOLDoc, topLevel);
				else if (topLevel.getType().equals(Sbol2Terms.Activity.Activity))
					parseActivity(SBOLDoc, topLevel, nested);
				else if (topLevel.getType().equals(Sbol2Terms.Agent.Agent))
					parseAgent(SBOLDoc, topLevel);
				else if (topLevel.getType().equals(Sbol2Terms.Plan.Plan))
					parsePlan(SBOLDoc, topLevel);
				else
					parseGenericTopLevel(SBOLDoc, topLevel);
			} catch (SBOLValidationException e) {
				if (keepGoing) {
					errors.add(e.getMessage());
				} else {
					throw new SBOLValidationException(e);
				}
			}
		}
	}

	/**
	 * @param SBOLDoc
	 * @param componentDef
	 * @return
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>if an SBOL validation rule violation occurred in any of the following constructors or methods:
	 * 	<ul>
	 * 		<li>{@link URIcompliance#createCompliantURI(String, String, String, String, boolean)},</li>
	 * 		<li>{@link #parseSequenceAnnotationV1(SBOLDocument, NestedDocument, List, String, int, Set)},</li>
	 * 		<li>{@link URIcompliance#createCompliantURI(String, String, String)},</li>
	 * 		<li>{@link Component#Component(URI, AccessType, URI)},</li>
	 * 		<li>{@link Component#setDisplayId(String)}, </li>
	 * 		<li>{@link Component#setVersion(String)}</li>
	 * 		<li>{@link SequenceAnnotation#setComponent(URI)}, </li>
	 * 		<li>{@link #parseDnaSequenceV1(SBOLDocument, IdentifiableDocument)}</li>
	 * 		<li>{@link RestrictionType#convertToURI(RestrictionType)},</li>
	 * 		<li>{@link SequenceConstraint#SequenceConstraint(URI, URI, URI, URI)},</li>
	 * 		<li>{@link SequenceConstraint#setDisplayId(String)},</li>
	 * 		<li>{@link SequenceConstraint#setVersion(String)},</li>
	 * 		<li>{@link ComponentDefinition#ComponentDefinition(URI, Set)},</li>
	 * 		<li>{@link ComponentDefinition#setVersion(String)},</li>
	 * 		<li>{@link ComponentDefinition#setWasDerivedFrom(URI)}, </li>
	 * 		<li>{@link Identified#setAnnotations(List)},</li>
	 * 		<li>{@link ComponentDefinition#setComponents(Set)}</li>
	 * 		<li>{@link ComponentDefinition#setSequenceConstraints(Set)}</li>
	 * 		<li>{@link ComponentDefinition#addSequence(URI)}</li>
	 * 		<li>{@link ComponentDefinition#addSequenceAnnotation(SequenceAnnotation)},</li>
	 * 		<li>{@link SBOLDocument#addComponentDefinition(ComponentDefinition)}, or</li>
	 * 		<li>{@link ComponentDefinition#copy(String, String, String)}; or</li>
	 * 	</ul> 
	 * </li>
	 * <li>the following SBOL validation rule was violated: 10202.</li>
	 * </ul>
	 * @throws SBOLConversionException
	 */
	private static ComponentDefinition parseDnaComponentV1(
			SBOLDocument SBOLDoc, IdentifiableDocument<QName> componentDef) throws SBOLValidationException, SBOLConversionException
	{
		String displayId   = null;
		String name 	   = null;
		String description = null;
		URI seq_identity   = null;
		Set<URI> roles 	   = new HashSet<>();
		URI identity 	   = componentDef.getIdentity();
		String persIdentity = componentDef.getIdentity().toString();

		List<Annotation> annotations 				 = new ArrayList<>();
		List<SequenceAnnotation> sequenceAnnotations = new ArrayList<>();
		Set<String> instantiatedComponents	         = new HashSet<>();
		Set<Component> components 					 = new HashSet<>();
		Set<SequenceConstraint> sequenceConstraints = new HashSet<>();
		List<SBOLPair> precedePairs 				 = new ArrayList<>();
		Map<URI, URI> componentDefMap 				 = new HashMap<>();

		Set<URI> type = new HashSet<>();
		type.add(ComponentDefinition.DNA_REGION);
		type.add(SequenceOntology.LINEAR);

		int component_num = 0;
		int sa_num 		  = 0;

		if (URIPrefix != null)
		{
			displayId = URIcompliance.findDisplayId(componentDef.getIdentity().toString());
			identity = createCompliantURI(URIPrefix,TopLevel.COMPONENT_DEFINITION,displayId,defaultVersion,typesInURI);
			persIdentity = createCompliantURI(URIPrefix,TopLevel.COMPONENT_DEFINITION,displayId,"",typesInURI).toString();
		}

		for (NamedProperty<QName> namedProperty : componentDef.getProperties())
		{
			if (namedProperty.getName().equals(Sbol1Terms.DNAComponent.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", componentDef.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
				displayId = URIcompliance.fixDisplayId(displayId);
				if (URIPrefix != null )
				{
					persIdentity = createCompliantURI(URIPrefix,TopLevel.COMPONENT_DEFINITION,displayId,"",typesInURI).toString();
					identity = createCompliantURI(URIPrefix,TopLevel.COMPONENT_DEFINITION,displayId,defaultVersion,typesInURI);
				}
			}
			else if (namedProperty.getName().equals(Sbol1Terms.DNAComponent.name))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", componentDef.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol1Terms.DNAComponent.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213", componentDef.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol1Terms.DNAComponent.type))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10507", componentDef.getIdentity());
				}
				URI convertedSO = SequenceOntology.convertSeqOntologyV1(((Literal<QName>) namedProperty.getValue()).getValue().toString());
				roles.add(convertedSO);
			}
			else if (namedProperty.getName().equals(Sbol1Terms.DNAComponent.annotations))
			{
				if (namedProperty.getValue() instanceof IdentifiableDocument) {
					SequenceAnnotation sa = parseSequenceAnnotationV1(SBOLDoc,
							((NestedDocument<QName>) namedProperty.getValue()),
							precedePairs, persIdentity, ++sa_num, instantiatedComponents);

					sequenceAnnotations.add(sa);

					URI component_identity    = createCompliantURI(persIdentity,"component" + component_num,defaultVersion);
					URI component_persIdentity = createCompliantURI(persIdentity,"component" + component_num,"");
					String component_displayId = "component"+component_num;
					AccessType access 		  = AccessType.PUBLIC;
					URI instantiatedComponent = sa.getComponentURI();
					ComponentDefinition instantiatedDef = SBOLDoc.getComponentDefinition(instantiatedComponent);
					if (compliant && instantiatedDef != null && instantiatedDef.isSetDisplayId() &&
							!instantiatedComponents.contains(instantiatedDef.getDisplayId())) {
						component_identity = createCompliantURI(persIdentity, instantiatedDef.getDisplayId(),defaultVersion);
						component_persIdentity = createCompliantURI(persIdentity, instantiatedDef.getDisplayId(),"");
						component_displayId = instantiatedDef.getDisplayId();
						instantiatedComponents.add(instantiatedDef.getDisplayId());
					} else {
						component_num++;
					}

					Component component = new Component(component_identity, access, instantiatedComponent);
					if (!persIdentity.equals("")) {
						component.setPersistentIdentity(component_persIdentity);
						component.setDisplayId(component_displayId);
						component.setVersion(defaultVersion);
					}
					components.add(component);

					URI originalURI 		  = ((NestedDocument<QName>) namedProperty.getValue()).getIdentity();
					componentDefMap.put(originalURI, component_identity);
					sa.setComponent(component_identity);
				} else {
					throw new SBOLConversionException("SequenceAnnotation must be nested in SBOL1.");
				}
			}
			else if (namedProperty.getName().equals(Sbol1Terms.DNAComponent.dnaSequence))
			{
				if (seq_identity != null) {
					throw new SBOLValidationException("sbol-10512", componentDef.getIdentity());
				}
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-10512", componentDef.getIdentity());
					}
					seq_identity =  URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
				} else {
					seq_identity = parseDnaSequenceV1(SBOLDoc,
							(NestedDocument<QName>) namedProperty.getValue()).getIdentity();
				}
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		if (roles.isEmpty())
			roles.add(SequenceOntology.ENGINEERED_REGION);

		int sc_number = 0;

		for (SBOLPair pair : precedePairs)
		{
			URI sc_identity    	= createCompliantURI(persIdentity,"sequenceConstraint" + ++sc_number,defaultVersion);
			URI restrictionURI 	= RestrictionType.convertToURI(RestrictionType.PRECEDES);
			//RestrictionType restriction = RestrictionType.convertToRestrictionType(restrictionURI);

			URI subject = null;
			URI object  = null;

			for (URI key : componentDefMap.keySet())
			{
				if (pair.getLeft().equals(key))
				{
					subject = componentDefMap.get(key);
				}
				else if (pair.getRight().equals(key))
				{
					object = componentDefMap.get(key);
				}
			}

			SequenceConstraint sc = null;
			if (compliant && !persIdentity.equals("")) {
				String subjectId = URIcompliance.extractDisplayId(subject);
				String objectId = URIcompliance.extractDisplayId(object);
				sc_identity = createCompliantURI(persIdentity,subjectId+"_cons_"+objectId,defaultVersion);
				sc = new SequenceConstraint(sc_identity, restrictionURI, subject, object);
				sc.setPersistentIdentity(createCompliantURI(persIdentity,subjectId+"_cons_"+objectId,""));
				sc.setDisplayId(subjectId+"_cons_"+objectId);
				sc.setVersion(defaultVersion);
			} else {
				sc = new SequenceConstraint(sc_identity, restrictionURI, subject, object);
			}
			sequenceConstraints.add(sc);
		}

		ComponentDefinition c = new ComponentDefinition(identity, type);
		if (!persIdentity.equals("")) {
			c.setPersistentIdentity(URI.create(persIdentity));
			c.setVersion(defaultVersion);
		}
		if(roles != null)
			c.setRoles(roles);
		if(identity != componentDef.getIdentity())
			c.addWasDerivedFrom(componentDef.getIdentity());
		if (displayId != null)
			c.setDisplayId(displayId);
		if (name != null && !name.isEmpty())
			c.setName(name);
		if (description != null && !description.isEmpty())
			c.setDescription(description);
		if (seq_identity != null)
			c.addSequence(seq_identity);
		if (!annotations.isEmpty())
			c.setAnnotations(annotations);
		if (!components.isEmpty())
			c.setComponents(components);
		if (!sequenceAnnotations.isEmpty()) {
			for (SequenceAnnotation sa : sequenceAnnotations) {
				if (!dropObjectsWithDuplicateURIs || c.getSequenceAnnotation(sa.getIdentity())==null) {
					c.addSequenceAnnotation(sa);
				}
			}
		}
		if (!sequenceConstraints.isEmpty())
			c.setSequenceConstraints(sequenceConstraints);

		ComponentDefinition oldC = SBOLDoc.getComponentDefinition(identity);
		if (oldC == null) {
			SBOLDoc.addComponentDefinition(c);
		} else if (c.getWasDerivedFroms().size()>0 && oldC.getWasDerivedFroms().size()>0 &&
				!c.getWasDerivedFroms().equals(oldC.getWasDerivedFroms())) {
			URI wasDerivedFrom = (URI)c.getWasDerivedFroms().toArray()[0];
			Set<TopLevel> topLevels = SBOLDoc.getByWasDerivedFrom(wasDerivedFrom);
			for (TopLevel topLevel : topLevels) {
				if (topLevel instanceof ComponentDefinition) {
					return (ComponentDefinition) topLevel;
				}
			}
			do {
				displayId = displayId + "_";
				identity = createCompliantURI(URIPrefix,TopLevel.COMPONENT_DEFINITION,displayId,defaultVersion,typesInURI);
				persIdentity = createCompliantURI(URIPrefix,TopLevel.COMPONENT_DEFINITION,displayId,"",typesInURI).toString();
			} while (SBOLDoc.getComponentDefinition(identity)!=null);
			c = c.copy(URIPrefix, displayId, defaultVersion);
			if(identity != componentDef.getIdentity()) {
				c.clearWasDerivedFroms();
				c.addWasDerivedFrom(componentDef.getIdentity());
			}
			SBOLDoc.addComponentDefinition(c);
		} else if (dropObjectsWithDuplicateURIs) {
			return oldC;
		} else {
			if (!c.equals(oldC)) {
				throw new SBOLValidationException("sbol-10202",c);
			}
		}
		return c;
	}

	/**
	 * @param SBOLDoc
	 * @param topLevel
	 * @return
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>if an SBOL validation rule violation occurred in any of the following constructors or methods:
	 * 	<ul>
	 * 		<li>{@link URIcompliance#createCompliantURI(String, String, String, String, boolean)},</li>
	 * 		<li>{@link Sequence#Sequence(URI, String, URI)},</li>
	 * 		<li>{@link Sequence#setVersion(String)},</li>
	 * 		<li>{@link Sequence#setWasDerivedFrom(URI)},</li>
	 * 		<li>{@link Sequence#setDisplayId(String)},</li>
	 * 		<li>{@link Identified#setAnnotations(List)},</li>
	 * 		<li>{@link Sequence#setIdentity(URI)}, or </li>
	 * 		<li>{@link SBOLDocument#addSequence(Sequence)}; or</li>
	 * 	</ul> 
	 * </li>
	 * <li>any of the following SBOL validation rules was violated: 10202, 10204, 10212, 10213.</li>
	 * </ul>
	 */
	private static Sequence parseDnaSequenceV1(SBOLDocument SBOLDoc, IdentifiableDocument<QName> topLevel) throws SBOLValidationException
	{
		String elements    = null;
		String displayId   = null;
		String name   	   = null;
		String description = null;
		URI identity 	   = topLevel.getIdentity();
		URI persistentIdentity = topLevel.getIdentity();
		URI encoding 	   = Sequence.IUPAC_DNA;
		List<Annotation> annotations = new ArrayList<>();

		if (URIPrefix != null)
		{
			displayId = URIcompliance.findDisplayId(topLevel.getIdentity().toString());
			identity = createCompliantURI(URIPrefix,TopLevel.SEQUENCE,displayId,defaultVersion,typesInURI);
			persistentIdentity = createCompliantURI(URIPrefix,TopLevel.SEQUENCE,displayId,"",typesInURI);
		}

		for (NamedProperty<QName> namedProperty : topLevel.getProperties())
		{
			if (namedProperty.getName().equals(Sbol1Terms.DNASequence.nucleotides))
			{
				elements = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", topLevel.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
				if (URIPrefix != null)
				{
					identity = createCompliantURI(URIPrefix,TopLevel.SEQUENCE,displayId,defaultVersion,typesInURI);
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", topLevel.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213", topLevel.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		Sequence sequence = new Sequence(identity, elements, encoding);
		if(persistentIdentity!=null) {
			sequence.setPersistentIdentity(persistentIdentity);
			sequence.setVersion(defaultVersion);
		}
		if(identity != topLevel.getIdentity())
			sequence.addWasDerivedFrom(topLevel.getIdentity());
		if (displayId != null)
			sequence.setDisplayId(displayId);
		if (name != null)
			sequence.setName(name);
		if (description != null)
			sequence.setDescription(description);
		if (!annotations.isEmpty())
			sequence.setAnnotations(annotations);

		Sequence oldS = SBOLDoc.getSequence(identity);
		if (oldS == null) {
			SBOLDoc.addSequence(sequence);
		} else if (sequence.getWasDerivedFroms().size()>0 && oldS.getWasDerivedFroms().size()>0 &&
				!sequence.getWasDerivedFroms().equals(oldS.getWasDerivedFroms())) {
			URI wasDerivedFrom = (URI)sequence.getWasDerivedFroms().toArray()[0];
			Set<TopLevel> topLevels = SBOLDoc.getByWasDerivedFrom(wasDerivedFrom);
			for (TopLevel top : topLevels) {
				if (top instanceof Sequence) {
					return (Sequence) top;
				}
			}
			do {
				displayId = displayId + "_";
				identity = createCompliantURI(URIPrefix,TopLevel.SEQUENCE,displayId,defaultVersion,typesInURI);
				persistentIdentity = createCompliantURI(URIPrefix,TopLevel.SEQUENCE,displayId,"",typesInURI);
			} while (SBOLDoc.getSequence(identity)!=null);
			sequence.setIdentity(identity);
			sequence.setDisplayId(displayId);
			sequence.setPersistentIdentity(persistentIdentity);
			SBOLDoc.addSequence(sequence);
		} else if (dropObjectsWithDuplicateURIs) {
			return oldS;
		} else {
			if (!sequence.equals(oldS)) {
				throw new SBOLValidationException("sbol-10202",sequence);
			}
		}
		return sequence;
	}

	/**
	 * @param SBOLDoc
	 * @param topLevel
	 * @return
	 * @throws SBOLConversionException
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>if an SBOL validation rule violation occurred in any of the following constructors or methods:
	 * 	<ul>
	 * 		<li>{@link URIcompliance#createCompliantURI(String, String, String, String, boolean)},</li>
	 * 		<li>{@link Collection#Collection(URI)},</li>
	 * 		<li>{@link Collection#setVersion(String)},</li>
	 * 		<li>{@link Collection#setWasDerivedFrom(URI)},</li>
	 * 		<li>{@link Collection#setDisplayId(String)},</li>
	 * 		<li>{@link Collection#setMembers(Set)},</li>
	 * 		<li>{@link Identified#setAnnotations(List)}, or</li>
	 * 		<li>{@link SBOLDocument#addCollection(Collection)}; or</li>
	 * 	</ul> 
	 * </li>
	 * <li>the following SBOL validation rule was violated: 10202.</li>
	 * </ul>
	 */
	private static Collection parseCollectionV1(SBOLDocument SBOLDoc, IdentifiableDocument<QName> topLevel) throws SBOLValidationException, SBOLConversionException
	{
		URI identity 	   = topLevel.getIdentity();
		URI persistentIdentity = null;
		String displayId   = null;
		String name 	   = null;
		String description = null;

		Set<URI> members 			 = new HashSet<>();
		List<Annotation> annotations = new ArrayList<>();

		if (URIPrefix != null)
		{
			displayId = URIcompliance.findDisplayId(topLevel.getIdentity().toString());
			identity = createCompliantURI(URIPrefix,TopLevel.SEQUENCE,displayId,defaultVersion,typesInURI);
			persistentIdentity = createCompliantURI(URIPrefix,TopLevel.SEQUENCE,displayId,"",typesInURI);
		}
		for (NamedProperty<QName> namedProperty : topLevel.getProperties())
		{
			if (namedProperty.getName().equals(Sbol1Terms.Collection.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", topLevel.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
				displayId = URIcompliance.fixDisplayId(displayId);
				if (URIPrefix != null)
				{
					identity = createCompliantURI(URIPrefix,TopLevel.COLLECTION,displayId,defaultVersion,typesInURI);
					persistentIdentity = createCompliantURI(URIPrefix,TopLevel.COLLECTION,displayId,"",typesInURI);
				}
			}
			else if (namedProperty.getName().equals(Sbol1Terms.Collection.name))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", topLevel.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol1Terms.Collection.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213",topLevel.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol1Terms.Collection.component))
			{
				if (namedProperty.getValue() instanceof Literal) {
					members.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
				} else {
					members.add(parseDnaComponentV1(SBOLDoc,
							(NestedDocument<QName>) namedProperty.getValue()).getIdentity());
				}
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		//		Collection c = SBOLDoc.createCollection(identity);
		Collection c = new Collection(identity);
		if (persistentIdentity!=null) {
			c.setPersistentIdentity(persistentIdentity);
			c.setVersion(defaultVersion);
		}
		if(identity != topLevel.getIdentity())
			c.addWasDerivedFrom(topLevel.getIdentity());
		if (displayId != null)
			c.setDisplayId(displayId);
		if (name != null)
			c.setName(name);
		if (description != null)
			c.setDescription(description);
		if (!members.isEmpty())
			c.setMembers(members);
		if (!annotations.isEmpty())
			c.setAnnotations(annotations);

		Collection oldC = SBOLDoc.getCollection(topLevel.getIdentity());
		if (oldC == null) {
			SBOLDoc.addCollection(c);
		} else {
			if (!c.equals(oldC)) {
				throw new SBOLValidationException("sbol-10202",c);
			}
		}
		return c;
	}

	/**
	 * @param SBOLDoc
	 * @param sequenceAnnotation
	 * @param precedePairs
	 * @param parentURI
	 * @param sa_num
	 * @param instantiatedComponents
	 * @return
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>if an SBOL validation rule violation occurred in any of the following constructors or methods:
	 * 	<ul>
	 * 		<li>{@link URIcompliance#createCompliantURI(String, String, String)}, </li>
	 * 		<li>{@link #parseDnaComponentV1(SBOLDocument, IdentifiableDocument)}, </li>
	 * 		<li>{@link Range#Range(URI, int, int)}, </li>
	 * 		<li>{@link Range#setDisplayId(String)}, </li>
	 * 		<li>{@link Range#setVersion(String)}, </li>
	 * 		<li>{@link GenericLocation#GenericLocation(URI)}, </li>
	 * 		<li>{@link SequenceAnnotation#SequenceAnnotation(URI, Set)}, </li>
	 * 		<li>{@link SequenceAnnotation#setDisplayId(String)}, </li>
	 * 		<li>{@link SequenceAnnotation#setVersion(String)}, </li>
	 * 		<li>{@link SequenceAnnotation#setWasDerivedFrom(URI)}, </li>
	 * 		<li>{@link SequenceAnnotation#setComponent(URI)}, or </li>
	 * 		<li>{@link SequenceAnnotation#setAnnotations(List)}; or</li>
	 * 	</ul> 
	 * </li>
	 * <li>the following SBOL validation rule was violated: 11002.</li>
	 * </ul>
	 * @throws SBOLConversionException
	 */
	private static SequenceAnnotation parseSequenceAnnotationV1(
			SBOLDocument SBOLDoc, NestedDocument<QName> sequenceAnnotation,
			List<SBOLPair> precedePairs, String parentURI, int sa_num,
			Set<String> instantiatedComponents) throws SBOLValidationException, SBOLConversionException
	{
		Integer start 	 = null;
		Integer end 	 = null;
		String strand    = null;
		URI componentURI = null;
		URI identity 	 = sequenceAnnotation.getIdentity();
		String persIdentity = sequenceAnnotation.getIdentity().toString();
		List<Annotation> annotations = new ArrayList<>();

		if (URIPrefix != null)
		{
			persIdentity = createCompliantURI(parentURI,"annotation"+sa_num,"").toString();
			identity = createCompliantURI(parentURI,"annotation"+sa_num,defaultVersion);
		}

		if (!sequenceAnnotation.getType().equals(Sbol1Terms.SequenceAnnotations.SequenceAnnotation))
		{
			throw new SBOLConversionException("QName has to be" + Sbol1Terms.SequenceAnnotations.SequenceAnnotation.toString());
		}

		for (NamedProperty<QName> namedProperty : sequenceAnnotation.getProperties())
		{
			if (namedProperty.getName().equals(Sbol1Terms.SequenceAnnotations.bioStart))
			{
				if (!(namedProperty.getValue() instanceof Literal) || start != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-11102",sequenceAnnotation.getIdentity());
				}
				String temp = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
				start = Integer.parseInt(temp);
			}
			else if (namedProperty.getName().equals(Sbol1Terms.SequenceAnnotations.bioEnd))
			{
				if (!(namedProperty.getValue() instanceof Literal) || end != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-11103",sequenceAnnotation.getIdentity());
				}
				String temp2 = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
				end = Integer.parseInt(temp2);
			}
			else if (namedProperty.getName().equals(Sbol1Terms.SequenceAnnotations.strand))
			{
				if (!(namedProperty.getValue() instanceof Literal) || strand != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-11002",sequenceAnnotation.getIdentity());
				}
				strand = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol1Terms.SequenceAnnotations.subComponent))
			{
				if (componentURI != null) {
					throw new SBOLValidationException("sbol-10904", sequenceAnnotation.getIdentity());
				}
				if (namedProperty.getValue() instanceof NestedDocument) {
					componentURI = parseDnaComponentV1(SBOLDoc,
							(NestedDocument<QName>) namedProperty.getValue()).getIdentity();
				} else {
					if (!(namedProperty.getValue() instanceof Literal) ||
							(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
						throw new SBOLValidationException("sbol-10904", sequenceAnnotation.getIdentity());
					}
					componentURI = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
				}
			}
			else if (namedProperty.getName().equals(Sbol1Terms.SequenceAnnotations.precedes))
			{
				URI left 	  = sequenceAnnotation.getIdentity();
				URI right	  = null;
				if (namedProperty.getValue() instanceof NestedDocument) {
					// TODO: need to check if ++sa_num here okay
					right = parseSequenceAnnotationV1(SBOLDoc,
							(NestedDocument<QName>) namedProperty.getValue(), precedePairs, parentURI, ++sa_num, instantiatedComponents).getIdentity();
				} else {
					if (!(namedProperty.getValue() instanceof Literal) ||
							(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
						throw new SBOLValidationException("sbol-11404", sequenceAnnotation.getIdentity());
					}
					right 	  = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
				}
				SBOLPair pair = new SBOLPair(left, right);
				precedePairs.add(pair);
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}
		String componentDisplayId = URIcompliance.extractDisplayId(componentURI);
		String displayId = "annotation" + sa_num;
		if (compliant && componentDisplayId!=null && 
				!instantiatedComponents.contains(componentDisplayId)) {
			identity = createCompliantURI(parentURI,componentDisplayId+"_annotation",defaultVersion);
			persIdentity = createCompliantURI(parentURI,componentDisplayId+"_annotation","").toString();
			displayId = componentDisplayId + "_annotation";
		}

		Location location = null; // Note: Do not create a seqAnnotation if Location is empty

		if (start != null && end != null) // create SequenceAnnotation & Component
		{
			URI range_identity = createCompliantURI(persIdentity,"range",defaultVersion);
			location = new Range(range_identity, start, end);
			if (!persIdentity.equals("")) {
				location.setPersistentIdentity(createCompliantURI(persIdentity,"range",""));
				location.setDisplayId("range");
				location.setVersion(defaultVersion);
			}
			if (strand != null)
			{
				if (strand.equals("+"))
				{
					location.setOrientation(OrientationType.INLINE);
				}
				else if (strand.equals("-"))
				{
					location.setOrientation(OrientationType.REVERSECOMPLEMENT);
				}
			}
		}
		else
		{
			URI dummyGenericLoc_id = createCompliantURI(persIdentity,"genericLocation",defaultVersion);
			location = new GenericLocation(dummyGenericLoc_id);
			if (!persIdentity.equals("")) {
				location.setPersistentIdentity(createCompliantURI(persIdentity,"genericLocation",""));
				location.setDisplayId("genericLocation");
				location.setVersion(defaultVersion);
			}
			if (strand != null)
			{
				if (strand.equals("+"))
				{
					location.setOrientation(OrientationType.INLINE);
				}
				else if (strand.equals("-"))
				{
					location.setOrientation(OrientationType.REVERSECOMPLEMENT);
				}
			}
		}

		Set<Location> locations = new HashSet<>();
		locations.add(location);
		SequenceAnnotation s = new SequenceAnnotation(identity, locations);
		if(!persIdentity.equals("")) {
			s.setPersistentIdentity(URI.create(persIdentity));
			s.setDisplayId(displayId);
			s.setVersion(defaultVersion);
		}
		if(identity != sequenceAnnotation.getIdentity())
			s.addWasDerivedFrom(sequenceAnnotation.getIdentity());
		if (componentURI != null)
			s.setComponent(componentURI);
		if (!annotations.isEmpty())
			s.setAnnotations(annotations);

		return s;
	}
	
	/**
	 * @param SBOLDoc
	 * @param topLevel
	 * @param nested
	 * @return
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>any of the following SBOL validation rules was violated: 
	 * 10202, 10203, 10204, 10206, 10208, 10212, 10213, 10502, 10507, 10512, or</li>
	 * <li>an SBOL validation rule violation occurred in the following constructor or methods:
	 * 	<ul>
	 * 		<li>{@link #parseComponent(NestedDocument, Map)},</li>
	 * 		<li>{@link #parseSequenceAnnotation(NestedDocument, Map)},</li>
	 * 		<li>{@link ComponentDefinition#ComponentDefinition(URI, Set)}, </li>
	 * 		<li>{@link ComponentDefinition#setDisplayId(String)}, </li>
	 * 		<li>{@link ComponentDefinition#setVersion(String)}, </li>
	 * 		<li>{@link ComponentDefinition#setWasDerivedFrom(URI)}, </li>
	 * 		<li>{@link Identified#setAnnotations(List)},</li>
	 * 		<li>{@link ComponentDefinition#setComponents(Set)},</li>
	 * 		<li>{@link ComponentDefinition#setSequenceAnnotations(Set)},</li>
	 * 		<li>{@link ComponentDefinition#setSequenceConstraints(Set)}, or</li>
	 * 		<li>{@link SBOLDocument#addComponentDefinition(ComponentDefinition)}.</li>
	 * 	</ul>
	 * </li>
	 * </ul>
	 */
	@SuppressWarnings("unchecked")
	private static ComponentDefinition parseComponentDefinition(SBOLDocument SBOLDoc, 
			IdentifiableDocument<QName> topLevel, Map<URI, NestedDocument<QName>> nested) throws SBOLValidationException
	{
		String displayId 	   = null;//URIcompliance.extractDisplayId(topLevel.getIdentity());
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;//URI.create(URIcompliance.extractPersistentId(topLevel.getIdentity()));
		String version 		   = null;
		Set<URI> wasDerivedFroms = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		Set<URI> attachments = new HashSet<>();
		Set<URI> type 		   = new HashSet<>();
		Set<URI> roles 	  	   = new HashSet<>();
		Set<URI> sequences	   = new HashSet<>();

		Set<Component> components 					 = new HashSet<>();
		List<Annotation> annotations 				 = new ArrayList<>();
		Set<SequenceAnnotation> sequenceAnnotations = new HashSet<>();
		Set<SequenceConstraint> sequenceConstraints = new HashSet<>();

		for (NamedProperty<QName> namedProperty : topLevel.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", topLevel.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", topLevel.getIdentity());
				}
				persistentIdentity  = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", topLevel.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.ComponentDefinition.type))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10502", topLevel.getIdentity());
				}
				type.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.ComponentDefinition.roles))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10507", topLevel.getIdentity());
				}
				roles.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.ComponentDefinition.hasComponent)||
					namedProperty.getName().equals(Sbol2Terms.ComponentDefinition.hasSubComponent))
			{
				if (namedProperty.getValue() instanceof NestedDocument) {
					NestedDocument<QName> nestedDocument = ((NestedDocument<QName>) namedProperty.getValue());
					if (nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.Component.Component)) {
						throw new SBOLValidationException("sbol-10519",topLevel.getIdentity());
					}
					components.add(parseComponent(SBOLDoc,((NestedDocument<QName>) namedProperty.getValue()), nested));
				}
				else {
					URI uri = (URI) ((Literal<QName>)namedProperty.getValue()).getValue();
					NestedDocument<QName> nestedDocument = nested.get(uri);
					if (nestedDocument==null || nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.Component.Component)) {
						throw new SBOLValidationException("sbol-10519",topLevel.getIdentity());
					}
					components.add(parseComponent(SBOLDoc,nested.get(uri), nested));
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.ComponentDefinition.hasSequence))
			{
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-10512", topLevel.getIdentity());
					}
					sequences.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
				}
				else if (namedProperty.getValue() instanceof IdentifiableDocument) {
					if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.Sequence.Sequence)) {
						Sequence sequence = parseSequence(SBOLDoc,(IdentifiableDocument<QName>)namedProperty.getValue());
						sequences.add(sequence.getIdentity());
					} else {
						throw new SBOLValidationException("sbol-10512", topLevel.getIdentity());
					}
				}
				else {
					throw new SBOLValidationException("sbol-10512", topLevel.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.ComponentDefinition.hasSequenceAnnotations))
			{
				if (namedProperty.getValue() instanceof NestedDocument) {
					NestedDocument<QName> nestedDocument = ((NestedDocument<QName>) namedProperty.getValue());
					if (nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.SequenceAnnotation.SequenceAnnotation)) {
						throw new SBOLValidationException("sbol-10521",topLevel.getIdentity());
					}
					sequenceAnnotations.add(parseSequenceAnnotation(((NestedDocument<QName>) namedProperty.getValue()), nested));
				}
				else {
					URI uri = (URI) ((Literal<QName>)namedProperty.getValue()).getValue();
					NestedDocument<QName> nestedDocument = nested.get(uri);
					if (nestedDocument==null || nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.SequenceAnnotation.SequenceAnnotation)) {
						throw new SBOLValidationException("sbol-10521",topLevel.getIdentity());
					}
					sequenceAnnotations.add(parseSequenceAnnotation(nested.get(uri), nested));
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.ComponentDefinition.hasSequenceConstraints))
			{
				if (namedProperty.getValue() instanceof NestedDocument) {
					NestedDocument<QName> nestedDocument = ((NestedDocument<QName>) namedProperty.getValue());
					if (nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.SequenceConstraint.SequenceConstraint)) {
						throw new SBOLValidationException("sbol-10524",topLevel.getIdentity());
					}
					sequenceConstraints.add(parseSequenceConstraint(((NestedDocument<QName>) namedProperty.getValue())));
				}
				else {
					URI uri = (URI) ((Literal<QName>)namedProperty.getValue()).getValue();
					NestedDocument<QName> nestedDocument = nested.get(uri);
					if (nestedDocument==null || nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.SequenceConstraint.SequenceConstraint)) {
						throw new SBOLValidationException("sbol-10524",topLevel.getIdentity());
					}
					sequenceConstraints.add(parseSequenceConstraint(nested.get(uri)));
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", topLevel.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213",topLevel.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208",topLevel.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",topLevel.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.TopLevel.hasAttachment))
			{
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
					attachments.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
				}
				else if (namedProperty.getValue() instanceof IdentifiableDocument) {
					if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.Attachment.Attachment)) {
						Attachment attachment = parseAttachment(SBOLDoc,(IdentifiableDocument<QName>)namedProperty.getValue());
						attachments.add(attachment.getIdentity());
					} else {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
				}
				else {
					throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
						.equals(Sbol2Terms.ComponentDefinition.ComponentDefinition.getNamespaceURI()+Sbol2Terms.ComponentDefinition.ComponentDefinition.getLocalPart())) {
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		//ComponentDefinition c = SBOLDoc.createComponentDefinition(topLevel.getIdentity(), type, roles);
		//c.setPersistentIdentity(topLevel.getOptionalUriPropertyValue(Sbol2Terms.Identified.persistentIdentity));
		//		ComponentDefinition c = SBOLDoc.createComponentDefinition(topLevel.getIdentity(), type);
		ComponentDefinition c = new ComponentDefinition(topLevel.getIdentity(), type);
		if(roles != null)
			c.setRoles(roles);
		if (displayId != null)
			c.setDisplayId(displayId);
		if (persistentIdentity != null)
			c.setPersistentIdentity(persistentIdentity);
		if (!sequences.isEmpty())
			c.setSequences(sequences);
		if (!components.isEmpty())
			c.setComponents(components);
		if (!sequenceAnnotations.isEmpty())
			c.setSequenceAnnotations(sequenceAnnotations);
		if (!sequenceConstraints.isEmpty())
			c.setSequenceConstraints(sequenceConstraints);
		if (name != null)
			c.setName(name);
		if (description != null)
			c.setDescription(description);
		if (!annotations.isEmpty())
			c.setAnnotations(annotations);
		if (version != null)
			c.setVersion(version);
		c.setWasDerivedFroms(wasDerivedFroms);
		c.setWasGeneratedBys(wasGeneratedBys);
		c.setAttachments(attachments);

		ComponentDefinition oldC = SBOLDoc.getComponentDefinition(topLevel.getIdentity());
		if (oldC == null) {
			SBOLDoc.addComponentDefinition(c);
		} else {
			if (!c.equals(oldC)) {
				throw new SBOLValidationException("sbol-10202",c);
			}
		}
		return c;
	}
	
	/**
	 * @param SBOLDoc
	 * @param topLevel
	 * @param nested
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static CombinatorialDerivation parseCombinatorialDerivation(SBOLDocument SBOLDoc, 
			IdentifiableDocument<QName> topLevel, Map<URI, NestedDocument<QName>> nested) throws SBOLValidationException
	{
		String displayId 	   = null;//URIcompliance.extractDisplayId(topLevel.getIdentity());
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;//URI.create(URIcompliance.extractPersistentId(topLevel.getIdentity()));
		URI template 		   = null;
		StrategyType strategy  = null;
		String version 		   = null;
		Set<URI> wasDerivedFroms = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		Set<URI> attachments = new HashSet<>();

		Set<VariableComponent> variableComponents    = new HashSet<>();
		List<Annotation> annotations 				 = new ArrayList<>();

		for (NamedProperty<QName> namedProperty : topLevel.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", topLevel.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", topLevel.getIdentity());
				}
				persistentIdentity  = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.CombinatorialDerivation.template))
			{
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-12904", topLevel.getIdentity());
					}
					template =URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
				}
				else if (namedProperty.getValue() instanceof IdentifiableDocument) {
					if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.ComponentDefinition.ComponentDefinition)) {
						ComponentDefinition cd = parseComponentDefinition(SBOLDoc,(IdentifiableDocument<QName>)namedProperty.getValue(),nested);
						template = cd.getIdentity();
					} else {
						throw new SBOLValidationException("sbol-12904", topLevel.getIdentity());
					}
				}
				else {
					throw new SBOLValidationException("sbol-12904", topLevel.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.CombinatorialDerivation.strategy))
			{
				if (!(namedProperty.getValue() instanceof Literal) || strategy != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-12914", topLevel.getIdentity());
				}
				String strategyTypeStr = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
				try {
					strategy = StrategyType.convertToStrategyType(URI.create(strategyTypeStr));
				}
				catch (SBOLValidationException e) {
					throw new SBOLValidationException("sbol-12902", topLevel.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", topLevel.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.CombinatorialDerivation.hasVariableComponent))
			{
				if (namedProperty.getValue() instanceof NestedDocument) {
					NestedDocument<QName> nestedDocument = ((NestedDocument<QName>) namedProperty.getValue());
					
					if (nestedDocument.getType() == null || 
							!nestedDocument.getType().equals(Sbol2Terms.VariableComponent.VariableComponent)) {
						throw new SBOLValidationException("sbol-12906",topLevel.getIdentity());
					}
					variableComponents.add(parseVariableComponent(SBOLDoc,((NestedDocument<QName>) namedProperty.getValue()), nested));
				}
				else {
					URI uri = (URI) ((Literal<QName>)namedProperty.getValue()).getValue();
					NestedDocument<QName> nestedDocument = nested.get(uri);
					if (nestedDocument == null || nestedDocument.getType() == null || 
							!nestedDocument.getType().equals(Sbol2Terms.VariableComponent.VariableComponent)) {
						throw new SBOLValidationException("sbol-12906",topLevel.getIdentity());
					}
					variableComponents.add(parseVariableComponent(SBOLDoc,nested.get(uri), nested));
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", topLevel.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213",topLevel.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208",topLevel.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",topLevel.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.TopLevel.hasAttachment))
			{
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
					attachments.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
				}
				else if (namedProperty.getValue() instanceof IdentifiableDocument) {
					if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.Attachment.Attachment)) {
						Attachment attachment = parseAttachment(SBOLDoc,(IdentifiableDocument<QName>)namedProperty.getValue());
						attachments.add(attachment.getIdentity());
					} else {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
				}
				else {
					throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.CombinatorialDerivation.CombinatorialDerivation.getNamespaceURI()+Sbol2Terms.CombinatorialDerivation.CombinatorialDerivation.getLocalPart())) {
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		CombinatorialDerivation c = new CombinatorialDerivation(topLevel.getIdentity(), template);

		if (strategy != null) 
			c.setStrategy(strategy);
		if (displayId != null)
			c.setDisplayId(displayId);
		if (persistentIdentity != null)
			c.setPersistentIdentity(persistentIdentity);
		if (name != null)
			c.setName(name);
		if (!variableComponents.isEmpty()) 
			c.setVariableComponents(variableComponents);
		if (description != null)
			c.setDescription(description);
		if (!annotations.isEmpty())
			c.setAnnotations(annotations);
		if (version != null)
			c.setVersion(version);
		c.setWasDerivedFroms(wasDerivedFroms);
		c.setWasGeneratedBys(wasGeneratedBys);
		c.setAttachments(attachments);

		CombinatorialDerivation oldC = SBOLDoc.getCombinatorialDerivation(topLevel.getIdentity());
		if (oldC == null) {
			SBOLDoc.addCombinatorialDerivation(c);
		} else {
			if (!c.equals(oldC)) {
				throw new SBOLValidationException("sbol-10202", c);
			}
		}
		return c;
	}

	//TODO: FIX COMMENTED SECTION
	@SuppressWarnings("unchecked")
	private static VariableComponent parseVariableComponent(SBOLDocument SBOLDoc, NestedDocument<QName> variableComponent,
			Map<URI, NestedDocument<QName>> nested) throws SBOLValidationException {
		String displayId 	   			= null;
		String name 	 	   			= null;
		String description 	    		= null;
		URI persistentIdentity			= null;
		String version					= null;
		List<Annotation> annotations 	= new ArrayList<>();
		URI variable					= null;
		OperatorType operator			= null;
		HashSet<URI> variants			= new HashSet<>();
		HashSet<URI> variantCollections	= new HashSet<>();
		HashSet<URI> variantDerivations	= new HashSet<>();
		Set<URI> wasDerivedFroms = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		

		for (NamedProperty<QName> namedProperty : variableComponent.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", variableComponent.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", variableComponent.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", variableComponent.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", variableComponent.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213",variableComponent.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if(namedProperty.getName().equals(Sbol2Terms.VariableComponent.hasVariable))
			{
				if (!(namedProperty.getValue() instanceof Literal) || variable != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-13004",variableComponent.getIdentity());
				}
				variable = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.VariableComponent.hasVariants))
			{
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-13007", variableComponent.getIdentity());
					}
					variants.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
				}
				else if (namedProperty.getValue() instanceof IdentifiableDocument) {
					if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.ComponentDefinition.ComponentDefinition)) {
						ComponentDefinition cd = parseComponentDefinition(SBOLDoc,(IdentifiableDocument<QName>)namedProperty.getValue(),nested);
						variants.add(cd.getIdentity());
					} else {
						throw new SBOLValidationException("sbol-13007", variableComponent.getIdentity());
					}
				}
				else {
					throw new SBOLValidationException("sbol-13007", variableComponent.getIdentity());
				}
			}
			else if(namedProperty.getName().equals(Sbol2Terms.VariableComponent.hasVariantCollections))
			{
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-13009", variableComponent.getIdentity());
					}
					variantCollections.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
				}
				else if (namedProperty.getValue() instanceof IdentifiableDocument) {
					if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.Collection.Collection)) {
						Collection cd = parseCollection(SBOLDoc,(IdentifiableDocument<QName>)namedProperty.getValue(),nested);
						variantCollections.add(cd.getIdentity());
					} else {
						throw new SBOLValidationException("sbol-13009", variableComponent.getIdentity());
					}
				}
				else {
					throw new SBOLValidationException("sbol-13009", variableComponent.getIdentity());
				}
			}
			else if(namedProperty.getName().equals(Sbol2Terms.VariableComponent.hasVariantDerivations))
			{
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-13013", variableComponent.getIdentity());
					}
					variantDerivations.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
				}
				else if (namedProperty.getValue() instanceof IdentifiableDocument) {
					if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.Collection.Collection)) {
						CombinatorialDerivation cd = parseCombinatorialDerivation(SBOLDoc,(IdentifiableDocument<QName>)namedProperty.getValue(),nested);
						variantDerivations.add(cd.getIdentity());
					} else {
						throw new SBOLValidationException("sbol-13013", variableComponent.getIdentity());
					}
				}
				else {
					throw new SBOLValidationException("sbol-13013", variableComponent.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.VariableComponent.hasOperator))
			{
				if (!(namedProperty.getValue() instanceof Literal) || operator != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-13002", variableComponent.getIdentity());
				}
				String operatorTypeStr = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
				try {
					operator = OperatorType.convertToOperatorType(URI.create(operatorTypeStr));
				}
				catch (SBOLValidationException e) {
					throw new SBOLValidationException("sbol-13003", variableComponent.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) || 
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208", variableComponent.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",variableComponent.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.VariableComponent.VariableComponent.getNamespaceURI()+Sbol2Terms.VariableComponent.VariableComponent.getLocalPart())) {
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		VariableComponent c = new VariableComponent(variableComponent.getIdentity(), operator, variable);
		
		if (persistentIdentity != null)
			c.setPersistentIdentity(persistentIdentity);
		if(version != null)
			c.setVersion(version);
		if (displayId != null)
			c.setDisplayId(displayId);
		if (!variants.isEmpty())
			c.setVariants(variants);
		if (!variantCollections.isEmpty())
			c.setVariantCollections(variantCollections);
		if (!variantDerivations.isEmpty())
			c.setVariantDerivations(variantDerivations);
		if (name != null)
			c.setName(name);
		if (description != null)
			c.setDescription(description);
		if (!annotations.isEmpty())
			c.setAnnotations(annotations);

		return c;
	}
	
	/**
	 * @param SBOLDoc
	 * @param topLevel
	 * @param nested
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Implementation parseImplementation(SBOLDocument SBOLDoc, 
			IdentifiableDocument<QName> topLevel, Map<URI, NestedDocument<QName>> nested) throws SBOLValidationException
	{
		String displayId 	   = null;//URIcompliance.extractDisplayId(topLevel.getIdentity());
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;//URI.create(URIcompliance.extractPersistentId(topLevel.getIdentity()));
		URI built 		   = null;
		String version 		   = null;
		Set<URI> wasDerivedFroms = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		Set<URI> attachments = new HashSet<>();

		List<Annotation> annotations 				 = new ArrayList<>();

		for (NamedProperty<QName> namedProperty : topLevel.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", topLevel.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", topLevel.getIdentity());
				}
				persistentIdentity  = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Implementation.built))
			{
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-13102", topLevel.getIdentity());
					}
					built = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
				}
				else if (namedProperty.getValue() instanceof IdentifiableDocument) {
					if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.ModuleDefinition.ModuleDefinition)) {
						ModuleDefinition md = parseModuleDefinition(SBOLDoc,(IdentifiableDocument<QName>)namedProperty.getValue(),nested);
						built = md.getIdentity();
					} else if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.ComponentDefinition.ComponentDefinition)) {
						ComponentDefinition md = parseComponentDefinition(SBOLDoc,(IdentifiableDocument<QName>)namedProperty.getValue(),nested);
						built = md.getIdentity();
					} else {
						throw new SBOLValidationException("sbol-13102", topLevel.getIdentity());
					}
				}
				else {
					throw new SBOLValidationException("sbol-13102", topLevel.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", topLevel.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", topLevel.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213",topLevel.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208",topLevel.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.TopLevel.hasAttachment))
			{
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
					attachments.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
				}
				else if (namedProperty.getValue() instanceof IdentifiableDocument) {
					if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.Attachment.Attachment)) {
						Attachment attachment = parseAttachment(SBOLDoc,(IdentifiableDocument<QName>)namedProperty.getValue());
						attachments.add(attachment.getIdentity());
					} else {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
				}
				else {
					throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",topLevel.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.Implementation.Implementation.getNamespaceURI()+Sbol2Terms.Implementation.Implementation.getLocalPart())) {
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		Implementation i = new Implementation(topLevel.getIdentity());

		if (displayId != null)
			i.setDisplayId(displayId);
		if (persistentIdentity != null)
			i.setPersistentIdentity(persistentIdentity);
		if (name != null)
			i.setName(name);
		if (description != null)
			i.setDescription(description);
		if (!annotations.isEmpty())
			i.setAnnotations(annotations);
		if (version != null)
			i.setVersion(version);
		if (built != null) 
			i.setBuilt(built);
		i.setWasDerivedFroms(wasDerivedFroms);
		i.setWasGeneratedBys(wasGeneratedBys);
		i.setAttachments(attachments);

		Implementation oldI = SBOLDoc.getImplementation(topLevel.getIdentity());
		if (oldI == null) {
			SBOLDoc.addImplementation(i);
		} else {
			if (!i.equals(oldI)) {
				throw new SBOLValidationException("sbol-10202", i);
			}
		}
		return i;
	}

	private static SequenceConstraint parseSequenceConstraint(NestedDocument<QName> sequenceConstraint) throws SBOLValidationException
	{
		String displayId 	   = null;//URIcompliance.extractDisplayId(sequenceConstraint.getIdentity());
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;//URI.create(URIcompliance.extractPersistentId(sequenceConstraint.getIdentity()));
		URI restriction  			 = null;
		URI subject 				 = null;
		URI object 					 = null;
		String version 				 = null;
		Set<URI> wasDerivedFroms	 = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		List<Annotation> annotations = new ArrayList<>();

		for (NamedProperty<QName> namedProperty : sequenceConstraint.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", sequenceConstraint.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty
						.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(
					Sbol2Terms.SequenceConstraint.restriction))
			{
				if (!(namedProperty.getValue() instanceof Literal) || restriction != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-11407", sequenceConstraint.getIdentity());
				}
				restriction = URI.create(((Literal<QName>) namedProperty
						.getValue()).getValue().toString());

			}
			else if (namedProperty.getName().equals(Sbol2Terms.SequenceConstraint.hasSubject))
			{
				if (!(namedProperty.getValue() instanceof Literal) || subject != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-11402", sequenceConstraint.getIdentity());
				}
				subject = URI
						.create(((Literal<QName>) namedProperty.getValue())
								.getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.SequenceConstraint.hasObject))
			{
				if (!(namedProperty.getValue() instanceof Literal) || object != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-11404", sequenceConstraint.getIdentity());
				}
				object = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", sequenceConstraint.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", sequenceConstraint.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213",sequenceConstraint.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", sequenceConstraint.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208",sequenceConstraint.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",sequenceConstraint.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.SequenceConstraint.SequenceConstraint.getNamespaceURI()+Sbol2Terms.SequenceConstraint.SequenceConstraint.getLocalPart())) {
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		SequenceConstraint s = new SequenceConstraint(sequenceConstraint.getIdentity(), restriction, subject, object);
		if (displayId != null)
			s.setDisplayId(displayId);
		if (name != null)
			s.setName(name);
		if (description != null)
			s.setDescription(description);
		if (persistentIdentity != null)
			s.setPersistentIdentity(persistentIdentity);
		if (version != null)
			s.setVersion(version);
		s.setWasDerivedFroms(wasDerivedFroms);		
		s.setWasGeneratedBys(wasGeneratedBys);
		if (!annotations.isEmpty())
			s.setAnnotations(annotations);
		return s;
	}

	/**
	 * @param sequenceAnnotation
	 * @param nested
	 * @return
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>any of the following SBOL validation rules was violated:
	 * 10203, 10204, 10206, 10208, 10212, 10213, 
	 * 10512,
	 * 10904; or 
	 *</li>
	 * <li>an SBOL validation rule violation occurred in the following constructor or methods:
	 * 	<ul>
	 * 		<li>{@link #parseLocation(NestedDocument)},</li>
	 * 		<li>{@link SequenceAnnotation#SequenceAnnotation(URI, Set)},</li>
	 * 		<li>{@link SequenceAnnotation#setDisplayId(String)},</li>
	 * 		<li>{@link SequenceAnnotation#setVersion(String)},</li>
	 * 		<li>{@link SequenceAnnotation#setComponent(URI)},</li>
	 * 		<li>{@link SequenceAnnotation#setWasDerivedFrom(URI)}, or</li>
	 * 		<li>{@link Identified#setAnnotations(List)}.</li>
	 * 	</ul>
	 * </li>
	 * </ul>
	 */
	private static SequenceAnnotation parseSequenceAnnotation(NestedDocument<QName> sequenceAnnotation, Map<URI, NestedDocument<QName>> nested) throws SBOLValidationException
	{
		String displayId 	   = null;//URIcompliance.extractDisplayId(sequenceAnnotation.getIdentity());
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;//URI.create(URIcompliance.extractPersistentId(sequenceAnnotation.getIdentity()));
		Location location 	   = null;
		URI componentURI 	   = null;
		String version   	   = null;
		Set<URI> wasDerivedFroms = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		Set<URI> roles 	  	   = new HashSet<>();
		Set<Location> locations = new HashSet<>();
		List<Annotation> annotations = new ArrayList<>();

		for (NamedProperty<QName> namedProperty : sequenceAnnotation.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", sequenceAnnotation.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", sequenceAnnotation.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", sequenceAnnotation.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.SequenceAnnotation.roles))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10906", sequenceAnnotation.getIdentity());
				}
				roles.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Location.Location))
			{
				if (namedProperty.getValue() instanceof NestedDocument) {
					NestedDocument<QName> nestedDocument = ((NestedDocument<QName>) namedProperty.getValue());
					if (nestedDocument.getType()==null || 
							!(nestedDocument.getType().equals(Sbol2Terms.Range.Range) ||
									nestedDocument.getType().equals(Sbol2Terms.Cut.Cut) ||
									nestedDocument.getType().equals(Sbol2Terms.GenericLocation.GenericLocation))) {
						throw new SBOLValidationException("sbol-10902",sequenceAnnotation.getIdentity());
					}
					location = parseLocation((NestedDocument<QName>) namedProperty.getValue());
				}
				else {
					URI uri = (URI) ((Literal<QName>)namedProperty.getValue()).getValue();
					NestedDocument<QName> nestedDocument = nested.get(uri);
					if (nestedDocument==null || nestedDocument.getType()==null || 
							!(nestedDocument.getType().equals(Sbol2Terms.Range.Range) ||
									nestedDocument.getType().equals(Sbol2Terms.Cut.Cut) ||
									nestedDocument.getType().equals(Sbol2Terms.GenericLocation.GenericLocation))) {
						throw new SBOLValidationException("sbol-10902",sequenceAnnotation.getIdentity());
					}
					location = parseLocation(nested.get(uri));
				}
				locations.add(location);
			}
			else if (namedProperty.getName().equals(Sbol2Terms.SequenceAnnotation.hasComponent))
			{
				if (!(namedProperty.getValue() instanceof Literal) || componentURI != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10904", sequenceAnnotation.getIdentity());
				}
				componentURI = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", sequenceAnnotation.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213", sequenceAnnotation.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208",sequenceAnnotation.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",sequenceAnnotation.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.SequenceAnnotation.SequenceAnnotation.getNamespaceURI()+Sbol2Terms.SequenceAnnotation.SequenceAnnotation.getLocalPart())) {
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		SequenceAnnotation s = new SequenceAnnotation(sequenceAnnotation.getIdentity(), locations);

		if (persistentIdentity != null)
			s.setPersistentIdentity(persistentIdentity);
		if(version != null)
			s.setVersion(version);
		if (displayId != null)
			s.setDisplayId(displayId);
		if (componentURI != null)
			s.setComponent(componentURI);
		if (name != null)
			s.setName(name);
		if (description != null)
			s.setDescription(description);
		s.setWasDerivedFroms(wasDerivedFroms);
		s.setWasGeneratedBys(wasGeneratedBys);
		if (!annotations.isEmpty())
			s.setAnnotations(annotations);
		if (!roles.isEmpty())
			s.setRoles(roles);
//		if (roleIntegration != null)
//			try {
//				s.setRoleIntegration(RoleIntegrationType.convertToRoleIntegrationType(roleIntegration));
//			} catch (SBOLValidationException e) {
//				throw new SBOLValidationException("sbol-10912",s);
//			}
		return s;
	}

	/**
	 * @param location
	 * @return
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>the following SBOL validation rules was violated: 10902
	 *</li>
	 * <li>an SBOL validation rule violation occurred in the following constructor or methods:
	 * 	<ul>
	 * 		<li>{@link #parseRange(NestedDocument)},</li>
	 * 		<li>{@link #parseCut(NestedDocument)}, or</li>
	 * 		<li>{@link #parseGenericLocation(NestedDocument)}.</li>
	 * 	</ul>
	 * </li>
	 * </ul>
	 */
	private static Location parseLocation(NestedDocument<QName> location) throws SBOLValidationException
	{
		Location l 					 = null;
		if (location.getType().equals(Sbol2Terms.Range.Range))
		{
			l = parseRange(location);
		}
		else if (location.getType().equals(Sbol2Terms.Cut.Cut))
		{
			l = parseCut(location);
		}
		else if (location.getType().equals(Sbol2Terms.GenericLocation.GenericLocation))
		{
			l = parseGenericLocation(location);
		}
		return l;

	}

	/**
	 * @param typeGenLoc
	 * @return
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>any of the following SBOL validation rules was violated:
	 * 10203, 10204, 10206, 10208, 10212, 10213, 
	 * 11002; or  
	 *</li>
	 * <li>an SBOL validation rule violation occurred in the following constructor or methods:
	 * 	<ul>
	 * 		<li>{@link GenericLocation#GenericLocation(URI)},</li>
	 * 		<li>{@link GenericLocation#setDisplayId(String)},</li>
	 * 		<li>{@link GenericLocation#setVersion(String)},</li>	
	 * 		<li>{@link GenericLocation#setWasDerivedFrom(URI)}, or</li>
	 * 		<li>{@link Identified#setAnnotations(List)}.</li>
	 * 	</ul>
	 * </li>
	 * </ul>
	 */
	private static GenericLocation parseGenericLocation(NestedDocument<QName> typeGenLoc) throws SBOLValidationException
	{
		String displayId 	   = null;//URIcompliance.extractDisplayId(typeGenLoc.getIdentity());
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;//URI.create(URIcompliance.extractPersistentId(typeGenLoc.getIdentity()));
		URI orientation 			 = null;
		URI sequence	 			 = null;
		String version        	     = null;
		Set<URI> wasDerivedFroms	 = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		List<Annotation> annotations = new ArrayList<>();

		for (NamedProperty<QName> namedProperty : typeGenLoc.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.GenericLocation.orientation)||
					namedProperty.getName().equals(Sbol2Terms.GenericLocation.Orientation))
			{
				if (!(namedProperty.getValue() instanceof Literal) || orientation != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-11002", typeGenLoc.getIdentity());
				}
				orientation = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Location.sequence))
			{
				if (!(namedProperty.getValue() instanceof Literal) || orientation != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-11003", typeGenLoc.getIdentity());
				}
				sequence = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", typeGenLoc.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", typeGenLoc.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213",typeGenLoc.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", typeGenLoc.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", typeGenLoc.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208",typeGenLoc.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",typeGenLoc.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.GenericLocation.GenericLocation.getNamespaceURI()+Sbol2Terms.GenericLocation.GenericLocation.getLocalPart())) {
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		GenericLocation gl = new GenericLocation(typeGenLoc.getIdentity());
		if(displayId != null)
			gl.setDisplayId(displayId);
		if (name != null)
			gl.setName(name);
		if (description != null)
			gl.setDescription(description);
		if(orientation != null)
			try {
				gl.setOrientation(OrientationType.convertToOrientationType(orientation));
			} catch (SBOLValidationException e) {
				throw new SBOLValidationException("sbol-11002",gl);
			}
		if(sequence != null) {
			gl.setSequence(sequence);
		}
		if(persistentIdentity != null)
			gl.setPersistentIdentity(persistentIdentity);
		if(version != null)
			gl.setVersion(version);
		gl.setWasDerivedFroms(wasDerivedFroms);
		gl.setWasGeneratedBys(wasGeneratedBys);
		if (!annotations.isEmpty())
			gl.setAnnotations(annotations);

		return gl;
	}

	/**
	 * @param typeCut
	 * @return
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>any of the following SBOL validation rules was violated: 
	 * 10203, 10204, 10206, 10208, 10212, 10213,
	 * 11002, 11202; or 
	 *</li>
	 * <li>an SBOL validation rule violation occurred in the following constructor or methods:
	 * 	<ul>
	 * 		<li>{@link Cut#Cut(URI, int)},</li>
	 * 		<li>{@link Cut#setDisplayId(String)},</li>
	 * 		<li>{@link Cut#setVersion(String)},</li>
	 * 		<li>{@link Cut#setWasDerivedFrom(URI)}, or</li>
	 * 		<li>{@link Identified#setAnnotations(List)}.</li>
	 * 	</ul>
	 * </li>
	 * </ul>
	 */
	private static Cut parseCut(NestedDocument<QName> typeCut) throws SBOLValidationException
	{
		String displayId 	   = null;//URIcompliance.extractDisplayId(typeCut.getIdentity());
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;//URI.create(URIcompliance.extractPersistentId(typeCut.getIdentity()));
		Integer at 			   = null;
		URI orientation 	   = null;
		URI sequence	 	   = null;
		String version 		   = null;
		Set<URI> wasDerivedFroms = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		List<Annotation> annotations = new ArrayList<>();

		for (NamedProperty<QName> namedProperty : typeCut.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", typeCut.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", typeCut.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", typeCut.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213",typeCut.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Cut.at))
			{
				if (!(namedProperty.getValue() instanceof Literal) || at != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-11202",typeCut.getIdentity());
				}
				String temp = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
				//at 			= Integer.parseInt(temp);
				try{
					at = Integer.parseInt(temp);
				}
				catch (NumberFormatException e) {
					throw new SBOLValidationException("sbol-11202",typeCut.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Cut.orientation))
			{
				if (!(namedProperty.getValue() instanceof Literal) || orientation != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-11002", typeCut.getIdentity());
				}
				orientation = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Location.sequence))
			{
				if (!(namedProperty.getValue() instanceof Literal) || orientation != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-11003", typeCut.getIdentity());
				}
				sequence = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", typeCut.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208", typeCut.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",typeCut.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.Cut.Cut.getNamespaceURI()+Sbol2Terms.Cut.Cut.getLocalPart())) {
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		if (at == null)
		{
			throw new SBOLValidationException("Cut requires at property.");
		}

		Cut c = new Cut(typeCut.getIdentity(), at);
		if (persistentIdentity != null)
			c.setPersistentIdentity(persistentIdentity);
		if (displayId != null)
			c.setDisplayId(displayId);
		if (name != null)
			c.setName(name);
		if (description != null)
			c.setDescription(description);
		if (orientation != null)
			try {
				c.setOrientation(OrientationType.convertToOrientationType(orientation));
			} catch (SBOLValidationException e) {
				throw new SBOLValidationException("sbol-11002",c);
			}
		if (sequence != null) {
			c.setSequence(sequence);
		}
		if(version != null)
			c.setVersion(version);
		c.setWasDerivedFroms(wasDerivedFroms);
		c.setWasGeneratedBys(wasGeneratedBys);
		if (!annotations.isEmpty())
			c.setAnnotations(annotations);

		return c;
	}

	/**
	 * @param typeRange
	 * @return
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>any of the following SBOL validation rules was violated:
	 * 10201, 10203, 10204, 10206, 10208, 10212, 10213, 11002, 11102, 11103; or
	 *</li>
	 * <li>an SBOL validation rule violation occurred in the following constructor or methods:
	 * 	<ul>
	 * 		<li>{@link Range#Range(URI, int, int)},</li>
	 * 		<li>{@link Range#setDisplayId(String)},</li>
	 * 		<li>{@link Range#setVersion(String)},</li>
	 * 		<li>{@link Range#setWasDerivedFrom(URI)}, or</li>
	 * 		<li>{@link Identified#setAnnotations(List)}.</li>
	 * 	</ul>
	 * </li>
	 * </ul>
	 */
	private static Location parseRange(NestedDocument<QName> typeRange) throws SBOLValidationException
	{
		String displayId 	   = null;//URIcompliance.extractDisplayId(typeRange.getIdentity());
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;//URI.create(URIcompliance.extractPersistentId(typeRange.getIdentity()));
		Integer start 		   = null;
		Integer end 		   = null;
		URI orientation 	   = null;
		URI sequence	 	   = null;
		String version 		   = null;
		Set<URI> wasDerivedFroms = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		List<Annotation> annotations = new ArrayList<>();

		for (NamedProperty<QName> namedProperty : typeRange.getProperties())
		{
			String temp;
			if (namedProperty.getName().equals(Sbol2Terms.Range.start))
			{
				if (!(namedProperty.getValue() instanceof Literal) || start != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-11102",typeRange.getIdentity());
				}
				temp  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
				//start = Integer.parseInt(temp);
				try{
					start = Integer.parseInt(temp);
				}
				catch (NumberFormatException e) {
					throw new SBOLValidationException("sbol-11102",typeRange.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", typeRange.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", typeRange.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", typeRange.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213",typeRange.getIdentity());
				}

				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Range.end))
			{
				if (!(namedProperty.getValue() instanceof Literal) || end != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-11103",typeRange.getIdentity());
				}
				temp = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
				//end  = Integer.parseInt(temp);
				try{
					end = Integer.parseInt(temp);
				}
				catch (NumberFormatException e) {
					throw new SBOLValidationException("sbol-11103",typeRange.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Range.orientation))
			{
				if (!(namedProperty.getValue() instanceof Literal) || orientation != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-11002", typeRange.getIdentity());
				}
				orientation = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Location.sequence))
			{
				if (!(namedProperty.getValue() instanceof Literal) || orientation != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-11003", typeRange.getIdentity());
				}
				sequence = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", typeRange.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208",typeRange.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",typeRange.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.Range.Range.getNamespaceURI()+Sbol2Terms.Range.Range.getLocalPart())) {
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		Location r = new Range(typeRange.getIdentity(), start, end);
		if (displayId != null)
			r.setDisplayId(displayId);
		if (name != null)
			r.setName(name);
		if (description != null)
			r.setDescription(description);
		if (persistentIdentity != null)
			r.setPersistentIdentity(persistentIdentity);
		if (orientation != null)
			try {
				r.setOrientation(OrientationType.convertToOrientationType(orientation));
			} catch (SBOLValidationException e) {
				throw new SBOLValidationException("sbol-11002",r);
			}
		if (sequence != null) {
			r.setSequence(sequence);
		}
		if(version != null)
			r.setVersion(version);
		r.setWasDerivedFroms(wasDerivedFroms);
		r.setWasGeneratedBys(wasGeneratedBys);
		if (!annotations.isEmpty())
			r.setAnnotations(annotations);
		return r;
	}

	/**
	 * @param component
	 * @param nested
	 * @return
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>any of the following SBOL validation rules was violated: 
	 * 10203, 10204, 10206, 10208, 10212, 10213, 10519, 10602, 10607, 10608; or 
	 *</li>
	 * <li>an SBOL validation rule violation occurred in the following constructor or methods:
	 * 	<ul>
	 * 		<li>{@link #parseMapsTo(NestedDocument, boolean)},</li>
	 * 		<li>{@link Component#Component(URI, AccessType, URI)},</li>
	 * 		<li>{@link Component#setVersion(String)},</li>
	 * 		<li>{@link Component#setDisplayId(String)},</li>
	 * 		<li>{@link Component#setAccess(AccessType)},</li>
	 * 		<li>{@link Component#setMapsTos(Set)},</li>
	 * 		<li>{@link Component#setDefinition(URI)},</li>
	 * 		<li>{@link Component#setWasDerivedFrom(URI)}, or</li>
	 * 		<li>{@link Identified#setAnnotations(List)}.</li>	
	 * 	</ul>
	 * </li>
	 * </ul>
	 */
	@SuppressWarnings("unchecked")
	private static Component parseComponent(SBOLDocument SBOLDoc, NestedDocument<QName> component, 
			Map<URI, NestedDocument<QName>> nested) throws SBOLValidationException
	{
		String displayId 	   = null;//URIcompliance.extractDisplayId(component.getIdentity());
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;//URI.create(URIcompliance.extractPersistentId(component.getIdentity()));
		String version 		   = null;
		URI subComponentURI    = null;
		AccessType access 	   = null;
		Location location 	   = null;
		Set<URI> wasDerivedFroms = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		Set<URI> roles 	  	   = new HashSet<>();
		URI roleIntegration = null;
		List<Annotation> annotations = new ArrayList<>();
		Set<MapsTo> mapsTo 		 = new HashSet<>();
		Set<Location> locations = new HashSet<>();
		Set<Location> sourceLocations = new HashSet<>();
		Set<Measure> measures = new HashSet<>();

		for (NamedProperty<QName> namedProperty : component.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", component.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", component.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", component.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Component.roles))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10702", component.getIdentity());
				}
				roles.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Component.roleIntegration))
			{
				if (!(namedProperty.getValue() instanceof Literal) || roleIntegration != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10708", component.getIdentity());
				}
				roleIntegration = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.ComponentInstance.access))
			{
				if (!(namedProperty.getValue() instanceof Literal) || access != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10607", component.getIdentity());
				}
				String accessTypeStr = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
				if (accessTypeStr.startsWith("http://www.sbolstandard.org/")) {
					System.err.println("Warning: namespace for access types should be http://sbols.org/v2#");
					accessTypeStr = accessTypeStr.replace("http://www.sbolstandard.org/", "http://sbols.org/v2#");
				}
				try {
					access = AccessType.convertToAccessType(URI.create(accessTypeStr));
				}
				catch (SBOLValidationException e) {
					throw new SBOLValidationException("sbol-10607",component.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Module.hasMapsTo))
			{
				if (namedProperty.getValue() instanceof NestedDocument) {
					NestedDocument<QName> nestedDocument = ((NestedDocument<QName>) namedProperty.getValue());
					if (nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.MapsTo.MapsTo)) {
						throw new SBOLValidationException("sbol-10606",component.getIdentity());
					}
					mapsTo.add(parseMapsTo(((NestedDocument<QName>) namedProperty.getValue()),false));
				}
				else {
					URI uri = (URI) ((Literal<QName>)namedProperty.getValue()).getValue();
					NestedDocument<QName> nestedDocument = nested.get(uri);
					if (nestedDocument==null || nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.MapsTo.MapsTo)) {
						throw new SBOLValidationException("sbol-10606",component.getIdentity());
					}
					mapsTo.add(parseMapsTo(nested.get(uri),false));
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.ComponentInstance.hasComponentDefinition))
			{
				if (subComponentURI != null) {
					throw new SBOLValidationException("sbol-10602", component.getIdentity());
				}
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-10602", component.getIdentity());
					}
					subComponentURI = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
				}
				else if (namedProperty.getValue() instanceof IdentifiableDocument) {
					if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.ComponentDefinition.ComponentDefinition)) {
						ComponentDefinition componentDefinition = parseComponentDefinition(SBOLDoc,
								(IdentifiableDocument<QName>)namedProperty.getValue(),nested);
						subComponentURI = componentDefinition.getIdentity();
					} else {
						throw new SBOLValidationException("sbol-10602", component.getIdentity());
					}
				}
				else {
					throw new SBOLValidationException("sbol-10602", component.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Location.Location))
			{
				if (namedProperty.getValue() instanceof NestedDocument) {
					NestedDocument<QName> nestedDocument = ((NestedDocument<QName>) namedProperty.getValue());
					if (nestedDocument.getType()==null || 
							!(nestedDocument.getType().equals(Sbol2Terms.Range.Range) ||
									nestedDocument.getType().equals(Sbol2Terms.Cut.Cut) ||
									nestedDocument.getType().equals(Sbol2Terms.GenericLocation.GenericLocation))) {
						// TODO: no rule for this yet
						throw new SBOLValidationException("sbol-10608",component.getIdentity());
					}
					location = parseLocation((NestedDocument<QName>) namedProperty.getValue());
				}
				else {
					URI uri = (URI) ((Literal<QName>)namedProperty.getValue()).getValue();
					NestedDocument<QName> nestedDocument = nested.get(uri);
					if (nestedDocument==null || nestedDocument.getType()==null || 
							!(nestedDocument.getType().equals(Sbol2Terms.Range.Range) ||
									nestedDocument.getType().equals(Sbol2Terms.Cut.Cut) ||
									nestedDocument.getType().equals(Sbol2Terms.GenericLocation.GenericLocation))) {
						// TODO: no rule for this yet
						throw new SBOLValidationException("sbol-10608",component.getIdentity());
					}
					location = parseLocation(nested.get(uri));
				}
				locations.add(location);
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Component.sourceLocation))
			{
				if (namedProperty.getValue() instanceof NestedDocument) {
					NestedDocument<QName> nestedDocument = ((NestedDocument<QName>) namedProperty.getValue());
					if (nestedDocument.getType()==null || 
							!(nestedDocument.getType().equals(Sbol2Terms.Range.Range) ||
									nestedDocument.getType().equals(Sbol2Terms.Cut.Cut) ||
									nestedDocument.getType().equals(Sbol2Terms.GenericLocation.GenericLocation))) {
						throw new SBOLValidationException("sbol-10710",component.getIdentity());
					}
					location = parseLocation((NestedDocument<QName>) namedProperty.getValue());
				}
				else {
					URI uri = (URI) ((Literal<QName>)namedProperty.getValue()).getValue();
					NestedDocument<QName> nestedDocument = nested.get(uri);
					if (nestedDocument==null || nestedDocument.getType()==null || 
							!(nestedDocument.getType().equals(Sbol2Terms.Range.Range) ||
									nestedDocument.getType().equals(Sbol2Terms.Cut.Cut) ||
									nestedDocument.getType().equals(Sbol2Terms.GenericLocation.GenericLocation))) {
						throw new SBOLValidationException("sbol-10710",component.getIdentity());
					}
					location = parseLocation(nested.get(uri));
				}
				sourceLocations.add(location);
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Measured.hasMeasure))
			{
				if (namedProperty.getValue() instanceof NestedDocument) {
					NestedDocument<QName> nestedDocument = ((NestedDocument<QName>) namedProperty.getValue());
					if (nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.Measure.Measure)) {
						throw new SBOLValidationException("sbol-10608",component.getIdentity());
					}
					measures.add(parseMeasure(((NestedDocument<QName>) namedProperty.getValue())));
				}
				else {
					URI uri = (URI) ((Literal<QName>)namedProperty.getValue()).getValue();
					NestedDocument<QName> nestedDocument = nested.get(uri);
					if (nestedDocument==null || nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.Measure.Measure)) {
						throw new SBOLValidationException("sbol-10608",component.getIdentity());
					}
					measures.add(parseMeasure(nested.get(uri)));
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", component.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213",component.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208", component.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",component.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.Component.Component.getNamespaceURI()+Sbol2Terms.Component.Component.getLocalPart())) {
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		Component c = new Component(component.getIdentity(), access, subComponentURI);
		if (persistentIdentity != null)
			c.setPersistentIdentity(persistentIdentity);
		if(version != null)
			c.setVersion(version);
		if (displayId != null)
			c.setDisplayId(displayId);
		if (access != null)
			c.setAccess(access);
		if (roleIntegration != null)
			try {
				c.setRoleIntegration(RoleIntegrationType.convertToRoleIntegrationType(roleIntegration));
			} catch (SBOLValidationException e) {
				throw new SBOLValidationException("sbol-10708",c);
			}
		if (!roles.isEmpty())
			c.setRoles(roles);
		if (!mapsTo.isEmpty())
			c.setMapsTos(mapsTo);
		if (subComponentURI != null)
			c.setDefinition(subComponentURI);
		if (name != null)
			c.setName(name);
		if (description != null)
			c.setDescription(description);
		c.setWasDerivedFroms(wasDerivedFroms);
		c.setWasGeneratedBys(wasGeneratedBys);
		if (!locations.isEmpty())
			c.setLocations(locations);
		if (!sourceLocations.isEmpty())
			c.setSourceLocations(sourceLocations);
		if (!annotations.isEmpty())
			c.setAnnotations(annotations);
		if (!measures.isEmpty()) 
			c.setMeasures(measures);

		return c;
	}

	/**
	 * @param SBOLDoc
	 * @param topLevel
	 * @return
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>any of the following SBOL validation rules was violated: 
	 * 10202, 10203, 10204, 10206, 10208, 10212, 10213, 12102; or</li>
	 * <li>an SBOL validation rule violation occurred in the following constructor or methods:
	 * 	<ul>
	 * 		<li>{@link GenericTopLevel#GenericTopLevel(URI, QName)}, </li>
	 * 		<li>{@link GenericTopLevel#setDisplayId(String)}, </li>
	 * 		<li>{@link GenericTopLevel#setVersion(String)}, </li>
	 * 		<li>{@link GenericTopLevel#setWasDerivedFrom(URI)}, </li>
	 * 		<li>{@link Identified#setAnnotations(List)}, or</li>
	 * 		<li>{@link SBOLDocument#addGenericTopLevel(GenericTopLevel)}.</li>
	 * 	</ul>
	 * </li>
	 * </ul>
	 */
	@SuppressWarnings("unchecked")
	private static GenericTopLevel parseGenericTopLevel(SBOLDocument SBOLDoc,
			IdentifiableDocument<QName> topLevel) throws SBOLValidationException
	{
		String displayId 	   = null;//URIcompliance.extractDisplayId(topLevel.getIdentity());
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;//URI.create(URIcompliance.extractPersistentId(topLevel.getIdentity()));
		String version 		   = null;
		Set<URI> wasDerivedFroms = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		Set<URI> attachments = new HashSet<>();
		QName type 			   = topLevel.getType();

		List<Annotation> annotations = new ArrayList<>();
		
		int sbol1TypeCount=0;
		for (NamedProperty<QName> namedProperty : topLevel.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Description.type)) {
				String typeStr = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
				String nameSpace = URIcompliance.extractNamespace(URI.create(typeStr));
				if (nameSpace.equals(Sbol1Terms.sbol1.getNamespaceURI())) {
					sbol1TypeCount++;
				}	
			}
		}
		
		for (NamedProperty<QName> namedProperty : topLevel.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Description.type)) {
				String typeStr = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
				String nameSpace = URIcompliance.extractNamespace(URI.create(typeStr));
				String localPart = URIcompliance.extractDisplayId(URI.create(typeStr));
				String prefix = null;
				if (nameSpace == null) {
					if (typeStr.lastIndexOf('/') > typeStr.lastIndexOf('#')) {
						if (typeStr.lastIndexOf('/') > typeStr.lastIndexOf(':')) {
							nameSpace = typeStr.substring(0, typeStr.lastIndexOf('/')+1);
							localPart = typeStr.substring(typeStr.lastIndexOf('/')+1);
						} else {
							nameSpace = typeStr.substring(0, typeStr.lastIndexOf(':')+1);
							localPart = typeStr.substring(typeStr.lastIndexOf(':')+1);
						}
					} else if (typeStr.lastIndexOf('#') > typeStr.lastIndexOf(':')) {
						nameSpace = typeStr.substring(0, typeStr.lastIndexOf('#')+1);
						localPart = typeStr.substring(typeStr.lastIndexOf('#')+1);
					} else {
						nameSpace = typeStr.substring(0, typeStr.lastIndexOf(':')+1);
						localPart = typeStr.substring(typeStr.lastIndexOf(':')+1);
					}
					prefix = SBOLDoc.getNamespacePrefix(URI.create(nameSpace));
					if (prefix == null) {
						prefix = "ns0";
						int prefixCnt = 0;
						while (SBOLDoc.getNamespace(prefix) != null) {
							prefixCnt++;
							prefix = "ns" + prefixCnt;
						}
						SBOLDoc.addNamespace(new QName(nameSpace,localPart,prefix));
					}
				} else {
					prefix = SBOLDoc.getNamespacePrefix(URI.create(nameSpace));
				}
				if (!nameSpace.equals(Sbol2Terms.sbol2.getNamespaceURI()))
					type = new QName(nameSpace,localPart,prefix);
				if (sbol1TypeCount==1) {
					if (nameSpace.equals(Sbol1Terms.sbol1.getNamespaceURI())) {
						type = new QName(nameSpace,localPart,prefix);
					} else {
						annotations.add(new Annotation(namedProperty));
					}
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", topLevel.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", topLevel.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", topLevel.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", topLevel.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213",topLevel.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208", topLevel.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",topLevel.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.TopLevel.hasAttachment))
			{
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
					attachments.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
				}
				else if (namedProperty.getValue() instanceof IdentifiableDocument) {
					if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.Attachment.Attachment)) {
						Attachment attachment = parseAttachment(SBOLDoc,(IdentifiableDocument<QName>)namedProperty.getValue());
						attachments.add(attachment.getIdentity());
					} else {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
				}
				else {
					throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
				}
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		//		GenericTopLevel t = SBOLDoc.createGenericTopLevel(topLevel.getIdentity(), topLevel.getType());
		GenericTopLevel t = new GenericTopLevel(topLevel.getIdentity(), type);
		if (persistentIdentity != null)
			t.setPersistentIdentity(persistentIdentity);
		if (version != null)
			t.setVersion(version);
		if (displayId != null)
			t.setDisplayId(displayId);
		if (name != null)
			t.setName(name);
		if (description != null)
			t.setDescription(description);
		t.setWasDerivedFroms(wasDerivedFroms);
		t.setWasGeneratedBys(wasGeneratedBys);
		t.setAttachments(attachments);
		if (!annotations.isEmpty())
			t.setAnnotations(annotations);

		GenericTopLevel oldG = SBOLDoc.getGenericTopLevel(topLevel.getIdentity());
		if (oldG == null) {
			SBOLDoc.addGenericTopLevel(t);
		} else {
			if (!t.equals(oldG)) {
				throw new SBOLValidationException("sbol-10202",t);
			}
		}
		return t;
	}
	
	@SuppressWarnings("unchecked")
	private static Activity parseActivity(SBOLDocument SBOLDoc,IdentifiableDocument<QName> topLevel,
			Map<URI, NestedDocument<QName>> nested) throws SBOLValidationException
	{
		String displayId 	   = null;
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;
		String version 		   = null;
		Set<URI> wasDerivedFroms = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		Set<URI> attachments = new HashSet<>();
		Set<URI> type = new HashSet<>();
		DateTime startedAtTime	= null;
		DateTime endedAtTime = null;
		Set<URI> wasInformedBys = new HashSet<>();
		Set<Association> qualifiedAssociations = new HashSet<>();
		Set<Usage> qualifiedUsages = new HashSet<>();
		List<Annotation> annotations = new ArrayList<>();

		for (NamedProperty<QName> namedProperty : topLevel.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", topLevel.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", topLevel.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", topLevel.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", topLevel.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213",topLevel.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) || 
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208", topLevel.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",topLevel.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Activity.type))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-12412", topLevel.getIdentity());
				}
				type.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.TopLevel.hasAttachment))
			{
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
					attachments.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
				}
				else if (namedProperty.getValue() instanceof IdentifiableDocument) {
					if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.Attachment.Attachment)) {
						Attachment attachment = parseAttachment(SBOLDoc,(IdentifiableDocument<QName>)namedProperty.getValue());
						attachments.add(attachment.getIdentity());
					} else {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
				}
				else {
					throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Activity.startedAtTime))
			{
				if (!(namedProperty.getValue() instanceof Literal) || startedAtTime != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-12402", topLevel.getIdentity());
				}
				DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
				try {
					startedAtTime = fmt.parseDateTime(((Literal<QName>) namedProperty.getValue()).getValue().toString());
				} catch (IllegalArgumentException e) {
					throw new SBOLValidationException("sbol-12402", topLevel.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Activity.endedAtTime))
			{
				if (!(namedProperty.getValue() instanceof Literal) || endedAtTime != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-12403", topLevel.getIdentity());
				}
				DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
				try {
					endedAtTime = fmt.parseDateTime(((Literal<QName>) namedProperty.getValue()).getValue().toString());
				} catch (IllegalArgumentException e) {
					throw new SBOLValidationException("sbol-12403", topLevel.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Activity.wasInformedBy))
			{
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-12406", topLevel.getIdentity());
					}
					wasInformedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
				}
				else if (namedProperty.getValue() instanceof IdentifiableDocument) {
					if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.Activity.Activity)) {
						Activity activity = parseActivity(SBOLDoc,(IdentifiableDocument<QName>)namedProperty.getValue(),nested);
						wasInformedBys.add(activity.getIdentity());
					} else {
						throw new SBOLValidationException("sbol-12406", topLevel.getIdentity());
					}
				}
				else {
					throw new SBOLValidationException("sbol-12406", topLevel.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Activity.qualifiedAssociation))
			{
				if (namedProperty.getValue() instanceof NestedDocument) {
					NestedDocument<QName> nestedDocument = ((NestedDocument<QName>) namedProperty.getValue());
					if (nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.Association.Association)) {
						throw new SBOLValidationException("sbol-12404",topLevel.getIdentity());
					}
					qualifiedAssociations.add(parseAssociation(SBOLDoc,((NestedDocument<QName>) namedProperty.getValue()), nested));
				}
				else {
					URI uri = (URI) ((Literal<QName>)namedProperty.getValue()).getValue();
					NestedDocument<QName> nestedDocument = nested.get(uri);
					if (nestedDocument==null || nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.Association.Association)) {
						throw new SBOLValidationException("sbol-12404",topLevel.getIdentity());
					}
					qualifiedAssociations.add(parseAssociation(SBOLDoc, nestedDocument, nested));
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Activity.qualifiedUsage))
			{
				if (namedProperty.getValue() instanceof NestedDocument) {
					NestedDocument<QName> nestedDocument = ((NestedDocument<QName>) namedProperty.getValue());
					if (nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.Usage.Usage)) {
						throw new SBOLValidationException("sbol-12405",topLevel.getIdentity());
					}
					qualifiedUsages.add(parseUsage(SBOLDoc,((NestedDocument<QName>) namedProperty.getValue()), nested));
				}
				else {
					URI uri = (URI) ((Literal<QName>)namedProperty.getValue()).getValue();
					NestedDocument<QName> nestedDocument = nested.get(uri);
					if (nestedDocument==null || nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.Usage.Usage)) {
						throw new SBOLValidationException("sbol-12405",topLevel.getIdentity());
					}
					qualifiedUsages.add(parseUsage(SBOLDoc, nestedDocument, nested));
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.Activity.Activity.getNamespaceURI()+Sbol2Terms.Activity.Activity.getLocalPart())) {
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		//		GenericTopLevel t = SBOLDoc.createGenericTopLevel(topLevel.getIdentity(), topLevel.getType());
		Activity t = new Activity(topLevel.getIdentity());
		if (persistentIdentity != null)
			t.setPersistentIdentity(persistentIdentity);
		if (version != null)
			t.setVersion(version);
		if (displayId != null)
			t.setDisplayId(displayId);
		if (name != null)
			t.setName(name);
		if (description != null)
			t.setDescription(description);
		if (!type.isEmpty()) {
			t.setTypes(type);
		}
		t.setWasDerivedFroms(wasDerivedFroms);
		t.setWasGeneratedBys(wasGeneratedBys);
		if (!annotations.isEmpty())
			t.setAnnotations(annotations);
		if (startedAtTime != null) 
			t.setStartedAtTime(startedAtTime);
		if (endedAtTime != null) 
			t.setEndedAtTime(endedAtTime);
		if (!qualifiedAssociations.isEmpty())
			t.setAssociations(qualifiedAssociations);
		if (!qualifiedUsages.isEmpty())
			t.setUsages(qualifiedUsages);
		if (!wasInformedBys.isEmpty())
			t.setWasInformedBys(wasInformedBys);
		if (!attachments.isEmpty())
			t.setAttachments(attachments);

		Activity oldA = SBOLDoc.getActivity(topLevel.getIdentity());
		if (oldA == null) {
			SBOLDoc.addActivity(t);
		} else {
			if (!t.equals(oldA)) {
				throw new SBOLValidationException("sbol-10202",t);
			}
		}
		return t;
	}
	
	private static Association parseAssociation(SBOLDocument SBOLDoc, NestedDocument<QName> association, 
			Map<URI, NestedDocument<QName>> nested) throws SBOLValidationException
	{
		String displayId 	   = null;
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;
		String version 		   = null;
		Set<URI> roles		   = new HashSet<>();
		URI planURI			   = null;
		URI agentURI		   = null;
		Set<URI> wasDerivedFroms = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		List<Annotation> annotations = new ArrayList<>();

		for (NamedProperty<QName> namedProperty : association.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", association.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", association.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", association.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", association.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213",association.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208", association.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",association.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Association.role))
			{
				if (!(namedProperty.getValue() instanceof Literal) || 
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-12602", association.getIdentity());
				}
				roles.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Association.agent))
			{
				if (!(namedProperty.getValue() instanceof Literal) || agentURI != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-12605", association.getIdentity());
				}
				agentURI = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Association.plan))
			{
				if (!(namedProperty.getValue() instanceof Literal) || planURI != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-12603", association.getIdentity());
				}
				planURI = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.Association.Association.getNamespaceURI()+Sbol2Terms.Association.Association.getLocalPart())) {
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		Association a = new Association(association.getIdentity(), agentURI);
		if (persistentIdentity != null)
			a.setPersistentIdentity(persistentIdentity);
		if(version != null)
			a.setVersion(version);
		if (displayId != null)
			a.setDisplayId(displayId);
		if (name != null)
			a.setName(name);
		if (description != null)
			a.setDescription(description);
		a.setWasDerivedFroms(wasDerivedFroms);
		a.setWasGeneratedBys(wasGeneratedBys);
		if (!annotations.isEmpty())
			a.setAnnotations(annotations);
		if (!roles.isEmpty()) 
			a.setRoles(roles);
		if (planURI != null) 
			a.setPlan(planURI);

		return a;
	}

	private static Usage parseUsage(SBOLDocument SBOLDoc, NestedDocument<QName> usage, 
			Map<URI, NestedDocument<QName>> nested) throws SBOLValidationException
	{
		String displayId 	   = null;
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;
		String version 		   = null;
		URI entityURI		   = null;
		Set<URI> roles		   = new HashSet<>();
		Set<URI> wasDerivedFroms = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		List<Annotation> annotations = new ArrayList<>();

		for (NamedProperty<QName> namedProperty : usage.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", usage.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", usage.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", usage.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", usage.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213",usage.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208", usage.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",usage.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Usage.role))
			{
				if (!(namedProperty.getValue() instanceof Literal) || 
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-12503", usage.getIdentity());
				}
				roles.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Usage.entity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || entityURI != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-12502", usage.getIdentity());
				}
				entityURI = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.Usage.Usage.getNamespaceURI()+Sbol2Terms.Usage.Usage.getLocalPart())) {
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		Usage u = new Usage(usage.getIdentity(), entityURI);
		if (persistentIdentity != null)
			u.setPersistentIdentity(persistentIdentity);
		if(version != null)
			u.setVersion(version);
		if (displayId != null)
			u.setDisplayId(displayId);
		if (name != null)
			u.setName(name);
		if (description != null)
			u.setDescription(description);
		u.setWasDerivedFroms(wasDerivedFroms);
		u.setWasGeneratedBys(wasGeneratedBys);
		if (!annotations.isEmpty())
			u.setAnnotations(annotations);
		if (!roles.isEmpty()) 
			u.setRoles(roles);

		return u;
	}

	@SuppressWarnings("unchecked")
	private static Agent parseAgent(SBOLDocument SBOLDoc,IdentifiableDocument<QName> topLevel) throws SBOLValidationException
	{
		String displayId 	   = null;
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;
		String version 		   = null;
		Set<URI> wasDerivedFroms = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		Set<URI> attachments = new HashSet<>();
		List<Annotation> annotations = new ArrayList<>();

		for (NamedProperty<QName> namedProperty : topLevel.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", topLevel.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", topLevel.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", topLevel.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", topLevel.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213",topLevel.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208", topLevel.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",topLevel.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.TopLevel.hasAttachment))
			{
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
					attachments.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
				}
				else if (namedProperty.getValue() instanceof IdentifiableDocument) {
					if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.Attachment.Attachment)) {
						Attachment attachment = parseAttachment(SBOLDoc,(IdentifiableDocument<QName>)namedProperty.getValue());
						attachments.add(attachment.getIdentity());
					} else {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
				}
				else {
					throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.Agent.Agent.getNamespaceURI()+Sbol2Terms.Agent.Agent.getLocalPart())) {
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		Agent t = new Agent(topLevel.getIdentity());
		if (persistentIdentity != null)
			t.setPersistentIdentity(persistentIdentity);
		if (version != null)
			t.setVersion(version);
		if (displayId != null)
			t.setDisplayId(displayId);
		if (name != null)
			t.setName(name);
		if (description != null)
			t.setDescription(description);
		t.setWasDerivedFroms(wasDerivedFroms);
		t.setWasGeneratedBys(wasGeneratedBys);
		t.setAttachments(attachments);
		if (!annotations.isEmpty())
			t.setAnnotations(annotations);

		Agent oldA = SBOLDoc.getAgent(topLevel.getIdentity());
		if (oldA == null) {
			SBOLDoc.addAgent(t);
		} else {
			if (!t.equals(oldA)) {
				throw new SBOLValidationException("sbol-10202",t);
			}
		}
		return t;
	}
	
	@SuppressWarnings("unchecked")
	private static Plan parsePlan(SBOLDocument SBOLDoc,IdentifiableDocument<QName> topLevel) throws SBOLValidationException
	{
		String displayId 	   = null;
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;
		String version 		   = null;
		Set<URI> wasDerivedFroms = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		Set<URI> attachments = new HashSet<>();
		List<Annotation> annotations = new ArrayList<>();

		for (NamedProperty<QName> namedProperty : topLevel.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", topLevel.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", topLevel.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", topLevel.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", topLevel.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213",topLevel.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208", topLevel.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",topLevel.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.TopLevel.hasAttachment))
			{
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
					attachments.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
				}
				else if (namedProperty.getValue() instanceof IdentifiableDocument) {
					if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.Attachment.Attachment)) {
						Attachment attachment = parseAttachment(SBOLDoc,(IdentifiableDocument<QName>)namedProperty.getValue());
						attachments.add(attachment.getIdentity());
					} else {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
				}
				else {
					throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.Plan.Plan.getNamespaceURI()+Sbol2Terms.Plan.Plan.getLocalPart())) {
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		Plan t = new Plan(topLevel.getIdentity());
		if (persistentIdentity != null)
			t.setPersistentIdentity(persistentIdentity);
		if (version != null)
			t.setVersion(version);
		if (displayId != null)
			t.setDisplayId(displayId);
		if (name != null)
			t.setName(name);
		if (description != null)
			t.setDescription(description);
		t.setWasDerivedFroms(wasDerivedFroms);
		t.setWasGeneratedBys(wasGeneratedBys);
		t.setAttachments(attachments);
		if (!annotations.isEmpty())
			t.setAnnotations(annotations);

		Plan oldA = SBOLDoc.getPlan(topLevel.getIdentity());
		if (oldA == null) {
			SBOLDoc.addPlan(t);
		} else {
			if (!t.equals(oldA)) {
				throw new SBOLValidationException("sbol-10202",t);
			}
		}
		return t;
	}

	/**
	 * @param SBOLDoc
	 * @param topLevel
	 * @return
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>any of the following SBOL validation rules was violated: 
	 * 10202, 10203, 10204, 10206, 10208, 10212, 10213, 10502, 10504, 10508; or</li>
	 * <li>an SBOL validation rule violation occurred in the following constructor or methods:
	 * 	<ul>
	 * 		<li>{@link Model#Model(URI, URI, URI, URI)}, </li>
	 * 		<li>{@link Model#setDisplayId(String)}, </li>
	 * 		<li>{@link Model#setVersion(String)}, </li>
	 * 		<li>{@link Model#setWasDerivedFrom(URI)}, </li>
	 * 		<li>{@link Identified#setAnnotations(List)}, or</li>
	 * 		<li>{@link SBOLDocument#addModel(Model)}.</li>
	 * 	</ul>
	 * </li>
	 * </ul>
	 */
	@SuppressWarnings("unchecked")
	private static Model parseModel(SBOLDocument SBOLDoc, IdentifiableDocument<QName> topLevel) throws SBOLValidationException
	{
		String displayId 	   = null;//URIcompliance.extractDisplayId(topLevel.getIdentity());
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;//URI.create(URIcompliance.extractPersistentId(topLevel.getIdentity()));
		String version 		   = null;
		URI source 			   = null;
		URI language 		   = null;
		URI framework 	 	   = null;
		Set<URI> wasDerivedFroms = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		Set<URI> attachments = new HashSet<>();

		List<Annotation> annotations = new ArrayList<>();

		for (NamedProperty<QName> namedProperty : topLevel.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", topLevel.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", topLevel.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", topLevel.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Model.source))
			{
				if (!(namedProperty.getValue() instanceof Literal) || source != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10502", topLevel.getIdentity());
				}
				source = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Model.language))
			{
				if (!(namedProperty.getValue() instanceof Literal) || language != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10504", topLevel.getIdentity());
				}
				language = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Model.framework))
			{
				if (!(namedProperty.getValue() instanceof Literal) || framework != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10508", topLevel.getIdentity());
				}
				framework = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", topLevel.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213",topLevel.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208", topLevel.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",topLevel.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.TopLevel.hasAttachment))
			{
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
					attachments.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
				}
				else if (namedProperty.getValue() instanceof IdentifiableDocument) {
					if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.Attachment.Attachment)) {
						Attachment attachment = parseAttachment(SBOLDoc,(IdentifiableDocument<QName>)namedProperty.getValue());
						attachments.add(attachment.getIdentity());
					} else {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
				}
				else {
					throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.Model.Model.getNamespaceURI()+Sbol2Terms.Model.Model.getLocalPart())) {
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		//		Model m = SBOLDoc.createModel(topLevel.getIdentity(), source, language, framework);
		Model m = new Model(topLevel.getIdentity(), source, language, framework);
		if (persistentIdentity != null)
			m.setPersistentIdentity(persistentIdentity);
		if (version != null)
			m.setVersion(version);
		if (displayId != null)
			m.setDisplayId(displayId);
		if (name != null)
			m.setName(name);
		if (description != null)
			m.setDescription(description);
		m.setWasDerivedFroms(wasDerivedFroms);
		m.setWasGeneratedBys(wasGeneratedBys);
		m.setAttachments(attachments);
		if (!annotations.isEmpty())
			m.setAnnotations(annotations);

		Model oldM = SBOLDoc.getModel(topLevel.getIdentity());
		if (oldM == null) {
			SBOLDoc.addModel(m);
		} else {
			if (!m.equals(oldM)) {
				throw new SBOLValidationException("sbol-10202",m);
			}
		}
		return m;
	}
	
	@SuppressWarnings("unchecked")
	private static Attachment parseAttachment(SBOLDocument SBOLDoc, IdentifiableDocument<QName> topLevel) throws SBOLValidationException
	{
		String displayId 	   = null;//URIcompliance.extractDisplayId(topLevel.getIdentity());
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;//URI.create(URIcompliance.extractPersistentId(topLevel.getIdentity()));
		String version 		   = null;
		URI source 			   = null;
		URI format	 		   = null;
		Long size	 	 	   = null;
		String hash	 	 	   = null;
		Set<URI> wasDerivedFroms = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		Set<URI> attachments = new HashSet<>();

		List<Annotation> annotations = new ArrayList<>();

		for (NamedProperty<QName> namedProperty : topLevel.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", topLevel.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", topLevel.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", topLevel.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Attachment.source))
			{
				if (!(namedProperty.getValue() instanceof Literal) || source != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-13202", topLevel.getIdentity());
				}
				source = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Attachment.format))
			{
				if (!(namedProperty.getValue() instanceof Literal) || format != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-13204", topLevel.getIdentity());
				}
				format = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Attachment.size))
			{
				if (!(namedProperty.getValue() instanceof Literal) || size != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-13207", topLevel.getIdentity());
				}
				size = Long.valueOf(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Attachment.hash))
			{
				if (!(namedProperty.getValue() instanceof Literal) || hash != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-13208", topLevel.getIdentity());
				}
				hash = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", topLevel.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213",topLevel.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208", topLevel.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",topLevel.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.TopLevel.hasAttachment))
			{
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
					attachments.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
				}
				else if (namedProperty.getValue() instanceof IdentifiableDocument) {
					if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.Attachment.Attachment)) {
						Attachment attachment = parseAttachment(SBOLDoc,(IdentifiableDocument<QName>)namedProperty.getValue());
						attachments.add(attachment.getIdentity());
					} else {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
				}
				else {
					throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.Attachment.Attachment.getNamespaceURI()+Sbol2Terms.Attachment.Attachment.getLocalPart())) {
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		Attachment a = new Attachment(topLevel.getIdentity(), source);
		if (persistentIdentity != null)
			a.setPersistentIdentity(persistentIdentity);
		if (version != null)
			a.setVersion(version);
		if (displayId != null)
			a.setDisplayId(displayId);
		if (name != null)
			a.setName(name);
		if (description != null)
			a.setDescription(description);
		a.setWasDerivedFroms(wasDerivedFroms);
		a.setWasGeneratedBys(wasGeneratedBys);
		a.setAttachments(attachments);
		if (!annotations.isEmpty())
			a.setAnnotations(annotations);
		if (format != null) 
			a.setFormat(format);
		if (size != null)
			a.setSize(size);
		if (hash != null)
			a.setHash(hash);

		Attachment oldA = SBOLDoc.getAttachment(topLevel.getIdentity());
		if (oldA == null) {
			SBOLDoc.addAttachment(a);
		} else {
			if (!a.equals(oldA)) {
				throw new SBOLValidationException("sbol-10202",a);
			}
		}
		return a;
	}

	/**
	 * @param SBOLDoc
	 * @param topLevel
	 * @return
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>any of the following SBOL validation rules was violated: 
	 * 10202, 10203, 10204, 10206, 10208, 10212, 10213, 12102; or</li>
	 * <li>an SBOL validation rule violation occurred in the following constructor or methods:
	 * 	<ul>
	 * 		<li>{@link Collection#Collection(URI)}, </li>
	 * 		<li>{@link Collection#setDisplayId(String)}, </li>
	 * 		<li>{@link Collection#setVersion(String)}, </li>
	 * 		<li>{@link Collection#setMembers(Set)}, </li>
	 * 		<li>{@link Collection#setWasDerivedFrom(URI)}, </li>
	 * 		<li>{@link Identified#setAnnotations(List)}, or</li>
	 * 		<li>{@link SBOLDocument#addCollection(Collection)}.</li>
	 * 	</ul>
	 * </li>
	 * </ul>
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Collection parseCollection(SBOLDocument SBOLDoc, IdentifiableDocument<QName> topLevel,
			Map<URI, NestedDocument<QName>> nested) throws SBOLValidationException
	{
		String displayId 	   = null;//URIcompliance.extractDisplayId(topLevel.getIdentity());
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;//URI.create(URIcompliance.extractPersistentId(topLevel.getIdentity()));
		String version 		   = null;
		Set<URI> wasDerivedFroms	 = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		Set<URI> attachments = new HashSet<>();
		Set<URI> members 			 = new HashSet<>();
		List<Annotation> annotations = new ArrayList<>();

		for (NamedProperty<QName> namedProperty : topLevel.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", topLevel.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", topLevel.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", topLevel.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Collection.hasMembers))
			{
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-12102", topLevel.getIdentity());
					}
					members.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
				} else if (namedProperty.getValue() instanceof NestedDocument) {
					if (((IdentifiableDocument)namedProperty).getType().equals(Sbol2Terms.Collection.Collection))
						parseCollection(SBOLDoc, (IdentifiableDocument)namedProperty, nested);
					else if (((IdentifiableDocument)namedProperty).equals(Sbol2Terms.ModuleDefinition.ModuleDefinition))
						parseModuleDefinition(SBOLDoc, (IdentifiableDocument)namedProperty, nested);
					else if (((IdentifiableDocument)namedProperty).equals(Sbol2Terms.Model.Model))
						parseModel(SBOLDoc, (IdentifiableDocument)namedProperty);
					else if (((IdentifiableDocument)namedProperty).equals(Sbol2Terms.Sequence.Sequence))
						parseSequence(SBOLDoc, (IdentifiableDocument)namedProperty);
					else if (((IdentifiableDocument)namedProperty).equals(Sbol2Terms.ComponentDefinition.ComponentDefinition))
						parseComponentDefinition(SBOLDoc, (IdentifiableDocument)namedProperty, nested);
					else if (((IdentifiableDocument)namedProperty).equals(Sbol2Terms.CombinatorialDerivation.CombinatorialDerivation))
						parseCombinatorialDerivation(SBOLDoc, (IdentifiableDocument)namedProperty, nested);
					else if (((IdentifiableDocument)namedProperty).equals(Sbol2Terms.Implementation.Implementation))
						parseImplementation(SBOLDoc, (IdentifiableDocument)namedProperty, nested);
					else if (((IdentifiableDocument)namedProperty).equals(Sbol2Terms.Attachment.Attachment))
						parseAttachment(SBOLDoc, (IdentifiableDocument)namedProperty);
					else if (((IdentifiableDocument)namedProperty).equals(Sbol2Terms.Activity.Activity))
						parseActivity(SBOLDoc, (IdentifiableDocument)namedProperty, nested);
					else if (((IdentifiableDocument)namedProperty).equals(Sbol2Terms.Agent.Agent))
						parseAgent(SBOLDoc, (IdentifiableDocument)namedProperty);
					else if (((IdentifiableDocument)namedProperty).equals(Sbol2Terms.Plan.Plan))
						parsePlan(SBOLDoc, (IdentifiableDocument)namedProperty);
					else
						parseGenericTopLevel(SBOLDoc, (IdentifiableDocument)namedProperty);
				} else {
					throw new SBOLValidationException("sbol-12102", topLevel.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", topLevel.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213",topLevel.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208", topLevel.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",topLevel.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.TopLevel.hasAttachment))
			{
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
					attachments.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
				}
				else if (namedProperty.getValue() instanceof IdentifiableDocument) {
					if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.Attachment.Attachment)) {
						Attachment attachment = parseAttachment(SBOLDoc,(IdentifiableDocument<QName>)namedProperty.getValue());
						attachments.add(attachment.getIdentity());
					} else {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
				}
				else {
					throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.Collection.Collection.getNamespaceURI()+Sbol2Terms.Collection.Collection.getLocalPart())) {
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		Collection c = new Collection(topLevel.getIdentity());
		if (displayId != null)
			c.setDisplayId(displayId);
		if (version != null)
			c.setVersion(version);
		if (persistentIdentity != null)
			c.setPersistentIdentity(persistentIdentity);
		if (!members.isEmpty())
			c.setMembers(members);
		if (name != null)
			c.setName(name);
		if (description != null)
			c.setDescription(description);
		c.setWasDerivedFroms(wasDerivedFroms);
		c.setWasGeneratedBys(wasGeneratedBys);
		c.setAttachments(attachments);
		if (!annotations.isEmpty())
			c.setAnnotations(annotations);

		Collection oldC = SBOLDoc.getCollection(topLevel.getIdentity());
		if (oldC == null) {
			SBOLDoc.addCollection(c);
		} else {
			if (!c.equals(oldC)) {
				throw new SBOLValidationException("sbol-10202",c);
			}
		}
		return c;
	}
	
	/**
	 * @param SBOLDoc
	 * @param topLevel
	 * @return
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>any of the following SBOL validation rules was violated: 
	 * 10202, 10203, 10204, 10206, 10208, 10212, 10213, 12102; or</li>
	 * <li>an SBOL validation rule violation occurred in the following constructor or methods:
	 * 	<ul>
	 * 		<li>{@link Experiment#Experiment(URI)}, </li>
	 * 		<li>{@link Experiment#setDisplayId(String)}, </li>
	 * 		<li>{@link Experiment#setVersion(String)}, </li>
	 * 		<li>{@link Experiment#setMembers(Set)}, </li>
	 * 		<li>{@link Experiment#setWasDerivedFrom(URI)}, </li>
	 * 		<li>{@link Identified#setAnnotations(List)}, or</li>
	 * 		<li>{@link SBOLDocument#addExperiment(Experiment)}.</li>
	 * 	</ul>
	 * </li>
	 * </ul>
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Experiment parseExperiment(SBOLDocument SBOLDoc, IdentifiableDocument<QName> topLevel,
			Map<URI, NestedDocument<QName>> nested) throws SBOLValidationException
	{
		String displayId 	   = null;//URIcompliance.extractDisplayId(topLevel.getIdentity());
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;//URI.create(URIcompliance.extractPersistentId(topLevel.getIdentity()));
		String version 		   = null;
		Set<URI> wasDerivedFroms	 = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		Set<URI> attachments = new HashSet<>();
		Set<URI> experimentalData = new HashSet<>();
		List<Annotation> annotations = new ArrayList<>();

		for (NamedProperty<QName> namedProperty : topLevel.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", topLevel.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", topLevel.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", topLevel.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Experiment.hasExperimentalData))
			{
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-13402", topLevel.getIdentity());
					}
					experimentalData.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
				} else if (namedProperty.getValue() instanceof NestedDocument) {
					if (((IdentifiableDocument)namedProperty).getType().equals(Sbol2Terms.ExperimentalData.ExperimentalData))
						parseExperimentalData(SBOLDoc, (IdentifiableDocument)namedProperty, nested);
				} else {
					throw new SBOLValidationException("sbol-13402", topLevel.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", topLevel.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213",topLevel.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208", topLevel.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",topLevel.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.TopLevel.hasAttachment))
			{
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
					attachments.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
				}
				else if (namedProperty.getValue() instanceof IdentifiableDocument) {
					if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.Attachment.Attachment)) {
						Attachment attachment = parseAttachment(SBOLDoc,(IdentifiableDocument<QName>)namedProperty.getValue());
						attachments.add(attachment.getIdentity());
					} else {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
				}
				else {
					throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.Experiment.Experiment.getNamespaceURI()+Sbol2Terms.Experiment.Experiment.getLocalPart())) {
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		Experiment c = new Experiment(topLevel.getIdentity());
		if (displayId != null)
			c.setDisplayId(displayId);
		if (version != null)
			c.setVersion(version);
		if (persistentIdentity != null)
			c.setPersistentIdentity(persistentIdentity);
		if (!experimentalData.isEmpty())
			c.setExperimentalData(experimentalData);
		if (name != null)
			c.setName(name);
		if (description != null)
			c.setDescription(description);
		c.setWasDerivedFroms(wasDerivedFroms);
		c.setWasGeneratedBys(wasGeneratedBys);
		c.setAttachments(attachments);
		if (!annotations.isEmpty())
			c.setAnnotations(annotations);

		Experiment oldC = SBOLDoc.getExperiment(topLevel.getIdentity());
		if (oldC == null) {
			SBOLDoc.addExperiment(c);
		} else {
			if (!c.equals(oldC)) {
				throw new SBOLValidationException("sbol-10202",c);
			}
		}
		return c;
	}
	
	/**
	 * @param SBOLDoc
	 * @param topLevel
	 * @return
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>any of the following SBOL validation rules was violated: 
	 * 10202, 10203, 10204, 10206, 10208, 10212, 10213, 12102; or</li>
	 * <li>an SBOL validation rule violation occurred in the following constructor or methods:
	 * 	<ul>
	 * 		<li>{@link ExperimentalData#ExperimentalData(URI)}, </li>
	 * 		<li>{@link ExperimentalData#setDisplayId(String)}, </li>
	 * 		<li>{@link ExperimentalData#setVersion(String)}, </li>
	 * 		<li>{@link ExperimentalData#setWasDerivedFrom(URI)}, </li>
	 * 		<li>{@link Identified#setAnnotations(List)}, or</li>
	 * 		<li>{@link SBOLDocument#addExperimentalData(ExperimentalData)}.</li>
	 * 	</ul>
	 * </li>
	 * </ul>
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static ExperimentalData parseExperimentalData(SBOLDocument SBOLDoc, IdentifiableDocument<QName> topLevel,
			Map<URI, NestedDocument<QName>> nested) throws SBOLValidationException
	{
		String displayId 	   = null;//URIcompliance.extractDisplayId(topLevel.getIdentity());
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;//URI.create(URIcompliance.extractPersistentId(topLevel.getIdentity()));
		String version 		   = null;
		Set<URI> wasDerivedFroms	 = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		Set<URI> attachments = new HashSet<>();
		List<Annotation> annotations = new ArrayList<>();

		for (NamedProperty<QName> namedProperty : topLevel.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", topLevel.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", topLevel.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", topLevel.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", topLevel.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213",topLevel.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208", topLevel.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",topLevel.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.TopLevel.hasAttachment))
			{
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
					attachments.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
				}
				else if (namedProperty.getValue() instanceof IdentifiableDocument) {
					if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.Attachment.Attachment)) {
						Attachment attachment = parseAttachment(SBOLDoc,(IdentifiableDocument<QName>)namedProperty.getValue());
						attachments.add(attachment.getIdentity());
					} else {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
				}
				else {
					throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.ExperimentalData.ExperimentalData.getNamespaceURI()+Sbol2Terms.ExperimentalData.ExperimentalData.getLocalPart())) {
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		ExperimentalData c = new ExperimentalData(topLevel.getIdentity());
		if (displayId != null)
			c.setDisplayId(displayId);
		if (version != null)
			c.setVersion(version);
		if (persistentIdentity != null)
			c.setPersistentIdentity(persistentIdentity);
		if (name != null)
			c.setName(name);
		if (description != null)
			c.setDescription(description);
		c.setWasDerivedFroms(wasDerivedFroms);
		c.setWasGeneratedBys(wasGeneratedBys);
		c.setAttachments(attachments);
		if (!annotations.isEmpty())
			c.setAnnotations(annotations);

		ExperimentalData oldC = SBOLDoc.getExperimentalData(topLevel.getIdentity());
		if (oldC == null) {
			SBOLDoc.addExperimentalData(c);
		} else {
			if (!c.equals(oldC)) {
				throw new SBOLValidationException("sbol-10202",c);
			}
		}
		return c;
	}

	/**
	 * @param SBOLDoc
	 * @param topLevel
	 * @param nested
	 * @return
	 * 
 	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>any of the following SBOL validation rules was violated: 
	 * 10202, 10203, 10204, 10206, 10208, 10212, 10213, 11602, 11607 or</li>
	 * <li>an SBOL validation rule violation occurred in the following constructor or methods:
	 * 	<ul>
	 * 		<li>{@link #parseModule(NestedDocument, Map)},</li>
	 * 		<li>{@link #parseInteraction(NestedDocument, Map)},</li>
	 * 		<li>{@link #parseFunctionalComponent(NestedDocument, Map)},</li>	
	 * 		<li>{@link ModuleDefinition#ModuleDefinition(URI)}, </li>
	 * 		<li>{@link ModuleDefinition#setDisplayId(String)}, </li>
	 * 		<li>{@link ModuleDefinition#setVersion(String)}, </li>
	 * 		<li>{@link ModuleDefinition#setFunctionalComponents(Set)}, </li>
	 * 		<li>{@link ModuleDefinition#setInteractions(Set)}, </li>
	 * 		<li>{@link ModuleDefinition#setModels(Set)}, </li>
	 * 		<li>{@link ModuleDefinition#setModules(Set)}, </li>
	 * 		<li>{@link ModuleDefinition#setWasDerivedFrom(URI)}, </li>
	 * 		<li>{@link ModuleDefinition#setAnnotations(List)}, or</li>
	 * 		<li>{@link SBOLDocument#addModuleDefinition(ModuleDefinition)}.</li>
	 * 	</ul>
	 * </li>
	 * </ul>
	 */
	@SuppressWarnings("unchecked")
	private static ModuleDefinition parseModuleDefinition(SBOLDocument SBOLDoc,
			IdentifiableDocument<QName> topLevel, Map<URI, NestedDocument<QName>> nested) throws SBOLValidationException
	{
		String displayId 	   = null;//URIcompliance.extractDisplayId(topLevel.getIdentity());
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;//URI.create(URIcompliance.extractPersistentId(topLevel.getIdentity()));
		String version 	       = null;
		Set<URI> wasDerivedFroms = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		Set<URI> attachments = new HashSet<>();
		Set<URI> roles 		   = new HashSet<>();
		Set<URI> models 	   = new HashSet<>();

		Set<FunctionalComponent> functionalComponents = new HashSet<>();
		Set<Interaction> interactions 				   = new HashSet<>();
		Set<Module> subModules 					   = new HashSet<>();
		List<Annotation> annotations 				   = new ArrayList<>();

		for (NamedProperty<QName> namedProperty : topLevel.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", topLevel.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", topLevel.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", topLevel.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.ModuleDefinition.roles))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-11602", topLevel.getIdentity());
				}
				roles.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.ModuleDefinition.hasModule) ||
					namedProperty.getName().equals(Sbol2Terms.ModuleDefinition.hasSubModule))
			{
				if (namedProperty.getValue() instanceof NestedDocument) {
					NestedDocument<QName> nestedDocument = ((NestedDocument<QName>) namedProperty.getValue());
					if (nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.Module.Module)) {
						throw new SBOLValidationException("sbol-11604",topLevel.getIdentity());
					}
					subModules.add(parseModule(SBOLDoc, ((NestedDocument<QName>) namedProperty.getValue()), nested));
				}
				else {
					URI uri = (URI) ((Literal<QName>)namedProperty.getValue()).getValue();
					NestedDocument<QName> nestedDocument = nested.get(uri);
					if (nestedDocument==null || nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.Module.Module)) {
						throw new SBOLValidationException("sbol-11604",topLevel.getIdentity());
					}
					subModules.add(parseModule(SBOLDoc, nested.get(uri), nested));
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.ModuleDefinition.hasInteractions))
			{
				if (namedProperty.getValue() instanceof NestedDocument) {
					NestedDocument<QName> nestedDocument = ((NestedDocument<QName>) namedProperty.getValue());
					if (nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.Interaction.Interaction)) {
						throw new SBOLValidationException("sbol-11605",topLevel.getIdentity());
					}
					interactions.add(parseInteraction((NestedDocument<QName>) namedProperty.getValue(), nested));
				}
				else {
					URI uri = (URI) ((Literal<QName>)namedProperty.getValue()).getValue();
					NestedDocument<QName> nestedDocument = nested.get(uri);
					if (nestedDocument==null || nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.Interaction.Interaction)) {
						throw new SBOLValidationException("sbol-11605",topLevel.getIdentity());
					}
					interactions.add(parseInteraction(nested.get(uri), nested));
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.ModuleDefinition.hasfunctionalComponent)||
					namedProperty.getName().equals(Sbol2Terms.ComponentDefinition.hasComponent))
			{
				if (namedProperty.getValue() instanceof NestedDocument) {
					NestedDocument<QName> nestedDocument = ((NestedDocument<QName>) namedProperty.getValue());
					if (nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.FunctionalComponent.FunctionalComponent)) {
						throw new SBOLValidationException("sbol-11606",topLevel.getIdentity());
					}
					functionalComponents
					.add(parseFunctionalComponent(SBOLDoc, (NestedDocument<QName>) namedProperty.getValue(), nested));
				}
				else {
					URI uri = (URI) ((Literal<QName>)namedProperty.getValue()).getValue();
					NestedDocument<QName> nestedDocument = nested.get(uri);
					if (nestedDocument==null || nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.FunctionalComponent.FunctionalComponent)) {
						throw new SBOLValidationException("sbol-11606",topLevel.getIdentity());
					}
					functionalComponents.add(parseFunctionalComponent(SBOLDoc, nested.get(uri), nested));
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.ModuleDefinition.hasModels))
			{
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-11607", topLevel.getIdentity());
					}
					models.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
				}
				else if (namedProperty.getValue() instanceof IdentifiableDocument) {
					if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.Model.Model)) {
						Model model = parseModel(SBOLDoc,(IdentifiableDocument<QName>)namedProperty.getValue());
						models.add(model.getIdentity());
					} else {
						throw new SBOLValidationException("sbol-11607", topLevel.getIdentity());
					}
				}
				else {
					throw new SBOLValidationException("sbol-11607", topLevel.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", topLevel.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213",topLevel.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208",topLevel.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",topLevel.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.TopLevel.hasAttachment))
			{
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
					attachments.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
				}
				else if (namedProperty.getValue() instanceof IdentifiableDocument) {
					if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.Attachment.Attachment)) {
						Attachment attachment = parseAttachment(SBOLDoc,(IdentifiableDocument<QName>)namedProperty.getValue());
						attachments.add(attachment.getIdentity());
					} else {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
				}
				else {
					throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.ModuleDefinition.ModuleDefinition.getNamespaceURI()+Sbol2Terms.ModuleDefinition.ModuleDefinition.getLocalPart())) {
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		//		ModuleDefinition moduleDefinition = SBOLDoc.createModuleDefinition(topLevel.getIdentity());
		ModuleDefinition moduleDefinition = new ModuleDefinition(topLevel.getIdentity());
		if (!roles.isEmpty())
			moduleDefinition.setRoles(roles);
		if (persistentIdentity != null)
			moduleDefinition.setPersistentIdentity(persistentIdentity);
		if (version != null)
			moduleDefinition.setVersion(version);
		if (displayId != null)
			moduleDefinition.setDisplayId(displayId);
		if (!functionalComponents.isEmpty())
			moduleDefinition.setFunctionalComponents(functionalComponents);
		if (!interactions.isEmpty())
			moduleDefinition.setInteractions(interactions);
		if (!models.isEmpty())
			moduleDefinition.setModels(models);
		if (!subModules.isEmpty())
			moduleDefinition.setModules(subModules);
		if (name != null)
			moduleDefinition.setName(name);
		if (description != null)
			moduleDefinition.setDescription(description);
		moduleDefinition.setWasDerivedFroms(wasDerivedFroms);
		moduleDefinition.setWasGeneratedBys(wasGeneratedBys);
		moduleDefinition.setAttachments(attachments);
		if (!annotations.isEmpty())
			moduleDefinition.setAnnotations(annotations);

		ModuleDefinition oldM = SBOLDoc.getModuleDefinition(topLevel.getIdentity());
		if (oldM == null) {
			SBOLDoc.addModuleDefinition(moduleDefinition);
		} else {
			if (!moduleDefinition.equals(oldM)) {
				throw new SBOLValidationException("sbol-10202",moduleDefinition);
			}
		}
		return moduleDefinition;
	}

	/**
	 * @param module
	 * @param nested
	 * @return
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>any of the following SBOL validation rules was violated: 
	 * 10203, 10204, 10206, 10208, 10212, 10213, 11604, 11702, 11707; or 
	 *</li>
	 * <li>an SBOL validation rule violation occurred in the following constructor or methods:
	 * 	<ul>
	 * 		<li>{@link #parseMapsTo(NestedDocument, boolean)},</li>
	 * 		<li>{@link Module#Module(URI, URI)},</li>
	 * 		<li>{@link Module#setDisplayId(String)},</li>
	 * 		<li>{@link Module#setVersion(String)},</li>
	 * 		<li>{@link Module#setWasDerivedFrom(URI)},</li>
	 * 		<li>{@link Identified#setAnnotations(List)}, or</li>
	 * 		<li>{@link Module#setMapsTos(Set)}.</li>
	 * 	</ul>
	 * </li>
	 * </ul>
	 */
	@SuppressWarnings("unchecked")
	private static Module parseModule(SBOLDocument SBOLDoc, NestedDocument<QName> module, 
			Map<URI, NestedDocument<QName>> nested) throws SBOLValidationException
	{
		String displayId 	   = null;//URIcompliance.extractDisplayId(module.getIdentity());
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;//URI.create(URIcompliance.extractPersistentId(module.getIdentity()));
		String version 		   = null;
		URI definitionURI 	   = null;
		Set<URI> wasDerivedFroms     = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		Set<MapsTo> mappings 		 = new HashSet<>();
		Set<Measure> measures 		 = new HashSet<>();
		List<Annotation> annotations = new ArrayList<>();

		for (NamedProperty<QName> namedProperty : module.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", module.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", module.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", module.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Module.hasMapsTo))
			{
				if (namedProperty.getValue() instanceof NestedDocument) {
					NestedDocument<QName> nestedDocument = ((NestedDocument<QName>) namedProperty.getValue());
					if (nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.MapsTo.MapsTo)) {
						throw new SBOLValidationException("sbol-11706",module.getIdentity());
					}
					mappings.add(parseMapsTo(((NestedDocument<QName>) namedProperty.getValue()),false));
				}
				else {
					URI uri = (URI) ((Literal<QName>)namedProperty.getValue()).getValue();
					NestedDocument<QName> nestedDocument = nested.get(uri);
					if (nestedDocument==null || nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.MapsTo.MapsTo)) {
						throw new SBOLValidationException("sbol-11706",module.getIdentity());
					}
					mappings.add(parseMapsTo(nested.get(uri),false));
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Module.hasMapping))
			{
				System.err.println("Warning: tag should be sbol:mapTo, not sbol:mapping.");
				if (namedProperty.getValue() instanceof NestedDocument) {
					mappings.add(parseMapsTo(((NestedDocument<QName>) namedProperty.getValue()),true));
				}
				else {
					URI uri = (URI) ((Literal<QName>)namedProperty.getValue()).getValue();
					mappings.add(parseMapsTo(nested.get(uri),true));
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Measured.hasMeasure))
			{
				if (namedProperty.getValue() instanceof NestedDocument) {
					NestedDocument<QName> nestedDocument = ((NestedDocument<QName>) namedProperty.getValue());
					if (nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.Measure.Measure)) {
						throw new SBOLValidationException("sbol-11707",module.getIdentity());
					}
					measures.add(parseMeasure(((NestedDocument<QName>) namedProperty.getValue())));
				}
				else {
					URI uri = (URI) ((Literal<QName>)namedProperty.getValue()).getValue();
					NestedDocument<QName> nestedDocument = nested.get(uri);
					if (nestedDocument==null || nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.Measure.Measure)) {
						throw new SBOLValidationException("sbol-11707",module.getIdentity());
					}
					measures.add(parseMeasure(nested.get(uri)));
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Module.hasDefinition))
			{
				if (definitionURI != null) {
					throw new SBOLValidationException("sbol-11702", module.getIdentity());
				}
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-11702", module.getIdentity());
					}	
					definitionURI = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
				}
				else if (namedProperty.getValue() instanceof IdentifiableDocument) {
					if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.ModuleDefinition.ModuleDefinition)) {
						ModuleDefinition moduleDefinition = parseModuleDefinition(SBOLDoc,
								(IdentifiableDocument<QName>)namedProperty.getValue(),nested);
						definitionURI = moduleDefinition.getIdentity();
					} else {
						throw new SBOLValidationException("sbol-11702", module.getIdentity());
					}
				}
				else {
					throw new SBOLValidationException("sbol-11702", module.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", module.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213", module.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208", module.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",module.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.Module.Module.getNamespaceURI()+Sbol2Terms.Module.Module.getLocalPart())) {
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		Module submodule = new Module(module.getIdentity(), definitionURI);
		if (persistentIdentity != null)
			submodule.setPersistentIdentity(persistentIdentity);
		if (version != null)
			submodule.setVersion(version);
		if (displayId != null)
			submodule.setDisplayId(displayId);
		if (!mappings.isEmpty())
			submodule.setMapsTos(mappings);
		if (name != null)
			submodule.setName(name);
		if (description != null)
			submodule.setDescription(description);
		submodule.setWasDerivedFroms(wasDerivedFroms);
		submodule.setWasGeneratedBys(wasGeneratedBys);
		if (!measures.isEmpty()) 
			submodule.setMeasures(measures);
		if (!annotations.isEmpty())
			submodule.setAnnotations(annotations);
		return submodule;
	}

	/**
	 * @param mapsTo
	 * @param inModule
	 * @return
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>any of the following SBOL validation rules was violated: 
	 * 10203, 10204, 10206, 10208, 10212, 10213, 10606, 10802, 10805, 10810, 11706; or
	 *</li>
	 * <li>an SBOL validation rule violation occurred in the following constructor or methods:
	 * 	<ul>
	 * 		<li>{@link MapsTo#MapsTo(URI, RefinementType, URI, URI)},</li> 		
	 * 		<li>{@link MapsTo#setDisplayId(String)},</li>
	 * 		<li>{@link MapsTo#setVersion(String)},</li>
	 * 		<li>{@link MapsTo#setWasDerivedFrom(URI)}, or</li>
	 * 		<li>{@link Identified#setAnnotations(List)}.</li>
	 * 	</ul>
	 * </li>
	 * </ul>
	 */
	private static MapsTo parseMapsTo(NestedDocument<QName> mapsTo, boolean inModule) throws SBOLValidationException
	{
		String displayId 	   = null;//URIcompliance.extractDisplayId(mapsTo.getIdentity());
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;//URI.create(URIcompliance.extractPersistentId(mapsTo.getIdentity()));
		String version 	  	 	  = null;
		URI remote 				  = null;
		RefinementType refinement = null;
		URI local 				  = null;
		Set<URI> wasDerivedFroms     = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();

		List<Annotation> annotations = new ArrayList<>();

		for (NamedProperty<QName> namedProperty : mapsTo.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", mapsTo.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", mapsTo.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", mapsTo.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213",mapsTo.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", mapsTo.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.MapsTo.refinement))
			{
				if (!(namedProperty.getValue() instanceof Literal) || refinement != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10810", mapsTo.getIdentity());
				}
				String refinementStr = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
				if (!refinementStr.startsWith("http://sbols.org/v2#")) {
					System.err.println("Warning: namespace for refinement types should be http://sbols.org/v2#");
					refinementStr = "http://sbols.org/v2#" + refinementStr;
				}
				try {
					refinement = RefinementType.convertToRefinementType(URI.create(refinementStr));
				} catch (SBOLValidationException e) {
					throw new SBOLValidationException("sbol-10810",mapsTo.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.MapsTo.hasRemote))
			{
				if (!(namedProperty.getValue() instanceof Literal) || remote != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10805", mapsTo.getIdentity());
				}
				remote = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.MapsTo.hasLocal))
			{
				if (!(namedProperty.getValue() instanceof Literal) || local != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10802", mapsTo.getIdentity());
				}
				local = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208", mapsTo.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",mapsTo.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.MapsTo.MapsTo.getNamespaceURI()+Sbol2Terms.MapsTo.MapsTo.getLocalPart())) {
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		MapsTo map = new MapsTo(mapsTo.getIdentity(), refinement, local, remote);
		if (displayId != null)
			map.setDisplayId(displayId);
		if (name != null)
			map.setName(name);
		if (description != null)
			map.setDescription(description);
		if (persistentIdentity != null)
			map.setPersistentIdentity(persistentIdentity);
		if (version != null)
			map.setVersion(version);
		map.setWasDerivedFroms(wasDerivedFroms);
		map.setWasGeneratedBys(wasGeneratedBys);
		if (!annotations.isEmpty())
			map.setAnnotations(annotations);
		return map;
	}
	
	/**
	 * @param measure
	 * @return
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>any of the following SBOL validation rules was violated:
	 * 10203, 10204, 10206, 10208, 10212, 10213, 13502, 13503, 13504; or  
	 *</li>
	 * <li>an SBOL validation rule violation occurred in the following constructor or methods:
	 * 	<ul>
	 * 		<li>{@link Measure#Measure(URI, Double, URI)},</li>
	 * 		<li>{@link Measure#setDisplayId(String)},</li>
	 * 		<li>{@link Measure#setVersion(String)},</li>
	 * 		<li>{@link Measure#setWasDerivedFrom(URI)}, or</li>
	 * 		<li>{@link Identified#setAnnotations(List)}.</li>
	 * 	</ul>
	 * </li>
	 * </ul>
	 */
	private static Measure parseMeasure(NestedDocument<QName> measure) throws SBOLValidationException
	{
		String displayId 	   = null;//URIcompliance.extractDisplayId(participation.getIdentity());
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;//URI.create(URIcompliance.extractPersistentId(participation.getIdentity()));
		String version 		   = null;
		Set<URI> types 		   = new HashSet<>();
		Double hasNumericalValue = null;
		URI hasUnit			   = null;
		Set<URI> wasDerivedFroms	 = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		List<Annotation> annotations = new ArrayList<>();

		for (NamedProperty<QName> namedProperty : measure.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", measure.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", measure.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", measure.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", measure.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213", measure.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Measure.type))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-13504", measure.getIdentity());
				}
				types.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue()
						.toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Measure.hasNumericalValue))
			{
				if (!(namedProperty.getValue() instanceof Literal) || hasNumericalValue != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-13502", measure.getIdentity());
				}
				try {
					hasNumericalValue = Double.parseDouble(((Literal<QName>) namedProperty.getValue()).getValue().toString());
				} catch (Exception e) {
					throw new SBOLValidationException("sbol-13502", measure.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Measure.hasUnit))
			{
				if (!(namedProperty.getValue() instanceof Literal) || hasUnit != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-13503", measure.getIdentity());
				}
				hasUnit = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208", measure.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",measure.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.Measure.Measure.getNamespaceURI()+Sbol2Terms.Measure.Measure.getLocalPart())) {
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		Measure m = new Measure(measure.getIdentity(), hasNumericalValue, hasUnit);
		if (displayId != null)
			m.setDisplayId(displayId);
		if (name != null)
			m.setName(name);
		if (description != null)
			m.setDescription(description);
		if (persistentIdentity != null)
			m.setPersistentIdentity(persistentIdentity);
		if (version != null)
			m.setVersion(version);
		m.setWasDerivedFroms(wasDerivedFroms);
		m.setWasGeneratedBys(wasGeneratedBys);
		if(!annotations.isEmpty())
			m.setAnnotations(annotations);
		if (!types.isEmpty()) {
			m.setTypes(types);
		}
		return m;
	}

	/**
	 * @param interaction
	 * @param nested
	 * @return
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>any of the following SBOL validation rules was violated:
	 * 10203, 10204, 10206, 10208,  10212, 10213, 11605, 11902, 11908; or 
	 *</li>
	 * <li>an SBOL validation rule violation occurred in the following constructor or methods:
	 * 	<ul>
	 * 		<li>{@link #parseParticipation(NestedDocument)},</li>
	 * 		<li>{@link Interaction#Interaction(URI, Set)},</li>
	 * 		<li>{@link Interaction#setParticipations(Set)}, </li>
	 * 		<li>{@link Interaction#setDisplayId(String)}, </li>
	 * 		<li>{@link Interaction#setVersion(String)}, </li>
	 * 		<li>{@link Interaction#setWasDerivedFrom(URI)}, or</li>
	 * 		<li>{@link Identified#setAnnotations(List)}. </li>
	 * 	</ul>
	 * </li>
	 * </ul>
	 */
	private static Interaction parseInteraction(NestedDocument<QName> interaction, Map<URI, NestedDocument<QName>> nested) throws SBOLValidationException
	{
		String displayId 	   = null;//URIcompliance.extractDisplayId(interaction.getIdentity());
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;//URI.create(URIcompliance.extractPersistentId(interaction.getIdentity()));
		String version 		   = null;
		Set<URI> wasDerivedFroms   = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		Set<URI> type 		   			   = new HashSet<>();
		Set<Participation> participations = new HashSet<>();
		Set<Measure> measures = new HashSet<>();
		List<Annotation> annotations 	   = new ArrayList<>();

		for (NamedProperty<QName> namedProperty : interaction.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", interaction.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", interaction.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", interaction.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Interaction.type))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-11902", interaction.getIdentity());
				}
				type.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Interaction.hasParticipations))
			{
				if (namedProperty.getValue() instanceof NestedDocument) {
					NestedDocument<QName> nestedDocument = ((NestedDocument<QName>) namedProperty.getValue());
					if (nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.Participation.Participation)) {
						throw new SBOLValidationException("sbol-11906",interaction.getIdentity());
					}
					participations.add(parseParticipation(((NestedDocument<QName>) namedProperty.getValue()),nested));
				}
				else {
					URI uri = (URI) ((Literal<QName>)namedProperty.getValue()).getValue();
					NestedDocument<QName> nestedDocument = nested.get(uri);
					if (nestedDocument==null || nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.Participation.Participation)) {
						throw new SBOLValidationException("sbol-11906",interaction.getIdentity());
					}
					participations.add(parseParticipation(nested.get(uri),nested));
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", interaction.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213", interaction.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208", interaction.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",interaction.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.Interaction.Interaction.getNamespaceURI()+Sbol2Terms.Interaction.Interaction.getLocalPart())) {
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Measured.hasMeasure))
			{
				if (namedProperty.getValue() instanceof NestedDocument) {
					NestedDocument<QName> nestedDocument = ((NestedDocument<QName>) namedProperty.getValue());
					if (nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.Measure.Measure)) {
						throw new SBOLValidationException("sbol-11908",interaction.getIdentity());
					}
					measures.add(parseMeasure(((NestedDocument<QName>) namedProperty.getValue())));
				}
				else {
					URI uri = (URI) ((Literal<QName>)namedProperty.getValue()).getValue();
					NestedDocument<QName> nestedDocument = nested.get(uri);
					if (nestedDocument==null || nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.Measure.Measure)) {
						throw new SBOLValidationException("sbol-11908",interaction.getIdentity());
					}
					measures.add(parseMeasure(nested.get(uri)));
				}
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		Interaction i = new Interaction(interaction.getIdentity(), type);
		if (!participations.isEmpty())
			i.setParticipations(participations);
		if (persistentIdentity != null)
			i.setPersistentIdentity(persistentIdentity);
		if (version != null)
			i.setVersion(version);
		if (displayId != null)
			i.setDisplayId(displayId);
		if (name != null)
			i.setName(name);
		if (description != null)
			i.setDescription(description);
		i.setWasDerivedFroms(wasDerivedFroms);
		i.setWasGeneratedBys(wasGeneratedBys);
		if (!annotations.isEmpty())
			i.setAnnotations(annotations);
		if (!measures.isEmpty()) 
			i.setMeasures(measures);
		return i;
	}

	/**
	 * @param participation
	 * @return
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>any of the following SBOL validation rules was violated:
	 * 10203, 10204, 10206, 10208, 10212, 10213, 11906, 12002, 12004, 12008; or  
	 *</li>
	 * <li>an SBOL validation rule violation occurred in the following constructor or methods:
	 * 	<ul>
	 * 		<li>{@link Participation#Participation(URI, URI, Set)},</li>
	 * 		<li>{@link Participation#setDisplayId(String)},</li>
	 * 		<li>{@link Participation#setVersion(String)},</li>
	 * 		<li>{@link Participation#setWasDerivedFrom(URI)}, or</li>
	 * 		<li>{@link Identified#setAnnotations(List)}.</li>
	 * 	</ul>
	 * </li>
	 * </ul>
	 */
	private static Participation parseParticipation(NestedDocument<QName> participation, Map<URI, NestedDocument<QName>> nested) throws SBOLValidationException
	{
		String displayId 	   = null;//URIcompliance.extractDisplayId(participation.getIdentity());
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;//URI.create(URIcompliance.extractPersistentId(participation.getIdentity()));
		String version 		   = null;
		Set<URI> roles 		   = new HashSet<>();
		URI participant        = null;
		Set<URI> wasDerivedFroms	 = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		Set<Measure> measures = new HashSet<>();
		List<Annotation> annotations = new ArrayList<>();

		for (NamedProperty<QName> namedProperty : participation.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", participation.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", participation.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", participation.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", participation.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213", participation.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Participation.role))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-12004", participation.getIdentity());
				}
				roles.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue()
						.toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Participation.hasParticipant))
			{
				if (!(namedProperty.getValue() instanceof Literal) || participant != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-12002", participation.getIdentity());
				}
				participant = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208", participation.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",participation.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.Participation.Participation.getNamespaceURI()+Sbol2Terms.Participation.Participation.getLocalPart())) {
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Measured.hasMeasure))
			{
				if (namedProperty.getValue() instanceof NestedDocument) {
					NestedDocument<QName> nestedDocument = ((NestedDocument<QName>) namedProperty.getValue());
					if (nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.Measure.Measure)) {
						throw new SBOLValidationException("sbol-12008",participation.getIdentity());
					}
					measures.add(parseMeasure(((NestedDocument<QName>) namedProperty.getValue())));
				}
				else {
					URI uri = (URI) ((Literal<QName>)namedProperty.getValue()).getValue();
					NestedDocument<QName> nestedDocument = nested.get(uri);
					if (nestedDocument==null || nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.Measure.Measure)) {
						throw new SBOLValidationException("sbol-12008",participation.getIdentity());
					}
					measures.add(parseMeasure(nested.get(uri)));
				}
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		Participation p = new Participation(participation.getIdentity(), participant, roles);
		if (displayId != null)
			p.setDisplayId(displayId);
		if (name != null)
			p.setName(name);
		if (description != null)
			p.setDescription(description);
		if (persistentIdentity != null)
			p.setPersistentIdentity(persistentIdentity);
		if (version != null)
			p.setVersion(version);
		p.setWasDerivedFroms(wasDerivedFroms);
		p.setWasGeneratedBys(wasGeneratedBys);
		if(!annotations.isEmpty())
			p.setAnnotations(annotations);
		if (!measures.isEmpty()) 
			p.setMeasures(measures);
		return p;
	}

	/**
	 * @param functionalComponent
	 * @param nested
	 * @return
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>any of the following SBOL validation rules was violated:
	 * 10203, 10204, 10206, 10208, 10212, 10213, 10602, 10607, 10608, 11606, 11802; or  
	 * </li>
	 * <li>an SBOL validation rule violation occurred in the following constructor or methods:
	 * 	<ul>
	 * 		<li>{@link #parseMapsTo(NestedDocument, boolean)},</li>
	 * 		<li>{@link FunctionalComponent#FunctionalComponent(URI, AccessType, URI, DirectionType)},</li>
	 * 		<li>{@link FunctionalComponent#setDisplayId(String)},</li>
	 * 		<li>{@link FunctionalComponent#setVersion(String)},</li>
	 * 		<li>{@link FunctionalComponent#setWasDerivedFrom(URI)},</li>
	 * 		<li>{@link FunctionalComponent#setMapsTos(Set)}, or</li>	
	 * 		<li>{@link Identified#setAnnotations(List)}.</li>
	 * 	</ul>
	 * </li>
	 * </ul>
	 */
	@SuppressWarnings("unchecked")
	private static FunctionalComponent parseFunctionalComponent(SBOLDocument SBOLDoc,
			NestedDocument<QName> functionalComponent, Map<URI, NestedDocument<QName>> nested) throws SBOLValidationException
	{
		String displayId 	   = null;//URIcompliance.extractDisplayId(functionalComponent.getIdentity());
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null;//URI.create(URIcompliance.extractPersistentId(functionalComponent.getIdentity()));
		String version 			   = null;
		AccessType access 		   = null;
		DirectionType direction    = null;
		URI functionalComponentURI = null;
		Set<URI> wasDerivedFroms	 = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		Set<Measure> measures = new HashSet<>();

		List<Annotation> annotations = new ArrayList<>();
		Set<MapsTo> mappings 		 = new HashSet<>();

		for (NamedProperty<QName> namedProperty : functionalComponent.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", functionalComponent.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", functionalComponent.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", functionalComponent.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.ComponentInstance.access))
			{
				if (!(namedProperty.getValue() instanceof Literal) || access != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10607", functionalComponent.getIdentity());
				}
				String accessTypeStr = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
				if (accessTypeStr.startsWith("http://www.sbolstandard.org/")) {
					System.err.println("Warning: namespace for access types should be http://sbols.org/v2#");
					accessTypeStr = accessTypeStr.replace("http://www.sbolstandard.org/", "http://sbols.org/v2#");
				}
				try {
					access = AccessType.convertToAccessType(URI.create(accessTypeStr));
				}
				catch (SBOLValidationException e) {
					throw new SBOLValidationException("sbol-10607",functionalComponent.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.FunctionalComponent.direction))
			{
				if (!(namedProperty.getValue() instanceof Literal) || direction != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-11802", functionalComponent.getIdentity());
				}
				String directionTypeStr = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
				if (directionTypeStr.startsWith("http://www.sbolstandard.org/")) {
					System.err.println("Warning: namespace for direction types should be http://sbols.org/v2#");
					directionTypeStr = directionTypeStr.replace("http://www.sbolstandard.org/", "http://sbols.org/v2#");
					directionTypeStr = directionTypeStr.replace("input","in");
					directionTypeStr = directionTypeStr.replace("output","out");
				}
				try {
					direction = DirectionType.convertToDirectionType(URI.create(directionTypeStr));
				} catch (SBOLValidationException e) {
					throw new SBOLValidationException("sbol-11802",functionalComponent.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.ComponentInstance.hasMapsTo))
			{
				if (namedProperty.getValue() instanceof NestedDocument) {
					NestedDocument<QName> nestedDocument = ((NestedDocument<QName>) namedProperty.getValue());
					if (nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.MapsTo.MapsTo)) {
						throw new SBOLValidationException("sbol-10606",functionalComponent.getIdentity());
					}
					mappings.add(parseMapsTo(((NestedDocument<QName>) namedProperty.getValue()),false));
				}
				else {
					URI uri = (URI) ((Literal<QName>)namedProperty.getValue()).getValue();
					NestedDocument<QName> nestedDocument = nested.get(uri);
					if (nestedDocument==null || nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.MapsTo.MapsTo)) {
						throw new SBOLValidationException("sbol-10606",functionalComponent.getIdentity());
					}
					mappings.add(parseMapsTo(nested.get(uri),false));
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.ComponentInstance.hasComponentDefinition))
			{
				if (functionalComponentURI != null) {
					throw new SBOLValidationException("sbol-10602", functionalComponent.getIdentity());
				}
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-10602", functionalComponent.getIdentity());
					}
					functionalComponentURI = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
				}
				else if (namedProperty.getValue() instanceof IdentifiableDocument) {
					if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.ComponentDefinition.ComponentDefinition)) {
						ComponentDefinition componentDefinition = parseComponentDefinition(SBOLDoc,
								(IdentifiableDocument<QName>)namedProperty.getValue(),nested);
						functionalComponentURI = componentDefinition.getIdentity();
					} else {
						throw new SBOLValidationException("sbol-10602", functionalComponent.getIdentity());
					}
				}
				else {
					throw new SBOLValidationException("sbol-10602", functionalComponent.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", functionalComponent.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213", functionalComponent.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208", functionalComponent.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",functionalComponent.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.FunctionalComponent.FunctionalComponent.getNamespaceURI()+Sbol2Terms.FunctionalComponent.FunctionalComponent.getLocalPart())) {
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Measured.hasMeasure))
			{
				if (namedProperty.getValue() instanceof NestedDocument) {
					NestedDocument<QName> nestedDocument = ((NestedDocument<QName>) namedProperty.getValue());
					if (nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.Measure.Measure)) {
						throw new SBOLValidationException("sbol-10608",functionalComponent.getIdentity());
					}
					measures.add(parseMeasure(((NestedDocument<QName>) namedProperty.getValue())));
				}
				else {
					URI uri = (URI) ((Literal<QName>)namedProperty.getValue()).getValue();
					NestedDocument<QName> nestedDocument = nested.get(uri);
					if (nestedDocument==null || nestedDocument.getType()==null || 
							!nestedDocument.getType().equals(Sbol2Terms.Measure.Measure)) {
						throw new SBOLValidationException("sbol-10608",functionalComponent.getIdentity());
					}
					measures.add(parseMeasure(nested.get(uri)));
				}
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}

		}
		FunctionalComponent fc = new FunctionalComponent(
				functionalComponent.getIdentity(), access,
				functionalComponentURI, direction);
		if (persistentIdentity != null)
			fc.setPersistentIdentity(persistentIdentity);
		if (version != null)
			fc.setVersion(version);
		if (displayId != null)
			fc.setDisplayId(displayId);
		if (!mappings.isEmpty())
			fc.setMapsTos(mappings);
		if (name != null)
			fc.setName(name);
		if (description != null)
			fc.setDescription(description);
		fc.setWasDerivedFroms(wasDerivedFroms);
		fc.setWasGeneratedBys(wasGeneratedBys);
		if (!annotations.isEmpty())
			fc.setAnnotations(annotations);
		if (!measures.isEmpty()) 
			fc.setMeasures(measures);
		return fc;
	}

	/**
	 * @param SBOLDoc
	 * @param topLevel
	 * @return
	 * @throws SBOLValidationException if either of the following conditions is satisfied:
	 * <ul>
	 * <li>any of the following SBOL validation rules was violated: 
	 * 10202, 10203, 10204, 10206, 10208, 10212, 10213, 10402, 10403, ; or</li>
	 * <li>an SBOL validation rule violation occurred in the following constructor or methods:
	 * 	<ul>
	 * 		<li>{@link Sequence#Sequence(URI, String, URI)}, </li>
	 * 		<li>{@link Sequence#setDisplayId(String)}, </li>
	 * 		<li>{@link Sequence#setVersion(String)}, </li>
	 * 		<li>{@link Sequence#setWasDerivedFrom(URI)}, </li>
	 * 		<li>{@link Identified#setAnnotations(List)}, or</li>
	 * 		<li>{@link SBOLDocument#addSequence(Sequence)}.</li>
	 * 	</ul>
	 * </li>
	 * </ul>
	 */
	@SuppressWarnings("unchecked")
	private static Sequence parseSequence(SBOLDocument SBOLDoc, IdentifiableDocument<QName> topLevel) throws SBOLValidationException
	{
		String displayId 	   = null; //URIcompliance.extractDisplayId(topLevel.getIdentity());
		String name 	 	   = null;
		String description 	   = null;
		URI persistentIdentity = null; //URI.create(URIcompliance.extractPersistentId(topLevel.getIdentity()));
		String version 		   = null;
		String elements 	   = null;
		URI encoding 		   = null;
		Set<URI> wasDerivedFroms	 = new HashSet<>();
		Set<URI> wasGeneratedBys = new HashSet<>();
		Set<URI> attachments = new HashSet<>();
		List<Annotation> annotations = new ArrayList<>();

		for (NamedProperty<QName> namedProperty : topLevel.getProperties())
		{
			if (namedProperty.getName().equals(Sbol2Terms.Identified.persistentIdentity))
			{
				if (!(namedProperty.getValue() instanceof Literal) || persistentIdentity != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10203", topLevel.getIdentity());
				}
				persistentIdentity = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.version))
			{
				if (!(namedProperty.getValue() instanceof Literal) || version != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10206", topLevel.getIdentity());
				}
				version  = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.displayId))
			{
				if (!(namedProperty.getValue() instanceof Literal) || displayId != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10204", topLevel.getIdentity());
				}
				displayId = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Sequence.elements))
			{
				if (!(namedProperty.getValue() instanceof Literal) || elements != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10402", topLevel.getIdentity());
				}
				elements = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Sequence.encoding))
			{
				if (!(namedProperty.getValue() instanceof Literal) || encoding != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10403", topLevel.getIdentity());
				}
				encoding = URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString());
				if (encoding.toString().equals("http://dx.doi.org/10.1021/bi00822a023")) {
					encoding = Sequence.IUPAC_DNA;
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.title))
			{
				if (!(namedProperty.getValue() instanceof Literal) || name != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10212", topLevel.getIdentity());
				}
				name = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.description))
			{
				if (!(namedProperty.getValue() instanceof Literal) || description != null ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof String))) {
					throw new SBOLValidationException("sbol-10213",topLevel.getIdentity());
				}
				description = ((Literal<QName>) namedProperty.getValue()).getValue().toString();
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasDerivedFrom))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10208", topLevel.getIdentity());
				}
				wasDerivedFroms.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Identified.wasGeneratedBy))
			{
				if (!(namedProperty.getValue() instanceof Literal) ||
						(!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI))) {
					throw new SBOLValidationException("sbol-10221",topLevel.getIdentity());
				}
				wasGeneratedBys.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
			}
			else if (namedProperty.getName().equals(Sbol2Terms.TopLevel.hasAttachment))
			{
				if (namedProperty.getValue() instanceof Literal) {
					if (!(((Literal<QName>) namedProperty.getValue()).getValue() instanceof URI)) {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
					attachments.add(URI.create(((Literal<QName>) namedProperty.getValue()).getValue().toString()));
				}
				else if (namedProperty.getValue() instanceof IdentifiableDocument) {
					if (((IdentifiableDocument<QName>)namedProperty).getType().equals(Sbol2Terms.Attachment.Attachment)) {
						Attachment attachment = parseAttachment(SBOLDoc,(IdentifiableDocument<QName>)namedProperty.getValue());
						attachments.add(attachment.getIdentity());
					} else {
						throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
					}
				}
				else {
					throw new SBOLValidationException("sbol-10306", topLevel.getIdentity());
				}
			}
			else if (namedProperty.getName().equals(Sbol2Terms.Description.type) && 
					((Literal<QName>) namedProperty.getValue()).getValue().toString()
					.equals(Sbol2Terms.Sequence.Sequence.getNamespaceURI()+Sbol2Terms.Sequence.Sequence.getLocalPart())) {
			}
			else
			{
				annotations.add(new Annotation(namedProperty));
			}
		}

		//		Sequence sequence = SBOLDoc.createSequence(topLevel.getIdentity(), elements, encoding);
		Sequence sequence = new Sequence(topLevel.getIdentity(), elements, encoding);
		if (persistentIdentity != null)
			sequence.setPersistentIdentity(persistentIdentity);
		if (version != null)
			sequence.setVersion(version);
		if (displayId != null)
			sequence.setDisplayId(displayId);
		if (name != null)
			sequence.setName(name);
		if (description != null)
			sequence.setDescription(description);
		sequence.setWasDerivedFroms(wasDerivedFroms);
		sequence.setWasGeneratedBys(wasGeneratedBys);
		sequence.setAttachments(attachments);
		if (!annotations.isEmpty())
			sequence.setAnnotations(annotations);

		Sequence oldS = SBOLDoc.getSequence(topLevel.getIdentity());
		if (oldS == null) {
			SBOLDoc.addSequence(sequence);
		} else {
			if (!sequence.equals(oldS)) {
				throw new SBOLValidationException("sbol-10202",sequence);
			}
		}
		return sequence;
	}

	/**
	 * Check if a string begins with LOCUS indicating that it is GenBank file string
	 *
	 * @param inputString input string to check if it is a GenBank file string
	 * @return true if the string begins with LOCUS indicating that it is a GenBank file string
	 */
	public static boolean isGenBankString(String inputString) {
		if (inputString!=null && inputString.startsWith("LOCUS")) return true;
		return false;
	}
	
	/**
	 * Check if a file begins with LOCUS indicating that it is GenBank file
	 *
	 * @param fileName file name of file to check if it is a GenBank file
	 * @return true if the string begins with LOCUS indicating that it is a GenBank file
	 * @throws IOException if there is an I/O exception reading the file
	 */
	public static boolean isGenBankFile(String fileName) throws IOException {
		File file = new File(fileName);
		FileInputStream stream     = new FileInputStream(file);
		BufferedInputStream buffer = new BufferedInputStream(stream);
		String strLine;
		BufferedReader br = new BufferedReader(new InputStreamReader(buffer));
		strLine = br.readLine();
		br.close();
		return isGenBankString(strLine);
	}
	
	/**
	 * Check if a string begins with "&gt;" or ";" indicating that it is Fasta file string
	 *
	 * @param inputString input string to check if it is a Fasta file string
	 * @return true if the string begins with "&gt;" or ";" indicating that it is a Fasta file string
	 */
	public static boolean isFastaString(String inputString) {
		if (inputString!=null && (inputString.startsWith(">")||inputString.startsWith(";"))) return true;
		return false;
	}
	
	/**
	 * Check if a file begins with "&gt;" or ";" indicating that it is Fasta file
	 *
	 * @param fileName file name of file to check if it is a Fasta file
	 * @return true if the string begins with "&gt;" or ";" indicating that it is a Fasta file
	 * @throws IOException if there is an I/O exception reading the file
	 */
	public static boolean isFastaFile(String fileName) throws IOException {
		File file = new File(fileName);
		FileInputStream stream     = new FileInputStream(file);
		BufferedInputStream buffer = new BufferedInputStream(stream);
		String strLine;
		BufferedReader br = new BufferedReader(new InputStreamReader(buffer));
		strLine = br.readLine();
		br.close();
		return isFastaString(strLine);
	}
	
	/**
	 * Check if a string begins with "##gff-version 3" indicating that it is GFF3 file string
	 *
	 * @param inputString input string to check if it is a GFF3 file string
	 * @return true if the string begins with "##gff-version 3" indicating that it is a GFF3 file string
	 */
	public static boolean isGFF3String(String inputString) {
		if (inputString!=null && inputString.startsWith("##gff-version 3")) return true;
		return false;
	}
	
	/**
	 * Check if a file begins with "##gff-version 3" indicating that it is GFF3 file
	 *
	 * @param fileName file name of file to check if it is a GFF3 file
	 * @return true if the string begins with "##gff-version 3" indicating that it is a GFF3 file
	 * @throws IOException if there is an I/O exception reading the file
	 */
	public static boolean isGFF3File(String fileName) throws IOException {
		File file = new File(fileName);
		FileInputStream stream     = new FileInputStream(file);
		BufferedInputStream buffer = new BufferedInputStream(stream);
		String strLine;
		BufferedReader br = new BufferedReader(new InputStreamReader(buffer));
		strLine = br.readLine();
		br.close();
		return isGFF3String(strLine);
	}
    
	/**
	 * Check if a file begins with the byte sequence indicating it is a SnapGene file
	 *
	 * @param fileName file name of file to check if it is a Fasta file
	 * @return true if the string begins with "&gt;" or ";" indicating that it is a Fasta file
	 * @throws IOException if there is an I/O exception reading the file
	 */
	public static boolean isSnapGeneFile(String fileName) throws IOException {
		File file = new File(fileName);
		FileInputStream stream = new FileInputStream(file);
        byte[] magicNumber = new byte[13];

        // \9\0\0\0\13SnapGene is the magic number
        byte[] expectedMagicNumber = {0x09, 0x00, 0x00, 0x00, 0x0e, 0x53,
                                      0x6e, 0x61, 0x70, 0x47, 0x65, 0x6e, 0x65};


        int bytesRead = stream.read(magicNumber);

        if(bytesRead != 13) {
            // There should be at least 13 bytes in the file, so if we didn't read that many, 
            // we know that it can't be a SnapGene file. 
        	stream.close();
            return false;
        }
        stream.close();
        return Arrays.equals(magicNumber, expectedMagicNumber);
	}
}

