
define([ 'visbol' ], function(visbol) {

    var Vec2 = visbol.Vec2,
        Rect = visbol.Rect;

    function createGeometry(boxSize) {

        return {
            start: Vec2(boxSize.x * 0.5, boxSize.y * 0.5),
            right: Vec2(boxSize.x, boxSize.y * 0.5),
            bottom: Vec2(boxSize.x * 0.5, boxSize.y),
            bottomRight: Vec2(boxSize.x, boxSize.y),
            top: Vec2(boxSize.x * 0.5, 0),
            topRight: Vec2(boxSize.x, 0)
        };
    }

    function renderGlyph(design, glyphObject, boxSize) {

        var geom = createGeometry(boxSize);

        var path = [

            'M' + Vec2.toPathString(geom.start),
            'C' + Vec2.toPathString(geom.right) + ' ' + Vec2.toPathString(geom.bottomRight) + ' ' + Vec2.toPathString(geom.bottom),
            'A' + Vec2.toPathString(geom.start) + ' 0 0 1 ' + Vec2.toPathString(geom.top),
            'L' + Vec2.toPathString(geom.topRight)

        ].join('');


        var glyph = design.surface.path(path);

        var color = glyphObject.color || 'black';

        glyph.attr('stroke', color);
        glyph.attr('stroke-width', glyphObject.thickness || '1px');
        glyph.attr('fill', 'none');

        glyph.marker('end', 10, 10, function(add) {
            add.path('M4,0L10,5L4,10z').stroke(color).fill(color);
        });

        return {
            glyph: glyph,
            backboneOffset: boxSize.y / 2
        };
    }

    return {

        render: renderGlyph

    };
});


