
define([

    './svg',
    './design',
    './geom/vec2',
    './geom/rect',
    './geom/matrix'

], function(SVG, Design, Vec2, Rect, Matrix) {

    console.log('visbol dependencies loaded');

    return {

        SVG: SVG,

        Design: Design,

        Vec2: Vec2,
        Rect: Rect,
        Matrix: Matrix
    };
});



