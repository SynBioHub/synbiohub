
define([ 'visbol' ], function(visbol) {

    var Vec2 = visbol.Vec2,
        Rect = visbol.Rect;

    function createGeometry(boxSize) {

        return {
            boxBottomLeft: Vec2(0, boxSize.y),
            boxBottomRight: Vec2(boxSize.x, boxSize.y),
            boxTopRight: Vec2(boxSize.x, 0),
            boxTopLeft: Vec2(0, 0),
        };
    }

    function renderGlyph(design, glyphObject, boxSize) {

        var geom = createGeometry(boxSize);

        var path = [

            'M' + Vec2.toPathString(geom.boxBottomLeft),
            'L' + Vec2.toPathString(geom.boxBottomRight),

            'C' + Vec2.toPathString(geom.boxTopRight) + ' ' + Vec2.toPathString(geom.boxTopLeft)
                + ' ' + Vec2.toPathString(geom.boxBottomLeft),

            'Z'

        ].join('');

        var glyph = design.surface.path(path);

        glyph.attr('stroke', 'black');
        glyph.attr('fill', glyphObject.color || '#966FD6');

        return {
            glyph: glyph,
            backboneOffset: boxSize.y
        };
    }

    return {

        render: renderGlyph,

        insets: {

            top: 0.55,
            left: 0.2,
            right: 0.2
        }


    };
});


