
var soToGlyphType = require('./soToGlyphType')

var config = require('./config')

function getDisplayList(componentDefinition) {

    var segments = [
        getDisplayListSegment(componentDefinition)
    ]

    segments = recurseGetDisplayList(componentDefinition,segments)

    return {
        version: 1,
        components: [
            {
                segments: segments
            }
        ]
    }

}

function recurseGetDisplayList(componentDefinition,segments) {

    sortedSubComponents(componentDefinition).forEach((component) => {

         if(component.definition) {

	     if (component.definition.components.length === 0) return segments

             var segment = getDisplayListSegment(component.definition)

             if(segment.sequence.length > 0) {
		 if (segments.filter(function(e) { return e.name == segment.name; }).length == 0) {
                     segments.push(segment)
		 }
	     }
	     segments = recurseGetDisplayList(component.definition,segments)
         }

    })
    return segments
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
                uri: '/'  + componentDefinition.uri.toString().replace(config.get('databasePrefix'),''),
		tooltip: 'Component\n'
	    }]
	}
    }

    return {
        name: displayName,
        sequence: sortedSequenceAnnotations(componentDefinition).map((sequenceAnnotation) => {

            var glyph = 'user-defined'

            var name = sequenceAnnotation.name
            var roles = sequenceAnnotation.roles

            var uri = ''
	    var tooltip = ''

            if(sequenceAnnotation.component) {

                var component = sequenceAnnotation.component
		tooltip = 'Component\n'
                if(component.definition) {

                    roles = roles.concat(component.definition.roles)

                    if(!name)
                        name = component.definition.name

                    uri = '/'  + component.definition.uri.toString().replace(config.get('databasePrefix'),'')
                }

            } else {
		tooltip = 'Feature\n'
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
                uri: uri,
		tooltip: tooltip
            }
        })
    }
}

function sortedSequenceAnnotations(componentDefinition) {

    return componentDefinition.sequenceAnnotations.sort((a, b) => {

	if (a.ranges.length > 0 && b.ranges.length > 0) {
	    if (start(a)===start(b)) {
		return end(a) - end(b)
	    } else {
		return start(a) - start(b)
	    }
	} else if (a.component && b.component) {
	    return position(componentDefinition, a.component) - position(componentDefinition, b.component)
	}
	return start(a) - start(b)

    })

    function start(sequenceAnnotation) {

	var minStart = sequenceAnnotation.ranges.length > 0 ? sequenceAnnotation.ranges[0].start : 0
	for (var i = 0; i < sequenceAnnotation.ranges.length; i++) {
	    if (sequenceAnnotation.ranges[i].start < minStart)
		minStart = sequenceAnnotation.ranges[i].start
	}
        return minStart

    }

    function end(sequenceAnnotation) {


	var maxEnd = sequenceAnnotation.ranges.length > 0 ? sequenceAnnotation.ranges[0].end : 0
	for (var i = 0; i < sequenceAnnotation.ranges.length; i++) {
	    if (sequenceAnnotation.ranges[i].end < maxEnd)
		maxEnd = sequenceAnnotation.ranges[i].end
	}
        return maxEnd

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


