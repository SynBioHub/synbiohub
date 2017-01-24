
define([ 'visbol' ], function(visbol) {

    var Vec2 = visbol.Vec2,
        Rect = visbol.Rect;

    function createGeometry(boxSize) {

        var arrowheadLength = 15

        return {
            lineStart: Vec2(0, boxSize.y * 0.5),
            lineEnd: Vec2(boxSize.x - arrowheadLength, boxSize.y * 0.5),
            arrowheadTop: Vec2(boxSize.x - arrowheadLength, 0),
            arrowheadRight: Vec2(boxSize.x, boxSize.y * 0.5),
            arrowheadBottom: Vec2(boxSize.x - arrowheadLength, boxSize.y),
        };
    }

    function renderGlyph(design, glyphObject, boxSize) {

        var geom = createGeometry(boxSize);

        var linePath = [

            'M' + Vec2.toPathString(geom.lineStart),
            'L' + Vec2.toPathString(geom.lineEnd)

        ].join('');

        var line = design.surface.path(linePath);

        line.attr('stroke', 'black');
        line.attr('stroke-width', '10')
        line.attr('stroke-dasharray', '5, 5');
        line.attr('fill', 'none')

        var arrowheadPath = [

            'M' + Vec2.toPathString(geom.arrowheadTop),
            'L' + Vec2.toPathString(geom.arrowheadRight),
            'L' + Vec2.toPathString(geom.arrowheadBottom),
            'Z'

        ].join('');

        var arrowhead = design.surface.path(arrowheadPath)

        arrowhead.attr('stroke', 'black');
        arrowhead.attr('stroke-width', '1')
        arrowhead.attr('fill', 'black')

        var glyph = design.surface.group().add(line).add(arrowhead)

        if(glyphObject.uri)
            glyph.attr('data-uri', glyphObject.uri);

        return {
            glyph: glyph,
            backboneOffset: boxSize.y / 2.0
        };
    }

    return {

        render: renderGlyph,

        insets: {
            top: 0.2,
            bottom: 0.2
        }

    };
});


