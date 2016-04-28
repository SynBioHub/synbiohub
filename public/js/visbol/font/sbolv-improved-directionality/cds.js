
define([ 'visbol' ], function(visbol) {

    var Vec2 = visbol.Vec2,
        Rect = visbol.Rect;

    function createGeometry(boxSize) {

        var headWidth = boxSize.y / 2.0;

        return {
            boxStart: Vec2(0, 0),
            boxTopRight: Vec2(boxSize.x - headWidth, 0),
            headPoint: Vec2(boxSize.x, boxSize.y / 2.0),
            boxBottomRight: Vec2(boxSize.x - headWidth, boxSize.y / 2),
            boxBottomLeft: Vec2(0, boxSize.y / 2)
        };
    }

    function renderGlyph(design, glyphObject, boxSize) {

        var geom = createGeometry(boxSize);

        var path = [

            'M' + Vec2.toPathString(geom.boxStart),
            'L' + Vec2.toPathString(geom.boxTopRight),
            'L' + Vec2.toPathString(geom.headPoint),
            'L' + Vec2.toPathString(geom.boxBottomRight),
            'L' + Vec2.toPathString(geom.boxBottomLeft),
            'Z'

        ].join('');

        var glyph = design.surface.path(path);

        glyph.attr('stroke', 'black');
        glyph.attr('fill', glyphObject.color || '#779ecb');

        return {
            glyph: glyph,
            backboneOffset: boxSize.y / 2.0
        };
    }

    return {

        render: renderGlyph

    };
});


