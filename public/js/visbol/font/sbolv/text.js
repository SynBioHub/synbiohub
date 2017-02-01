
define([ 'visbol' ], function(visbol) {

    var Vec2 = visbol.Vec2,
        Rect = visbol.Rect,
        Matrix = visbol.Matrix;

    function renderGlyph(design, glyphObject, boxSize) {

        var text = design.surface.text('');

        text.font({ anchor: 'middle' });

        text.build(true);

        var label = text.tspan(glyphObject.text || '')

        text.attr('alignment-baseline', 'middle');

        text.build(false);

        return {
            glyph: text,
            backboneOffset: boxSize.y
        };
    }

    return {

        render: renderGlyph

    };
});


