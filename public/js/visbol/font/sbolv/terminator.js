
define([ 'visbol' ], function(visbol) {

    var Vec2 = visbol.Vec2,
        Rect = visbol.Rect;

    function createGeometry(boxSize) {

        return {
            stemBottom: Vec2(boxSize.x / 2.0, boxSize.y),
            stemTop: Vec2(boxSize.x / 2.0, 0),
            topLeft: Vec2(0, 0),
            topRight: Vec2(boxSize.x, 0),
        };
    }

    function renderGlyph(design, glyphObject, boxSize) {

        var geom = createGeometry(boxSize);

        var path = [

            'M' + Vec2.toPathString(geom.stemBottom),
            'L' + Vec2.toPathString(geom.stemTop),
            'M' + Vec2.toPathString(geom.topLeft),
            'L' + Vec2.toPathString(geom.topRight),

        ].join('');

        var glyph = design.surface.path(path);

        glyph.attr('stroke', glyphObject.color || '#c23b22');
        glyph.attr('stroke-width', glyphObject.thickness || '5px');
        glyph.attr('stroke-linejoin', 'round');
        glyph.attr('fill', 'none');

        return {
            glyph: glyph,
            backboneOffset: boxSize.y
        };
    }

    return {

        render: renderGlyph,

        insets: {
            top: 0.2
        }

    };
});


