
define([ 'visbol' ], function(visbol) {

    var Vec2 = visbol.Vec2,
        Rect = visbol.Rect,
        Matrix = visbol.Matrix;

    function createGeometry(boxSize) {

        var headWidth = boxSize.y / 2.0;

        return {
            boxStart: Vec2(0, 0),
            boxTopRight: Vec2(boxSize.x - headWidth, 0),
            headPoint: Vec2(boxSize.x, boxSize.y / 2.0),
            boxBottomRight: Vec2(boxSize.x - headWidth, boxSize.y),
            boxBottomLeft: Vec2(0, boxSize.y)
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
        glyph.attr('fill', 'pink')

        if(glyphObject.uri)
            glyph.attr('data-uri', glyphObject.uri);

            var glyphMatrix = Matrix();
            var labelMatrix = Matrix();



        glyph.transform({
            matrix: Matrix.toSVGString(Matrix.rotation(-135))
            //matrix: Matrix.toSVGString(Matrix())
        })

        var group = design.surface.group()
        group.add(glyph)

        return {
            glyph: group,
            backboneOffset: boxSize.y / 2.0
        };
    }

    return {

        render: renderGlyph,

        insets: {
            top: 0.35,
            bottom: 0.35,
            left: 0.2,
            right: 0.2
        }

    };
});


