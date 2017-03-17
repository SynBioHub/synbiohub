
const SBOLDocument = require('sboljs')

const request = require('request')

const { getPart, getSequence } = require('../../../ice')

const splitUri = require('../../../splitUri')

function fetchSBOLObjectRecursive(remoteConfig, sbol, type, uri) {

    const { displayId } = splitUri(uri)

    // TODO collections
    //
    return getPart(remoteConfig, displayId).then((part) => {

        if(part.hasSequence) {

            return getSequence(part.id).then(createSBOL)
            
        } else {

            return createSBOL()

        }

        function createSBOL(iceSbol) {

            /* { "id": 130, "type": "STRAIN", "visible": "OK", "parents": [], "index": 0, "recordId": "cbf8d2cf-53c4-46ef-a661-4e13f7d8b901", "name": "JBEI-2464", "owner": "system", "ownerEmail": "system", "ownerId": 21, "creator": "Rachel Krupa", "creatorEmail": "", "creatorId": 0, "status": "complete", "shortDescription": "Biobrick B expression vector", "longDescription": "", "references": "", "creationTime": 1312923433031, "modificationTime": 1312923433031, "bioSafetyLevel": 1, "intellectualProperty": "", "partId": "JPUB_000130", "links": [], "principalInvestigator": "Jay Keasling", "principalInvestigatorId": 0, "selectionMarkers": [ "Chloramphenicol" ], "fundingSource": "", "basePairCount": 0, "featureCount": 0, "viewCount": 0, "hasAttachment": false, "hasSample": false, "hasSequence": false, "hasOriginalSequence": false, "parameters": [], "canEdit": false, "accessPermissions": [], "publicRead": true, "linkedParts": [], "strainData": { "host": "DH10B", "genotypePhenotype": "" } }*/

            const doc = new SBOLDocument()

            const componentDefinition = doc.componentDefinition()
            componentDefinition.displayId = part.partId
            componentDefinition.version = '1'
            componentDefinition.persistentIdentity = config.get('databasePrefix') + 'public/' + remoteConfig.id + '/' + componentDefinition.displayId
            componentDefinition.uri = componentDefinition.persistentIdentity + '/' + componentDefinition.version
            componentDefinition.name = part.name
            componentDefinition.description = part.shortDescription
            componentDefinition.wasDerivedFrom = remote + '/rest/parts/' + part.partId
            componentDefinition.addStringAnnotation('http://wiki.synbiohub.org/wiki/Terms/ice#id', part.id)
            componentDefinition.addStringAnnotation('http://wiki.synbiohub.org/wiki/Terms/ice#type', part.type)
            componentDefinition.addStringAnnotation('http://wiki.synbiohub.org/wiki/Terms/ice#visible', part.visible)
            // parents
            componentDefinition.addStringAnnotation('http://wiki.synbiohub.org/wiki/Terms/ice#index', part.index)
            componentDefinition.addStringAnnotation('http://wiki.synbiohub.org/wiki/Terms/ice#recordId', part.recordId)
            componentDefinition.addStringAnnotation('http://wiki.synbiohub.org/wiki/Terms/ice#owner', part.owner)
            componentDefinition.addStringAnnotation('http://wiki.synbiohub.org/wiki/Terms/ice#ownerEmail', part.ownerEmail)
            componentDefinition.addStringAnnotation('http://wiki.synbiohub.org/wiki/Terms/ice#ownerId', part.ownerId)
            componentDefinition.addStringAnnotation('http://purl.org/dc/elements/1.1/creator', part.creator)
            componentDefinition.addStringAnnotation('http://wiki.synbiohub.org/wiki/Terms/ice#creatorEmail', part.creatorEmail)
            componentDefinition.addStringAnnotation('http://wiki.synbiohub.org/wiki/Terms/ice#creatorId', part.creatorId)
            componentDefinition.addStringAnnotation('http://wiki.synbiohub.org/wiki/Terms/ice#status', part.status)
            componentDefinition.addStringAnnotation('http://wiki.synbiohub.org/wiki/Terms/synbiohub#mutableDescription', part.longDescription)
            componentDefinition.addStringAnnotation('http://wiki.synbiohub.org/wiki/Terms/ice#references', part.references)
            componentDefinition.addStringAnnotation('http://purl.org/dc/terms/created', part.creationTime)
            componentDefinition.addStringAnnotation('http://purl.org/dc/terms/modified', part.modificationTime)
            componentDefinition.addStringAnnotation('http://wiki.synbiohub.org/wiki/Terms/ice#bioSafetyLevel', part.bioSafetyLevel)
            componentDefinition.addStringAnnotation('http://wiki.synbiohub.org/wiki/Terms/ice#intellectualProperty', part.intellectualProperty)
            // links
            componentDefinition.addStringAnnotation('http://wiki.synbiohub.org/wiki/Terms/ice#principalInvestigator', part.principalInvestigator)
            componentDefinition.addStringAnnotation('http://wiki.synbiohub.org/wiki/Terms/ice#principalInvestigatorId', part.principalInvestigatorId)
            // selectionMarkers
            componentDefinition.addStringAnnotation('http://wiki.synbiohub.org/wiki/Terms/ice#fundingSource', part.fundingSource)
            componentDefinition.addStringAnnotation('http://wiki.synbiohub.org/wiki/Terms/ice#viewCount', part.viewCount)
            // parameters
            // linkedParts
            // strainData
            // genotypePhenotype

            if(iceSbol) {

                if(iceSbol.sequences.length > 0) {

                    const iceSequence = iceSbol.sequences[0]

                    const sequence = doc.sequence()
                    sequence.displayId = componentDefinition.displayId + '_sequence'
                    sequence.version = '1'
                    sequence.persistentIdentity = config.get('databasePrefix') + '/public/' + remoteConfig.id + '/' + sequence.displayId
                    sequence.uri = sequence.persistentIdentity + '/' + sequence.version
                    sequence.encoding = iceSequence.encoding
                    sequence.elements = iceSequence.elements
                    sequence.wasDerivedFrom = remote + '/rest/file/' + part.id + '/sequence/sbol2'

                    componentDefinition.addSequence(sequence)

                }

                iceSbol.sequenceAnnotations.forEach((iceSA) => {

                    const sa = doc.sequenceAnnotation()
                    sa.displayId = iceSA.displayId
                    sa.version = '1'
                    sa.persistentIdentity = componentDefinition.persistentIdentity + '/' + sa.displayId
                    sa.uri = sa.persistentIdentity + '/' + sa.version

                    iceSA.locations.forEach((iceLocation) => {

                        const range = doc.range()
                        range.displayId = iceLocation.displayId
                        range.persistentIdentity = sa.persistentIdentity + '/' + range.displayId
                        range.version = '1'
                        range.uri = range.persistentIdentity + '/' + range.version
                        range.start = iceLocation.start
                        range.end = iceLocation.end
                        range.orientation = 'http://sbols.org/v2#inline'

                        sa.addLocation(range)

                    })

                    componentDefinition.addSequenceAnnotation(sa)

                })
            }

            return Promise.resolve({
                sbol: doc,
                object: doc.componentDefinitions[0]
            })
        }

    })
}

module.exports = {
    fetchSBOLObjectRecursive: fetchSBOLObjectRecursive
}

