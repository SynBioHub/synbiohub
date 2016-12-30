
var soToGlyphType = require('./soToGlyphType')

function getDisplayList(componentDefinition) {

    var segments = [
        getDisplayListSegment(componentDefinition)
    ]

    // sortedSubComponents(componentDefinition).forEach((component) => {

    //      if(component.definition) {

    //          var segment = getDisplayListSegment(component.definition)

    //          if(segment.sequence.length > 0)
    //              segments.push(segment)

    //      }

    // })

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

    var displayName = componentDefinition.displayId 
    if (componentDefinition.name != '' && componentDefinition.name != componentDefinition.displayId) {
	displayName += ' ('+componentDefinition.name+')'
    }

    if (componentDefinition.sequenceAnnotations.length===0) {

	var glyph = 'user-defined'
	var name = componentDefinition.name
	var roles = componentDefinition.roles
		
	roles.forEach((role) => {

            var so = (role + '').match(/SO.([0-9]+)/g)
	    
            if(!so || !so.length)
		return

            var soCode = so[0].split('_').join(':')

            var glyphType = soToGlyphType(soCode)

            if(glyphType)
		glyph = glyphType
	})

	return {
	    name: displayName,
	    sequence: [{
                strand: "positive",
                type: glyph,
                id: componentDefinition.uri + '',
                name: name
	    }]
	}
    }

    return {
        name: displayName,
        sequence: sortedSequenceAnnotations(componentDefinition).map((sequenceAnnotation) => {

            var glyph = 'user-defined'

            var name = sequenceAnnotation.name
            var roles = sequenceAnnotation.roles

            if(sequenceAnnotation.component) {

                var component = sequenceAnnotation.component

                if(component.definition) {

                    roles = roles.concat(component.definition.roles)

                    if(!name)
                        name = component.definition.name
                }

            }

            roles.forEach((role) => {

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
                id: sequenceAnnotation.uri + '',
                name: name
            }
        })
    }
}

function sortedSequenceAnnotations(componentDefinition) {

    return componentDefinition.sequenceAnnotations.sort((a, b) => {

        return start(a) - start(b)

    })

    function start(sequenceAnnotation) {

        return sequenceAnnotation.ranges.length > 0 ? sequenceAnnotation.ranges[0] : 0

    }

}

function sortedSubComponents(componentDefinition) {

    return sortedSequenceAnnotations(componentDefinition).map((sequenceAnnotation) => {

        return sequenceAnnotation.component

    })

}


module.exports = getDisplayList


