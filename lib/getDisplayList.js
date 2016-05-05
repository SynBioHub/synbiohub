
var soToGlyphType = require('./soToGlyphType')

function getDisplayList(componentDefinition) {

    var segments = [
        getDisplayListSegment(componentDefinition)
    ]

    sortedSubComponents(componentDefinition).forEach((component) => {

        var segment = getDisplayListSegment(component.definition)

        if(segment.sequence.length > 0)
            segments.push(segment)

    })

    return {
        version: 1,
        components: [
            {
                segments: segments
            }
        ]
    }

}

function getDisplayListSegment(componentDefinition) {

    return {
        name: componentDefinition.name,
        sequence: sortedSubComponents(componentDefinition).map((component) => {

            var glyph = 'user-defined'

            component.definition.roles.forEach((role) => {

                var so = (role + '').match(/SO.([0-9]+)/g)

                if(!so || !so.length)
                    return

                var soCode = so[0].split('_').join(':')

                var glyphType = soToGlyphType(soCode)

                if(glyphType)
                    glyph = glyphType
            })

            return {
                strand: "positive",
                type: glyph,
                id: component.uri + '',
                name: component.definition.name
            }
        })
    }
}

function sortedSubComponents(componentDefinition) {

    var components = []

    var sortedSequenceAnnotations = componentDefinition.sequenceAnnotations.sort((a, b) => {

        return start(a) - start(b)

    })

    return sortedSequenceAnnotations.map((sequenceAnnotation) => {

        return sequenceAnnotation.component

    })

    function start(sequenceAnnotation) {

        return sequenceAnnotation.ranges.length > 0 ? sequenceAnnotation.ranges[0] : 0

    }

}


module.exports = getDisplayList


