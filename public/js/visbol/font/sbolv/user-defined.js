
define([ 'visbol' ], function(visbol) {

    var Vec2 = visbol.Vec2,
        Rect = visbol.Rect,
        Matrix = visbol.Matrix;

    function renderGlyph(design, glyphObject, boxSize) {

        var glyph = design.surface.rect(boxSize.x, boxSize.y);

        glyph.attr('stroke', 'black');
        glyph.attr('fill', glyphObject.color || '#cee');

        var group = design.surface.group();

        group.add(glyph);

        return {
            glyph: group,
            backboneOffset: boxSize.y / 2.0
        };
    }

    return {

        render: renderGlyph

    };
});


