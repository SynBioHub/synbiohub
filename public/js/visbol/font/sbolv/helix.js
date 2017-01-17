
define([ 'visbol' ], function(visbol) {

    var Vec2 = visbol.Vec2,
        Rect = visbol.Rect,
        Matrix = visbol.Matrix;

    function renderGlyph(design, glyphObject, boxSize) {

        var paths = createPaths(design, glyphObject, boxSize.y)

        var group = design.surface.group();

        paths.forEach((path) => group.add(path))

        if(glyphObject.uri)
            group.attr('data-uri', glyphObject.uri);

        return {
            glyph: group,
            backboneOffset: boxSize.y
        };
    }

    return {

        render: renderGlyph

    };
});


function createPaths(design, glyphObject, reqSize) {

    const origSize = 891.1

    const strand1Path = [
            'M', size(594.6), ' ', size(828.7),
            'c', size(-86.5), ' ', size(-154.5), ' ', size(75.5), ' ', size(-330.6), ' ', size(1.7), ' ', size(-472.5),
            'c', size(-96.7), ' ', size(-185.8), ' ', size(-411.6), ' ', size(33.9), ' ', size(-555.8), ' ', size(-74.4),
    ].join('')

    const strand2Path = [
        'M', size(291.8), ' ', size(64.3),
        'c', size(79.8), ' ', size(163.4), ' ', size(-125.6), ' ', size(431.4), ' ', size(70.3), ' ', size(525.4),
        'c', size(154.6), ' ', size(74.2), ' ', size(329.4), ' ', size(-56.7), ' ', size(483.9), ' ', size(20)
    ].join('')

    const lineCoords = [
        { x1:298.7, y1:80.8, x2:77.2, y2:302.3 },
        { x1:315.2, y1:157.8, x2:164.9, y2:308.1 },
        { x1:430.9, y1:278.4, x2:280.6, y2:428.7 },
        { x1:506.1, y1:293.9, x2:290.8, y2:509.2 },
        { x1:571.1, y1:324.9, x2:328.1, y2:567.9 },
        { x1:610.6, y1:378.2, x2:395.3, y2:593.5 },
        { x1:625.6, y1:453.9, x2:475.2, y2:604.2 },
        { x1:716.9, y1:588.9, x2:566.6, y2:739.2 },
        { x1:802.2, y1:593.5, x2:580.7, y2:815.1 }
    ]

    const lines = lineCoords.map((lineCoord) => {

        const line = design.surface.line()

        line.attr('x1', size(lineCoord.x1))
        line.attr('y1', size(lineCoord.y1))
        line.attr('x2', size(lineCoord.x2))
        line.attr('y2', size(lineCoord.y2))
        line.attr('stroke-width', '1')

        return line

    })


    const strand1 = design.surface.path(strand1Path)

    strand1.attr('fill', 'none');
    strand1.attr('stroke', '#1D71B8')
    strand1.attr('stroke-width', '3')
    strand1.attr('stroke-linecap', 'round');
    strand1.attr('stroke-miterlimit', 10)

    const strand2 = design.surface.path(strand2Path)

    strand2.attr('fill', 'none');
    strand2.attr('stroke', '#C6C6C6')
    strand2.attr('stroke-width', '3')
    strand2.attr('stroke-linecap', 'round');
    strand2.attr('stroke-miterlimit', 10)

    return lines.concat([
        strand1,
        strand2
    ])

    function size(n) {

        return ((n / origSize) * reqSize) + ''

    }
}



