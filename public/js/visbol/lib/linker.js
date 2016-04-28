
define(['./svg',
        './component',
        './geom/rect',
        './geom/vec2',
        './grid' ],
        
function(SVG, Component, Rect, Vec2, Grid) {

function link(surface, obstacles, arcs, opts)
{
    opts = opts || {};

    opts = {

        cellSize: opts.cellSize || 8,

        offset: Vec2(0, 0),
        
        arcDistance: opts.arcDistance || 32,
        arcEndLength: opts.arcEndLength || 32,
        arcTurnLength: opts.arcTurnLength || 20,

    };

    var boundingBoxes = [];

    function createBoundingBox(component) {

        var box = Rect(component.bbox());

        box.topLeft = Vec2.add(box.topLeft, opts.offset);
        box.bottomRight = Vec2.add(box.bottomRight, opts.offset);

        /* Use the offset to prevent negative coordinates (which the grid
         * cannot cope with)
         */
        var deltaOffset = Vec2();

        if(box.topLeft.x < 0)
            deltaOffset.x = Math.abs(box.topLeft.x);

        if(box.topLeft.y < 0)
            deltaOffset.y = Math.abs(box.topLeft.y);

        boundingBoxes.push(box);

        adjustOffset(deltaOffset);

        return box;
    }

    function adjustOffset(delta) {

        boundingBoxes.forEach(function(box) {

            box.topLeft = Vec2.add(box.topLeft, delta);
            box.bottomRight = Vec2.add(box.bottomRight, delta);

        });

        opts.offset = Vec2.add(opts.offset, delta);
    }

    obstacles = obstacles.map(createBoundingBox);

    arcs = arcs.map(function(arc) {

        return {

            arc: arc,

            start: extend(createBoundingBox(arc.start.component), {
                direction: arc.start.direction || Vec2(0, -1)
            }),

            end: extend(createBoundingBox(arc.end.component), {
                direction: arc.end.direction || Vec2(0, -1)
            }),

            style: extend(arc.style, {
                fill: 'none'
            })
        };
    });

    /* Leave some room for growth, as the arcs may be outside of the bounds
     * of the glyphs.
     */
    var boundingBox = Rect.expand(getBounds(boundingBoxes), 100);

    if(opts.rearrange === true)
        arrangeComponents(surface, boundingBox, obstacles, arcs, opts);

    return routeArcs(surface, boundingBox, obstacles, arcs, opts);
}

function arrangeComponents(surface, boundingBox, obstacles, arcs, opts)
{
    /* TODO: Re-arrange the components into an optimal configuration for the
     * arcs to be drawn.
     */
}

function routeArcs(surface, boundingBox, obstacles, arcs, opts)
{
    var grid = new Grid(Rect.size(boundingBox),
                        Vec2.abs(boundingBox.topLeft),
                        opts.cellSize);

    function getEdgeMidPoint(box, direction) { 

        return Vec2.add(Rect.center(box),
                    Vec2.multiply(Rect.size(box),
                        Vec2.multiply(direction, 0.5)));
    }

    function pointToPathCoord(point) {

        return Vec2.toPathString(Vec2.subtract(point, opts.offset));

    }

    return arcs.map(function(arc) {

        /* The arc always starts travelling perpendicular to the edge it's
         * leaving.
         */
        var startPoint = Vec2.add(getEdgeMidPoint(arc.start, arc.start.direction),
                                        Vec2.multiply(arc.start.direction, opts.arcDistance)),

            endPoint = Vec2.add(getEdgeMidPoint(arc.end, arc.end.direction),
                                    Vec2.multiply(arc.end.direction, opts.arcDistance));

        var startPos = grid.pointToGridCoord(startPoint),
            endPos = grid.pointToGridCoord(endPoint);

        var arcEndGridLength = Math.round(opts.arcEndLength / opts.cellSize);
        var arcTurnGridLength = Math.round(opts.arcTurnLength / opts.cellSize);

        var arcBeginPos =
            Vec2.add(startPos, Vec2.multiply(arc.start.direction, arcEndGridLength));

        var goals = [
            grid.at(arcBeginPos),
            grid.at(Vec2(endPos.x, arcBeginPos.y)),
            grid.at(endPos)
        ];

        var start = grid.at(startPos);

        var path = [
            'M' + pointToPathCoord(grid.gridCoordToPoint(start.position))
        ];

        grid.markObstacles(obstacles);

        while(goals.length > 0) {

            var subpath = pathfind(grid, start, goals[0], opts);

            path = path.concat(subpath.map(function(point) {

                return 'L' + pointToPathCoord(point);

            }));

            start = goals[0];
            goals = goals.slice(1);

            grid.clear();
        }

        var svgPath = surface.path(path.join('\n'));

        svgPath.attr(arc.style);

        if(arc.style.cap === 'arrow') {

            svgPath.marker('end', 10, 10, function(add) {
                add.path('M4,0L10,5L4,10z').stroke('black').fill('black');
            });

        } else if(arc.style.cap === 'line') {

            svgPath.marker('end', 10, 10, function(add) {
                add.path('M5,0L5,10z').stroke('black').fill('none');
            });
        }

        return {
            arc: arc,
            path: svgPath
        };
    });

}


function pathfind(grid, start, end, opts) {

    while(start.obstacle) {

        start = grid.at(Vec2(start.position.x, start.position.y - 1));

    }

    while(end.obstacle) {

        end = grid.at(Vec2(end.position.x, end.position.y - 1));

    }

    if(start.obstacle || end.obstacle)
        throw new Error('Cannot start or end inside an obstacle');

    var openSet = [];

    function removeNodeFromOpenSet(node) {

        node.state = 'closed';

        var index = openSet.indexOf(node);

        openSet.splice(index, 1);
    }

    function addNodeToOpenSet(node) {

        node.state = 'open';

        for(var i = 0; i < openSet.length; ++ i) {

            if(openSet[i].fScore > node.fScore) {

                openSet.splice(i, 0, node);
                return;

            }

        }

        openSet.push(node);
    }



    start.state = 'closed';

    adjacent(start).forEach(function(candidate) {

        var node = candidate.node,
            cost = candidate.cost,
            direction = candidate.direction;

        if(node.state !== 'closed') {

            node.parent = start;
            node.gScore = cost;
            node.fScore = node.gScore + manhattanDistance(node.position, end.position);

            addNodeToOpenSet(node);

        }

    });

    while(true) {

        if(openSet.length === 0) {

            /* open list empty
             */

            console.warn('No route found!  Cannot route arc');
            return [];
        }

        var bestNode = openSet.shift();


        bestNode.state = 'closed';

        if(bestNode === end) {

            return createPath(grid, start, end, opts);
        }

        adjacent(bestNode).forEach(function(candidate) {

            var node = candidate.node,
                cost = candidate.cost,
                direction = candidate.direction;

            var numberOfTurns = bestNode.numberOfTurns;

            if(!Vec2.equals(bestNode.directionFromParent, direction))
                ++ numberOfTurns;

            if(node.state !== 'closed') {

                var gScore = bestNode.gScore + cost,
                    fScore = gScore + manhattanDistance(node.position, end.position);

                if(node.state === 'open') {

                    if(gScore < node.gScore) {

                        removeNodeFromOpenSet(node);

                    } else {

                        return;
                    }

                }

                node.parent = bestNode;
                node.directionFromParent = direction;
                node.numberOfTurns = numberOfTurns;
                node.gScore = gScore;
                node.fScore = fScore;

                addNodeToOpenSet(node);
            }
        });
    }

    function adjacent(node) {

        var adjacent = [];

        function addAdjacent(direction) {

            adjacent.push({
                node: grid.at(Vec2.add(node.position, direction)),
                cost: 10,
                direction: direction
            });
        }

        if(node.position.x > 0)
            addAdjacent(Vec2(-1, 0));

        if(node.position.x < grid.size.x - 1)
            addAdjacent(Vec2(1, 0));

        if(node.position.y > 0)
            addAdjacent(Vec2(0, -1));

        if(node.position.y < grid.size.y - 1)
            addAdjacent(Vec2(0, 1));

        return adjacent;
    };
}

function createPath(grid, start, end, opts) {

    var gridRoute = [];

    for(; end; end = end.parent) {

        if(end === start)
            break;

        gridRoute.unshift(end);

    }

    var startPoint = grid.gridCoordToPoint(start.position),
        endPoint = grid.gridCoordToPoint(end.position);

    var curPoint = Vec2(startPoint);

    return gridRoute.map(function(node) {
        
        return grid.gridCoordToPoint(node.position);

    });

}

function manhattanDistance(a, b) {

   return Math.abs(b.x - a.x) + Math.abs(b.y - a.y);
}

function extend(a, b) {

    for(var prop in b)
        if({}.hasOwnProperty.call(b, prop))
            a[prop] = b[prop];

    return a;
}


function getBounds(rects) {

    return rects.reduce(function(prev, cur) {

        return {
            topLeft: Vec2.min(prev.topLeft, cur.topLeft),
            bottomRight: Vec2.max(prev.bottomRight, cur.bottomRight)
        };

    });
}


return {
    link: link
};

});

