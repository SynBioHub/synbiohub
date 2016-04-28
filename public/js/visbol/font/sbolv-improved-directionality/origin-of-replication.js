

define([ 'visbol' ], function(visbol) {

    var Vec2 = visbol.Vec2,
        Rect = visbol.Rect;

    function createGeometry(boxSize) {

        return {
            arrowStart: Vec2(boxSize.x / 2.0, 0),
            arrowEnd: Vec2(boxSize.x, 0)
        };
    }

    function renderGlyph(design, glyphObject, boxSize) {

        var geom = createGeometry(boxSize);

        var ellipse = design.surface.ellipse(
            boxSize.x, boxSize.y);

        ellipse.attr('stroke', 'black');
        ellipse.attr('fill', glyphObject.color || '#cee');

        var path = [

            'M' + Vec2.toPathString(geom.arrowStart),
            'L' + Vec2.toPathString(geom.arrowEnd)

        ].join('');

        var arrow = design.surface.path(path);

        arrow.attr('stroke', 'black');
        arrow.attr('fill', 'none');

        arrow.marker('end', 10, 10, function(add) {
            add.path('M4,0L10,5L4,10z').stroke('black').fill('black');
        });

        var glyph = design.surface.group();

        glyph.add(ellipse);
        glyph.add(arrow);

        return {
            glyph: glyph,
            backboneOffset: boxSize.y / 2
        };
    }

    return {

        render: renderGlyph

    };
});


