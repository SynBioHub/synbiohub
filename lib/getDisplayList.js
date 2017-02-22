
var soToGlyphType = require('./soToGlyphType')

var config = require('./config')

var sbolmeta = require('sbolmeta')

var URI = require('urijs')

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

        if(component.definition && !(component.definition instanceof URI)) {
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
	var name = componentDefinition.name != ''?componentDefinition.name:componentDefinition.displayId
	var roles = componentDefinition.roles

	var tooltip = 'Component\n'
	if (componentDefinition.displayId) tooltip += 'Identifier: ' + componentDefinition.displayId + '\n'
	if (componentDefinition.name) tooltip += 'Name: ' + componentDefinition.name + '\n'
	if (componentDefinition.description) tooltip += 'Description: ' + componentDefinition.description + '\n'
		
	roles.forEach((role) => {

            var so = (role + '').match(/SO.([0-9]+)/g)
	    
            if(!so || !so.length)
		return

            var soCode = so[0].split('_').join(':')

            var glyphType = soToGlyphType(soCode)

            if(glyphType)
		glyph = glyphType

	    tooltip += 'Role: ' + role
	})

	return {
	    name: displayName,
	    sequence: [{
                strand: "positive",
                type: glyph,
                id: componentDefinition.uri + '',
                name: name,
                uri: '/'  + componentDefinition.uri.toString().replace(config.get('databasePrefix'),''),
		tooltip: tooltip
	    }]
	}
    }

    return {
        name: displayName,
        sequence: sortedSequenceAnnotations(componentDefinition).map((sequenceAnnotation) => {

            var glyph = 'user-defined'

            var name = sequenceAnnotation.name!=''?sequenceAnnotation.name:sequenceAnnotation.displayId
            var roles = sequenceAnnotation.roles

            var uri = ''
	    var tooltip = ''

            if(sequenceAnnotation.component) {

                var component = sequenceAnnotation.component
		tooltip = 'Component\n'
                if(component.definition && !(component.definition instanceof URI)) {

                    roles = roles.concat(component.definition.roles)

                    name = component.definition.name!=''?component.definition.name:component.definition.displayId

                    uri = '/'  + component.definition.uri.toString().replace(config.get('databasePrefix'),'')

		    if (component.definition.displayId) tooltip += 'Identifier: ' + component.definition.displayId + '\n'
		    if (component.definition.name) tooltip += 'Name: ' + component.definition.name + '\n'
		    if (component.definition.description) tooltip += 'Description: ' + component.definition.description + '\n'
                } else {
		    uri = component.definition.toString()
		}

            } else {
		tooltip = 'Feature\n'
		if (sequenceAnnotation.displayId) tooltip += 'Identifier: ' + sequenceAnnotation.displayId + '\n'
		if (sequenceAnnotation.name) tooltip += 'Name: ' + sequenceAnnotation.name + '\n'
		if (sequenceAnnotation.description) tooltip += 'Description: ' + sequenceAnnotation.description + '\n'
	    }

            roles.forEach((role) => {

                var igemPartPrefix = 'http://wiki.synbiohub.org/wiki/Terms/igem#partType/'
                var igemFeaturePrefix = 'http://wiki.synbiohub.org/wiki/Terms/igem#feature/'
                var soPrefix = 'http://identifiers.org/so/'

                if(role.toString().indexOf(igemPartPrefix) === 0) {

                    tooltip += 'iGEM Part Type: ' + role.toString().slice(igemPartPrefix.length) + '\n'

                } else if(role.toString().indexOf(igemFeaturePrefix) === 0) {

                    tooltip += 'iGEM Feature Type: ' + role.toString().slice(igemFeaturePrefix.length) + '\n'

                } else if(role.toString().indexOf(soPrefix) === 0) {
		    
                    var soTerm = role.toString().slice(soPrefix.length).split('_').join(':')
                    tooltip += 'Role: ' + sbolmeta.sequenceOntology[soTerm].name + '\n'

                }

                var so = (role + '').match(/SO.([0-9]+)/g)

                if(!so || !so.length)
                    return

                var soCode = so[0].split('_').join(':')

                var glyphType = soToGlyphType(soCode)

                if(glyphType)
                    glyph = glyphType
            })
	    
	    sequenceAnnotation.ranges.forEach((range) => {
		if (range.orientation) tooltip += 'Orientation: ' + range.orientation.toString().replace('http://sbols.org/v2#','') + '\n'
		tooltip += range.start + '..' + range.end + '\n'
	    })
	    
	    sequenceAnnotation.cuts.forEach((cut) => {
		if (cut.orientation) tooltip += 'Orientation: ' + cut.orientation.toString().replace('http://sbols.org/v2#','') + '\n'
		tooltip += cut.at + '^' + cut.at + '\n'
	    })
	    
	    sequenceAnnotation.genericLocations.forEach((genericLocation) => {
		if (genericLocation.orientation) tooltip += 'Orientation: ' + genericLocation.orientation.toString().replace('http://sbols.org/v2#','') + '\n'
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
	    return position(componentDefinition, a.component, {}) - position(componentDefinition, b.component, {})
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
    function position(componentDefinition,component,visited) {

	var curPos = 0
	if (visited[component.uri]) return curPos
	componentDefinition.sequenceConstraints.forEach((sequenceConstraint) => {
	    sequenceConstraint.link()
	    if (sequenceConstraint.restriction.toString()==='http://sbols.org/v2#precedes') {
		if (sequenceConstraint.object.uri.toString()===component.uri.toString()) {
		    visited[component.uri] = true
		    var subPos = position(componentDefinition,sequenceConstraint.subject, visited)
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


