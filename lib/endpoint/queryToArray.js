
function queryToArray(q) {

    var arr = []

    Object.keys(q).forEach(function(i) {

        arr[i] = q[i]

    })

    return arr
}

module.exports = queryToArray

