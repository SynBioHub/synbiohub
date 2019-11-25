package org.sbolstandard.core2;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.sbolstandard.core2.Testing.AnnotationTest;
import org.sbolstandard.core2.Testing.CollectionTest;
import org.sbolstandard.core2.Testing.ComponentDefinitionTest;
import org.sbolstandard.core2.Testing.ComponentTest;
import org.sbolstandard.core2.Testing.CutTest;
import org.sbolstandard.core2.Testing.EDAMOntologyTest;
import org.sbolstandard.core2.Testing.FunctionalComponentTest;
import org.sbolstandard.core2.Testing.GenericTopLevelTest;
import org.sbolstandard.core2.Testing.InteractionTest;
import org.sbolstandard.core2.Testing.MapsToTest;
import org.sbolstandard.core2.Testing.ModelTest;
import org.sbolstandard.core2.Testing.ModuleDefinitionTest;
import org.sbolstandard.core2.Testing.ModuleTest;
import org.sbolstandard.core2.Testing.ParticipationTest;
import org.sbolstandard.core2.Testing.RangeTest;
import org.sbolstandard.core2.Testing.SequenceAnnotationTest;
import org.sbolstandard.core2.Testing.SequenceConstraintTest;
import org.sbolstandard.core2.Testing.SequenceOntologyTest;
import org.sbolstandard.core2.Testing.SequenceTest;
import org.sbolstandard.core2.Testing.SystemsBiologyOntologyTest;


/**
 * Runs all specified Test Classes.
 * @author Zhen Zhang
 * @author Tramy Nguyen
 * @author Matthew Pocock
 * @author Goksel Misirli
 * @author Chris Myers
 */
@RunWith(Suite.class)
@SuiteClasses(
		{
//						SBOLGenerateFile.class,
			SBOLReaderTest.class,
			SBOLWriterTest.class,
			SBOLReadWriteTest.class,
			SBOLTestConversion.class,
			URIcomplianceTest.class,
			SBOLDocumentTest.class,
			ValidationTest.class,
			OntologyTest.class,
			GenbankTest.class,
			FASTATest.class,
			AnnotationTest.class,
			CollectionTest.class,
			ComponentDefinitionTest.class,
			ComponentTest.class,
			CutTest.class,
			EDAMOntologyTest.class,
			FunctionalComponentTest.class,
			GenericTopLevelTest.class,
			InteractionTest.class,
			MapsToTest.class,
			ModelTest.class,
			ModuleDefinitionTest.class,
			ModuleTest.class,
			ParticipationTest.class,
			RangeTest.class,
			SequenceAnnotationTest.class,
			SequenceConstraintTest.class,
			SequenceOntologyTest.class,
			SequenceTest.class,
			SystemsBiologyOntologyTest.class
				
		}
		)

public class SBOLTestSuite {
}


