
define([ './svg',
         './geom/rect',
         './geom/vec2',
         './geom/matrix'
], 

function(SVG, Rect, Vec2, Matrix) {

    function renderSegment(design, segment) {

        var surface = design.surface;

        var glyphOffset = 0;

        var glyphs = segment.sequence.map(function(glyphObject) {

            var glyph = design.font[glyphObject.type]

            var glyphLength;

            if(design.proportional &&
                    glyphObject.start !== glyphObject.end) {

                glyphLength = (glyphObject.end - glyphObject.start) * design.scale;

            } else {

                glyphLength = design.geom.defaultGlyphSize.x;

            }

            glyphLength = Math.max(glyphLength, design.geom.defaultGlyphSize.x);

            var boundingBoxSize = Vec2(glyphLength, design.geom.defaultGlyphSize.y);

            if(glyph === undefined)
                glyph = design.font['user-defined'];

            var glyphInsets = {
                left: glyph.insets && glyph.insets.left ?
                    glyph.insets.left * boundingBoxSize.x : 0,

                top: glyph.insets && glyph.insets.top ?
                    glyph.insets.top * boundingBoxSize.y : 0,

                right: glyph.insets && glyph.insets.right ?
                    glyph.insets.right * boundingBoxSize.x : 0,

                bottom: glyph.insets && glyph.insets.bottom ?
                    glyph.insets.bottom * boundingBoxSize.y : 0
            };

            boundingBoxSize.x -= glyphInsets.left + glyphInsets.right;
            boundingBoxSize.y -= glyphInsets.top + glyphInsets.bottom;

            glyphOffset.x += glyphInsets.left;
            glyphOffset.y += glyphInsets.top;

            var glyphProps = glyph.render(design, glyphObject, boundingBoxSize);

            var glyph = glyphProps.glyph;
            var backboneOffset = glyphProps.backboneOffset;

            var glyphMatrix = Matrix();
            var labelMatrix = Matrix();

            glyphMatrix = Matrix.translate(glyphMatrix, Vec2(0, -backboneOffset));

            glyphMatrix = Matrix.translate(glyphMatrix, Vec2(glyphOffset, 0));

            if(glyphObject.strand === 'negative') {

                glyphMatrix = Matrix.translate(glyphMatrix, Vec2(0, backboneOffset));
                glyphMatrix = Matrix.rotate(glyphMatrix, 180, Vec2(boundingBoxSize.x, 0));
                glyphMatrix = Matrix.translate(glyphMatrix, Vec2(boundingBoxSize.x, 0));
                glyphMatrix = Matrix.translate(glyphMatrix, Vec2(0, -backboneOffset));
            }

            glyph.transform({ matrix: Matrix.toSVGString(glyphMatrix) });


            var glyphBBox = Rect(glyph.bbox());

            var labelText;

            if(glyphObject.name !== undefined) {

                labelText = design.surface.text('');

                labelText.font({ anchor: 'middle' });

                labelText.build(true);

                var label = labelText.tspan(glyphObject.name);

                label.attr('alignment-baseline', 'middle');

                labelText.build(false);

                var labelPos = Vec2(
                    glyphOffset + glyphBBox.topLeft.x + Rect.width(glyphBBox) / 2.0,
                    
                    glyphObject.strand === 'negative' ?
                        backboneOffset + design.geom.labelOffset :
                        0 - backboneOffset - design.geom.labelOffset);

                labelMatrix = Matrix.translate(labelMatrix, labelPos);

                labelText.transform({ matrix: Matrix.toSVGString(labelMatrix) });
            }


            glyphOffset += boundingBoxSize.x;
            glyphOffset += design.geom.glyphPadding;

            var group = surface.group().add(glyph);
            
            if(labelText !== undefined)
                group.add(labelText);

            glyph.displayList = glyphObject;
            glyphObject.svg = group;

            glyphObject.arcEdge =
                glyphObject.strand === 'negative' ?
                    'bottom' : 'top';

            return group;
        });


        var backbone = surface.line(-design.geom.segmentPadding,
                                    0,
                                    glyphOffset,
                                    0);

        backbone.attr('stroke', segment.color || 'black');
        backbone.attr('stroke-width', segment.thickness || '2px');

        var group = surface.group();

        glyphs.forEach(group.add.bind(group));
        glyphs.forEach(design.addObstacle.bind(design));

        group.add(backbone);
        backbone.back();

        if(segment.name !== undefined) {

            var labelText = design.surface.text('');
            labelText.font({ anchor: 'left' });
            labelText.build(true);

            var label = labelText.tspan(segment.name);

            label.attr('alignment-baseline', 'middle');
            labelText.build(false);

            labelText.transform({
                matrix: Matrix.toSVGString
                    (Matrix.translation(Vec2(-100, -100))) });

            group.add(labelText);
        }

        group.displayList = segment;
        segment.svg = group;

        return group;
    }

    return {

        render: renderSegment

    };
});


