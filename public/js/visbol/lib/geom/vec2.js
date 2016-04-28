
define([], function() {

    function Vec2(x, y)
    {
        if(arguments.length === 0) {

            return { x: 0, y: 0 };

        }

        if(typeof(x) == 'number') {

            return {
                x: x,
                y: arguments.length > 1 ? y : x
            };

        }

        if({}.hasOwnProperty.call(x, 'x')) {
            return { x: x.x, y: x.y };
        }

        return { x: 0, y: 0 };
    }

    Vec2.add = function add(a, b) {

        return typeof(b) === 'number' ?
                 Vec2(a.x + b, a.y + b) :
                 Vec2(a.x + b.x, a.y + b.y);

    }

    Vec2.subtract = function subtract(a, b) {

        return typeof(b) === 'number' ?
                 Vec2(a.x - b, a.y - b) :
                 Vec2(a.x - b.x, a.y - b.y);
    }

    Vec2.multiply = function multiply(a, b) {

        return typeof(b) === 'number' ?
                 Vec2(a.x * b, a.y * b) :
                 Vec2(a.x * b.x, a.y * b.y);
    }

    Vec2.min = function min(a, b) {
        
        return Vec2(Math.min(a.x, b.x), Math.min(a.y, b.y));

    }

    Vec2.max = function max(a, b) {
        
        return Vec2(Math.max(a.x, b.x), Math.max(a.y, b.y));

    }

    Vec2.abs = function abs(vector) {
        
        return Vec2(Math.abs(vector.x), Math.abs(vector.y));

    }

    Vec2.difference = function difference(a, b) {

        return Vec2.abs(Vec2.subtract(a, b));

    }

    Vec2.toPathString = function toPathString(v) {

        return v.x + ',' + v.y;

    };

    Vec2.direction = function direction(a, b) { 

        if(b.x > a.x) {

            return Vec2(1, 0);

        } else if(b.x < a.x) {

            return Vec2(-1, 0);

        } else if(b.y > a.y) {

            return Vec2(0, 1);

        } else if(b.y < a.y) {

            return Vec2(0, -1);

        } else {

            return Vec2(0, 0);

        }
        
    }

    Vec2.equals = function equals(a, b) {

        return a.x === b.x && a.y === b.y;


    }

    return Vec2;
});






