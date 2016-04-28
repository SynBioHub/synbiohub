
define([ './vec2' ], function(Vec2) {

    function Matrix() {

        return [
            1, 0, 0,
            0, 1, 0
        ];
    }

    Matrix.translation = function translation(vector) {

        return [
            1, 0, vector.x,
            0, 1, vector.y
        ];
    }

    Matrix.rotation = function rotation(angle, origin) {

        angle = angle * (Math.PI / 180.0);

        var rotation = [
            Math.cos(angle), Math.sin(angle), 0,
            -Math.sin(angle), Math.cos(angle), 0
        ];

        if(origin !== undefined) {

           return Matrix.multiply(
                       Matrix.translation(origin),
                       rotation,
                       Matrix.translation(Vec2.multiply(origin, -1)));

        } else {
            
            return rotation;

        }
    }

    Matrix.translate = function translate(matrix, vector) {

        return Matrix.multiply(matrix, Matrix.translation(vector));

    }

    Matrix.rotate = function rotate(matrix, angle, origin) {

        return Matrix.multiply(matrix, Matrix.rotation(angle, origin));

    }

    Matrix.multiply = function multiply(a, b) {

        var matrices = Array.prototype.slice.call(arguments, 0);

        var m = matrices.reduce(function(a, b) {

            return [
                a[0] * b[0] + a[1] * b[3],
                a[0] * b[1] + a[1] * b[4],
                a[0] * b[2] + a[1] * b[5] + a[2],
                a[3] * b[0] + a[4] * b[3],
                a[3] * b[1] + a[4] * b[4],
                a[3] * b[2] + a[4] * b[5] + a[5]
            ];

        }, Matrix());

        return m;
    }

    Matrix.toSVGString = function toSVGString(matrix) {

        return [

            matrix[0], matrix[3],
            matrix[1], matrix[4],
            matrix[2], matrix[5]
            
        ].join(',');
    }

    return Matrix;
});






