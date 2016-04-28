
define([ './geom/rect',
         './geom/vec2',
         './geom/matrix'
], 

function(Rect, Vec2, Matrix) {

    function Grid(surfaceSize, offset, cellSize) {
            
        this.offset = Vec2(offset);

        this.cellSize = cellSize;

        this.size = Vec2(Math.ceil(surfaceSize.x / cellSize) + 1,
                         Math.ceil(surfaceSize.y / cellSize) + 1);

        this.grid = Array(this.size.y);

        for(var y = 0; y < this.size.y; ++ y) {

            this.grid[y] = Array(this.size.x);

            for(var x = 0; x < this.size.x; ++ x) {

                this.grid[y][x] = new Grid.Cell(Vec2(x, y));
            }
        }
    }

    Grid.prototype = {

        pointToGridCoord: function pointToGridCoord(point) {

            return Vec2(Math.floor((this.offset.x + point.x) / this.cellSize),
                        Math.floor((this.offset.y + point.y) / this.cellSize));

        },

        gridCoordToPoint: function gridCoordToPoint(coord) {

            return Vec2.subtract(Vec2.add(Vec2.multiply(coord, this.cellSize),
                                      this.cellSize * 0.5), this.offset);
        },

        at: function at(coord) {

            return this.grid[coord.y][coord.x];

        },

        markObstacles: function markObstacles(rects) {

            for(var y = 0; y < this.size.y; ++ y) {

                for(var x = 0; x < this.size.x; ++ x) {

                    var cell = this.grid[y][x];
                    var cellPos = this.gridCoordToPoint(cell.position);

                    cell.obstacle = false;

                    for(var i = 0; i < rects.length; ++ i)
                    {
                        var rect = rects[i];

                        if(rect.bottomRight.y >= cellPos.y &&
                           rect.topLeft.y <= cellPos.y + this.cellSize &&
                           rect.bottomRight.x >= cellPos.x &&
                           rect.topLeft.x <= cellPos.x + this.cellSize) {

                            cell.obstacle = true;
                            cell.state = 'closed';
                        }
                    }
                }
            }
        },

        clear: function clear() {

            for(var y = 0; y < this.size.y; ++ y)
                for(var x = 0; x < this.size.x; ++ x)
                    this.grid[y][x].clear();
        }

    };

    Grid.Cell = function(position) {

        this.position = Vec2(position);
        this.clear();

    };

    Grid.Cell.prototype = {

        toString: function toString() {

            return 'GridCell@' + this.position.x + ',' + this.position.y;

        },

        clear: function clear() {

            this.gScore = 0;
            this.parent = null;
            this.state = '';
            this.directionFromParent = Vec2(0, 0);
            this.numberOfTurns = 0;
        }
    };

    function extend(a, b) {

        for(var prop in b)
            if({}.hasOwnProperty.call(b, prop))
                a[prop] = b[prop];

        return a;
    }

    return Grid;
});


