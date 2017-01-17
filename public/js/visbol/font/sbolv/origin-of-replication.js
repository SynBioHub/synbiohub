
define([ 'visbol' ], function(visbol) {

    var Vec2 = visbol.Vec2,
        Rect = visbol.Rect;

    function createGeometry(boxSize) {

    }

    function renderGlyph(design, glyphObject, boxSize) {

        var geom = createGeometry(boxSize);

        var glyph = design.surface.ellipse(
            boxSize.x, boxSize.y);


        glyph.attr('stroke', 'black');
        glyph.attr('fill', glyphObject.color || '#cee');

        if(glyphObject.uri)
            glyph.attr('data-uri', glyphObject.uri);

        return {
            glyph: glyph,
            backboneOffset: boxSize.y / 2
        };
    }

    return {

        render: renderGlyph

    };
});


