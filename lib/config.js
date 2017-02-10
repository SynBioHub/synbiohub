
var fs = require('fs')
var extend = require('xtend')

var path = 'config.json'
var localPath = 'config.local.json'

var config = extend(
    JSON.parse(fs.readFileSync(path) + ''),
    JSON.parse(fs.readFileSync(localPath) + '')
)

module.exports = {

    get: function configGet(key) {

        if(arguments.length === 0)
            return config

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

