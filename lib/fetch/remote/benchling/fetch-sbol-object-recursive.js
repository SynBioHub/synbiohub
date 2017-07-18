
const SBOLDocument = require('sboljs')
const Range = require('sboljs/lib/Range')
const GenericLocation = require('sboljs/lib/GenericLocation')

const request = require('request')

const benchling = require('../../../benchling')

const splitUri = require('../../../splitUri')

const config = require('../../../config')

const doiRegex = require('doi-regex')

function fetchSBOLObjectRecursive(remoteConfig, sbol, type, uri) {

    const { displayId } = splitUri(uri)

    const version = '1' //new Date().getTime() + '_retrieved'

    if(displayId === remoteConfig.rootCollection.displayId) {

        const rootCollection = sbol.collection()
        rootCollection.displayId = remoteConfig.rootCollection.displayId
        rootCollection.version = version
        rootCollection.persistentIdentity = config.get('databasePrefix') + 'public/' + rootCollection.displayId
        rootCollection.uri = config.get('databasePrefix') + 'public/' + rootCollection.displayId + '/' + rootCollection.version
        rootCollection.wasDerivedFrom = 'https://benchling.com' //remoteConfig.url
        rootCollection.name = remoteConfig.rootCollection.name
        rootCollection.description = remoteConfig.rootCollection.description

        return benchling.getRootFolders(remoteConfig).then((rootFolders) => {

            return Promise.all(
                rootFolders.map((folder) => folderToCollection(folder.id))
            )

        }).then((collections) => {

            collections.forEach((collection) => {

                rootCollection.addMember(collection)

            })

            return Promise.resolve({
                sbol: sbol,
                object: rootCollection,
                remote: true
            })

        })
    }

    if(displayId.indexOf(remoteConfig.folderPrefix) === 0) {

        const folderId = displayId.slice(remoteConfig.folderPrefix.length)

        return folderToCollection(folderId).then((collection) => {

            return Promise.resolve({
                sbol: sbol,
                object: collection,
                remote: true
            })

        })


    }
    
    if(displayId.endsWith(remoteConfig.sequenceSuffix)) {

        const partDisplayId = displayId.slice(0, - remoteConfig.sequenceSuffix.length)
            
        return benchling.getSequence(remoteConfig, partDisplayId).then((part) => {
            
            const sequence = sbol.sequence()
            sequence.displayId = displayId
            sequence.name = part.name + ' sequence'
            sequence.version = version
            sequence.persistentIdentity = config.get('databasePrefix') + 'public/' + remoteConfig.id + '/' + sequence.displayId
            sequence.uri = sequence.persistentIdentity + '/' + sequence.version
            sequence.encoding = SBOLDocument.terms.dnaSequence
            sequence.elements = part.bases
            sequence.wasDerivedFrom = 'https://benchling.com' //remoteConfig.url + '/sequences/' + part.id

            return Promise.resolve({
                sbol: sbol,
                object: sequence,
                remote: true
            })

        })
    }

    return benchling.getPart(remoteConfig, displayId).then(partToComponentDefinition).then((componentDefinition) => {

        return Promise.resolve({
            sbol: sbol,
            object: componentDefinition,
            remote: true
        })

    })

    function folderToCollection(folderId) {

        return Promise.all([
            benchling.getFolder(remoteConfig, folderId),
            benchling.getFolderEntries(remoteConfig, folderId)
        ]).then((res) => {

            const [ folder, entries ] = res
            const collection = sbol.collection()
            collection.displayId = remoteConfig.folderPrefix + folder.id
            collection.version = version
            collection.persistentIdentity = config.get('databasePrefix') + 'public/' + remoteConfig.id + '/' + collection.displayId
            collection.uri = collection.persistentIdentity + '/' + collection.version
            collection.name = folder.name
            collection.wasDerivedFrom = 'https://benchling.com' //remoteConfig.url + '/folders/' + folder.id

            //collection.addStringAnnotation('http://wiki.synbiohub.org/wiki/Terms/ice#folderType', folder.type)

            return Promise.all(entries.map((entry) => {

                return benchling.getPart(remoteConfig, entry.id).then(partToComponentDefinition)
            
            })).then((componentDefinitions) => {

                componentDefinitions.forEach((componentDefinition) => {
                    collection.addMember(componentDefinition)
                })

                return Promise.resolve(collection)
            })



        })

    }

    function genBankToSO(genBankTerm) {
	if (genBankTerm === "allele") {
	    return "http://identifiers.org/so/SO:0001023";}
	if (genBankTerm === "attenuator") {
	    return "http://identifiers.org/so/SO:0000140";}
	if (genBankTerm === "C_region") {
	    return "http://identifiers.org/so/SO:0001834";}
	if (genBankTerm === "CAAT_signal") {
	    return "http://identifiers.org/so/SO:0000172";}
	if (genBankTerm === "CDS") {
	    return "http://identifiers.org/so/SO:0000316";}
	/* if (genBankTerm === "conflict") {
	   return "http://identifiers.org/so/SO_";} */
	if (genBankTerm === "D-loop") {
	    return "http://identifiers.org/so/SO:0000297";}
	if (genBankTerm === "D_segment") {
	    return "http://identifiers.org/so/SO:0000458";}
	if (genBankTerm === "enhancer") {
	    return "http://identifiers.org/so/SO:0000165";}
	if (genBankTerm === "exon") {
	    return "http://identifiers.org/so/SO:0000147";}
	if (genBankTerm === "gene") {
	    return "http://identifiers.org/so/SO:0000704";}
	if (genBankTerm === "GC_signal") {
	    return "http://identifiers.org/so/SO:0000173";}
	if (genBankTerm === "iDNA") {
	    return "http://identifiers.org/so/SO:0000723";}
	if (genBankTerm === "intron") {
	    return "http://identifiers.org/so/SO:0000188";}
	if (genBankTerm === "J_region") {
	    return "http://identifiers.org/so/SO:0000470";}
	if (genBankTerm === "LTR") {
	    return "http://identifiers.org/so/SO:0000286";}
	if (genBankTerm === "mat_peptide") {
	    return "http://identifiers.org/so/SO:0000419";}
	if (genBankTerm === "misc_binding") {
	    return "http://identifiers.org/so/SO:0000409";}
	if (genBankTerm === "misc_difference") {
	    return "http://identifiers.org/so/SO:0000413";}
	if (genBankTerm === "misc_feature") {
	    return "http://identifiers.org/so/SO:0000001";}
	if (genBankTerm === "misc_marker") {
	    return "http://identifiers.org/so/SO:0001645";}
	if (genBankTerm === "misc_recomb") {
	    return "http://identifiers.org/so/SO:0000298";}
	if (genBankTerm === "misc_RNA") {
	    return "http://identifiers.org/so/SO:0000233";}
	if (genBankTerm === "misc_signal") {
	    return "http://identifiers.org/so/SO:0001411";}
	if (genBankTerm === "misc_structure") {
	    return "http://identifiers.org/so/SO:0000002";}
	if (genBankTerm === "modified_base") {
	    return "http://identifiers.org/so/SO:0000305";}
	if (genBankTerm === "mRNA") {
	    return "http://identifiers.org/so/SO:0000234";}
	/* if (genBankTerm === "mutation") {
	   return "http://identifiers.org/so/SO_";} */
	if (genBankTerm === "N_region") {
	    return "http://identifiers.org/so/SO:0001835";}
	/* if (genBankTerm === "old_sequence") {
	   return "http://identifiers.org/so/SO_";} */
	if (genBankTerm === "polyA_signal") {
	    return "http://identifiers.org/so/SO:0000551";}
	if (genBankTerm === "polyA_site") {
	    return "http://identifiers.org/so/SO:0000553";}
	if (genBankTerm === "precursor_RNA") {
	    return "http://identifiers.org/so/SO:0000185";}
	if (genBankTerm === "prim_transcript") {
	    return "http://identifiers.org/so/SO:0000185";}
	if (genBankTerm === "primer") {
	    return "http://identifiers.org/so/SO:0000112";}
	if (genBankTerm === "primer_bind") {
	    return "http://identifiers.org/so/SO:0005850";}
	if (genBankTerm === "promoter") {
	    return "http://identifiers.org/so/SO:0000167";}
	if (genBankTerm === "protein_bind") {
	    return "http://identifiers.org/so/SO:0000410";}
	if (genBankTerm === "RBS") {
	    return "http://identifiers.org/so/SO:0000139";}
	if (genBankTerm === "rep_origin") {
	    return "http://identifiers.org/so/SO:0000296";}
	if (genBankTerm === "repeat_region") {
	    return "http://identifiers.org/so/SO:0000657";}
	if (genBankTerm === "repeat_unit") {
	    return "http://identifiers.org/so/SO:0000726";}
	if (genBankTerm === "rRNA") {
	    return "http://identifiers.org/so/SO:0000252";}
	if (genBankTerm === "S_region") {
	    return "http://identifiers.org/so/SO:0001836";}
	if (genBankTerm === "satellite") {
	    return "http://identifiers.org/so/SO:0000005";}
	if (genBankTerm === "scRNA") {
	    return "http://identifiers.org/so/SO:0000013";}
	if (genBankTerm === "sig_peptide") {
	    return "http://identifiers.org/so/SO:0000418";}
	if (genBankTerm === "snRNA") {
	    return "http://identifiers.org/so/SO:0000274";}
	if (genBankTerm === "source") {
	    return "http://identifiers.org/so/SO:0000149";}
	if (genBankTerm === "stem_loop") {
	    return "http://identifiers.org/so/SO:0000019";}
	if (genBankTerm === "STS") {
	    return "http://identifiers.org/so/SO:0000331";}
	if (genBankTerm === "TATA_signal") {
	    return "http://identifiers.org/so/SO:0000174";}
	if (genBankTerm === "terminator") {
	    return "http://identifiers.org/so/SO:0000141";}
	if (genBankTerm === "transit_peptide") {
	    return "http://identifiers.org/so/SO:0000725";}
	if (genBankTerm === "transposon") {
	    return "http://identifiers.org/so/SO:0001054";}
	if (genBankTerm === "tRNA") {
	    return "http://identifiers.org/so/SO:0000253";}
	/* if (genBankTerm === "unsure") {
	   return "http://identifiers.org/so/SO_";} */
	if (genBankTerm === "V_region") {
	    return "http://identifiers.org/so/SO:0001833";}
	if (genBankTerm === "variation") {
	    return "http://identifiers.org/so/SO:0001060";}
	if (genBankTerm === "-10_signal") {
	    return "http://identifiers.org/so/SO:0000175";}
	if (genBankTerm === "-35_signal") {
	    return "http://identifiers.org/so/SO:0000176";}
	if (genBankTerm === "3'clip") {
	    return "http://identifiers.org/so/SO:0000557";}
	if (genBankTerm === "3'UTR") {
	    return "http://identifiers.org/so/SO:0000205";}
	if (genBankTerm === "5'clip") {
	    return "http://identifiers.org/so/SO:0000555";}
	if (genBankTerm === "5'UTR") {
	    return "http://identifiers.org/so/SO:0000204";}
	if (genBankTerm === "regulatory") {
	    return "http://identifiers.org/so/SO:0005836";}
	if (genBankTerm === "snoRNA") {
	    return "http://identifiers.org/so/SO:0000275";}
	return ""
    }

    function partToComponentDefinition(part) {

        if(part.hasSequence) {

            return benchling.getSequence(remoteConfig, part.id).then(createSBOL)
            
        } else {

            return createSBOL()

        }

        function createSBOL() {

            const componentDefinition = sbol.componentDefinition()
            componentDefinition.displayId = part.id
            componentDefinition.version = version
            componentDefinition.persistentIdentity = config.get('databasePrefix') + 'public/' + remoteConfig.id + '/' + componentDefinition.displayId
            componentDefinition.uri = componentDefinition.persistentIdentity + '/' + componentDefinition.version
            componentDefinition.name = part.name
            componentDefinition.description = part.description
            componentDefinition.wasDerivedFrom = 'https://benchling.com' //'http://benchling.com' + part.editURL //remoteConfig.url + '/sequences/' + part.id
	    var first = true
	    part.aliases.forEach((alias) => {
		if (alias.startsWith('http://')||alias.startsWith('https://')) {
		    componentDefinition.wasDerivedFrom = alias
		    if (first) {
			componentDefinition.addUriAnnotation('http://wiki.synbiohub.org/wiki/Terms/benchling#edit', 'http://benchling.com' + part.editURL + '')
			first = false
		    }
		} else {
		    componentDefinition.addStringAnnotation('http://www.w3.org/2000/01/rdf-schema#label', alias + '')
		}
	    })
            //componentDefinition.addStringAnnotation('http://wiki.synbiohub.org/wiki/Terms/ice#id', part.id + '')

            componentDefinition.addStringAnnotation('http://purl.org/dc/terms/created', new Date(part.createdAt).toISOString() + '')
            componentDefinition.addStringAnnotation('http://purl.org/dc/terms/modified', new Date(part.modifiedAt).toISOString() + '')
            componentDefinition.addStringAnnotation('http://purl.org/dc/elements/1.1/creator', part.creator.name + '')
	    componentDefinition.addStringAnnotation('http://wiki.synbiohub.org/wiki/Terms/benchling#color', part.color + '')
	    
            componentDefinition.addType(SBOLDocument.terms.dnaRegion)
	    if (part.circular) {
		componentDefinition.addType("http://identifiers.org/so/SO:0000988")
	    } else {
		componentDefinition.addType("http://identifiers.org/so/SO:0000987")
	    }
	    // TODO: folder, tags, tagSchema, primers, notes

            const sequence = sbol.sequence()
            sequence.displayId = componentDefinition.displayId + remoteConfig.sequenceSuffix
            sequence.version = componentDefinition.version
            sequence.persistentIdentity = config.get('databasePrefix') + 'public/' + remoteConfig.id + '/' + sequence.displayId
            sequence.uri = sequence.persistentIdentity + '/' + sequence.version
            sequence.name = componentDefinition.name + ' sequence'
            sequence.encoding = SBOLDocument.terms.dnaSequence
            sequence.elements = part.bases
            sequence.wasDerivedFrom = 'https://benchling.com' //remoteConfig.url + '/sequences/' + part.id
            componentDefinition.addSequence(sequence)

	    var annotationNum = 0;

	    part.annotations.forEach((annotation) => {

                const sa = sbol.sequenceAnnotation()
                sa.displayId = 'annotation' + annotationNum
		annotationNum++
                sa.name = annotation.name
                sa.version = componentDefinition.version
                sa.persistentIdentity = componentDefinition.persistentIdentity + '/' + sa.displayId
                sa.uri = sa.persistentIdentity + '/' + componentDefinition.version
		if (genBankToSO(annotation.type) != "") {
		    sa.addRole(genBankToSO(annotation.type))
		} else if (genBankToSO(annotation.type.toLowerCase()) != "") {
		    sa.addRole(genBankToSO(annotation.type.toLowerCase()))
		} else {
		    sa.description = annotation.type
		    sa.addRole("http://identifiers.org/so/SO:0000001")
		}
		sa.addStringAnnotation('http://wiki.synbiohub.org/wiki/Terms/benchling#color', annotation.color + '')

		const range = sbol.range()
                range.displayId = 'range'
                range.persistentIdentity = sa.persistentIdentity + '/' + range.displayId
                range.version = componentDefinition.version
                range.uri = range.persistentIdentity + '/' + range.version
                range.start = annotation.start
		// 0 seems to mean the origin
		if (range.start===0) {
		    range.start = part.length
		}
                range.end = annotation.end
		if (range.end===0) {
		    range.end = part.length
		}
		// Start after end, so swap
		if (range.start > range.end) {
		    temp = range.start
		    range.start = range.end
		    range.end = temp
		}
		if (annotation.strand===1) {
                    range.orientation = 'http://sbols.org/v2#inline'
		} else if (annotation.strand===-1){
                    range.orientation = 'http://sbols.org/v2#reverseComplement'
		}
                sa.addLocation(range)

                componentDefinition.addSequenceAnnotation(sa)

            })

            return Promise.resolve(componentDefinition)
        }

    }

}

module.exports = {
    fetchSBOLObjectRecursive: fetchSBOLObjectRecursive
}

