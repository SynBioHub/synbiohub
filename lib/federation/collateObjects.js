
var extend = require('xtend')

function collateObjects(results, res) {

    var collatedObject = {}

    /* merge in reverse order so local object takes priority
     */
    results.reverse()

    results.forEach((result) => {

        var object = JSON.parse(result.body)

        collatedObject = extend(collatedObject, object)

    })
    
    res.header('content-type', 'application/json')
    res.send(JSON.stringify(collatedObject))
}

module.exports = collateObjects



