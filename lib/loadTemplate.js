
var fs = require('fs')

module.exports = function loadTemplate(filename, args) {

    var template = fs.readFileSync(filename) + '';

    Object.keys(args).forEach(function(key) {
        template = template.replace(new RegExp('\\$' + key, 'g'), args[key])
    })

    return template;
}


