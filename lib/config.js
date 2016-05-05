
var fs = require('fs')
var extend = require('xtend')

var path = 'config.json'

var config = JSON.parse(fs.readFileSync(path) + '')

module.exports = {

    get: function configGet(key) {

        return config[key]

    },

    set: function configSet(key, value) {

        console.log('config: set ' + key + ' => ' + value)

        config = extend(config, {

            [key]: clone(value)

        })

        fs.writeFileSync(path, JSON.stringify(config, null, 2))
    }

}


function clone(val) {

    return JSON.parse(JSON.stringify(val))

}

