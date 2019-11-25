package org.sbolstandard.core2.examples;

public class RunAllExamples 
{
    public static void main( String[] args ) throws Exception
    {
        AnnotationOutput.main(null);
        CollectionOutput.main(null);
        ComponentDefinitionOutput.main(null);
        CutExample.main(null);
        GenericTopLevelOutput.main(null);
        ModelOutput.main(null);
        ModuleDefinitionOutput.main(null);
        SBOLDocumentOutput.main(null);
        SequenceConstraintOutput.main(null);
        SequenceOutput.main(null);        
        SimpleComponentDefinitionExample.main(null);   
        SimpleModuleDefinition.main(null);
        Provenance_CodonOptimization.main(null);
        Provenance_StrainDerivation.main(null);
    }
}