const fs = require('fs')

module.exports = function() {
    var fileName = "./.git/refs/heads/master"

    try {
        var fileContents = fs.readFileSync(fileName)
        return fileContents.toString().substring(0, 8)
    } catch (err) {
        return ""
    }
}
