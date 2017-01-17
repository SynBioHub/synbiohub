
var soToGlyphType = require('./soToGlyphType')

function getDisplayList(componentDefinition) {

    var segments = [
        getDisplayListSegment(componentDefinition)
    ]

    sortedSubComponents(componentDefinition).forEach((component) => {

         if(component.definition) {

             var segment = getDisplayListSegment(component.definition)

             if(segment.sequence.length > 0) {
		 if (segments.filter(function(e) { return e.name == segment.name; }).length == 0) {
                     segments.push(segment)
		 }
	     }
         }

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
                name: name,
                uri: componentDefinition.uri + ''
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
                name: name,
                uri: sequenceAnnotation.uri + ''
            }
        })
    }
}

function sortedSequenceAnnotations(componentDefinition) {

    return componentDefinition.sequenceAnnotations.sort((a, b) => {

	if (a.ranges.length > 0 && b.ranges.length > 0) {
            return start(a) - start(b)
	} else if (a.component && b.component) {
	    return position(componentDefinition, a.component) - position(componentDefinition, b.component)
	}
	return start(a) - start(b)

    })

    function start(sequenceAnnotation) {

        return sequenceAnnotation.ranges.length > 0 ? sequenceAnnotation.ranges[0] : 0

    }

    // TODO: note that cycle of sequenceConstraints creates infinite loop
    function position(componentDefinition,component) {

	var curPos = 0
	componentDefinition.sequenceConstraints.forEach((sequenceConstraint) => {
	    sequenceConstraint.link()
	    if (sequenceConstraint.restriction.toString()==='http://sbols.org/v2#precedes') {
		if (sequenceConstraint.object.uri.toString()===component.uri.toString()) {
		    var subPos = position(componentDefinition,sequenceConstraint.subject)
		    if (subPos+1 > curPos)
			curPos = subPos + 1
		}
	    }
	})
	return curPos

    }

}

function sortedSubComponents(componentDefinition) {

    return sortedSequenceAnnotations(componentDefinition).map((sequenceAnnotation) => {

        return sequenceAnnotation.component

    })

}


module.exports = getDisplayList


