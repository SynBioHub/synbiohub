


define([ './svg',
         './geom/rect',
         './geom/vec2',
         './geom/matrix'
], 

function(SVG, Rect, Vec2, Matrix) {

    function renderProcess(design, process) {

        var group = design.surface.group();

        group.add(design.surface.rect(
            design.geom.processBoxSize.x,
            design.geom.processBoxSize.y));

        group.displayList = process;
        process.svg = group;

        return group;
    }

    return {

        render: renderProcess

    };
});


