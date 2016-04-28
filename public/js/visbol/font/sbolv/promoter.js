
define([ 'visbol' ], function(visbol) {

    var Vec2 = visbol.Vec2,
        Rect = visbol.Rect;

    function createGeometry(boxSize) {

        var arrowForkSize = Vec2(boxSize.y / 4.0,
                                 boxSize.y / 4.0);

        return {
            start: Vec2(0, boxSize.y),
            turn: Vec2(0, arrowForkSize.y),
            end: Vec2(boxSize.x, arrowForkSize.y),
            topArrowForkEnd: Vec2(boxSize.x - arrowForkSize.x, 0),
            bottomArrowForkEnd: Vec2(boxSize.x - arrowForkSize.x, arrowForkSize.y * 2)
        };
    }

    function renderGlyph(design, glyphObject, boxSize) {

        var geom = createGeometry(boxSize);

        var path = [

            'M' + Vec2.toPathString(geom.start),
            'L' + Vec2.toPathString(geom.turn),
            'L' + Vec2.toPathString(geom.end),
            'L' + Vec2.toPathString(geom.topArrowForkEnd),
            'M' + Vec2.toPathString(geom.end),
            'L' + Vec2.toPathString(geom.bottomArrowForkEnd)

        ].join('');

        var glyph = design.surface.path(path);

        glyph.attr('stroke', glyphObject.color || '#03c03c');
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


