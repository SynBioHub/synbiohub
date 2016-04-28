
define([ './vec2' ], function(Vec2) {

    function Rect(topLeft, bottomRight)
    {
        /* getBoundingClientRect format? 
         */
        if(topLeft.left !== undefined) {
            return {
                topLeft: Vec2(
                    topLeft.left,
                    topLeft.top
                ),
                bottomRight: Vec2(
                    topLeft.right,
                    topLeft.bottom
                )
            };
        }


        /* SVG getBBox format?
         */
        if(topLeft.width !== undefined) {
            return {
                topLeft: Vec2(
                    topLeft.x,
                    topLeft.y
                ),
                bottomRight: Vec2(
                    topLeft.x + topLeft.width,
                    topLeft.y + topLeft.height
                )
            };
        }

        return {
            topLeft: Vec2(topLeft.x, topLeft.y),
            bottomRight: Vec2(bottomRight.x, bottomRight.y)
        };
    }

    Rect.width = function width(rect) {
        return rect.bottomRight.x - rect.topLeft.x;
    };

    Rect.height = function height(rect) {
        return rect.bottomRight.y - rect.topLeft.y;
    };

    Rect.size = function size(rect) {
        return Vec2.subtract(rect.bottomRight, rect.topLeft);
    }

    Rect.clone = function clone(rect) {
        return Rect(rect.topLeft, rect.bottomRight);
    };

    Rect.area = function area(rect) {
        return Rect.width(rect) * Rect.height(rect);
    };

    Rect.expand = function contract(rect, amount) {

        return Rect(Vec2.subtract(rect.topLeft, amount),
                    Vec2.add(rect.bottomRight, amount));

    };

    Rect.contract = function contract(rect, amount) {

        return Rect(Vec2.add(rect.topLeft, amount),
                    Vec2.subtract(rect.bottomRight, amount));

    };

    Rect.move = function move(rect, delta) {

        return Rect(Vec2.add(rect.topLeft, delta),
                    Vec2.add(rect.bottomRight, delta));

    }

    Rect.toString = function toString(rect) {

        return JSON.stringify(rect);

    }

    Rect.center = function center(rect) {

        return Vec2.add(rect.topLeft, Vec2.multiply(Rect.size(rect), 0.5));

    }

    return Rect;
});






