
var soToGlyphType = require('./soToGlyphType')

function getDisplayList(componentDefinition) {

    var segments = [
        getDisplayListSegment(componentDefinition)
    ]

    componentDefinition.components.forEach((component) => {

        segments.push(getDisplayListSegment(component.definition))

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
        sequence: componentDefinition.components.map((component) => {

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


module.exports = getDisplayList


