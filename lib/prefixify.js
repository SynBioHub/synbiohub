
function prefixify(uri, prefixes) {

    var prefixNames = Object.keys(prefixes)

    for(var i = 0; i < prefixNames.length; ++ i) {

        var prefixName = prefixNames[i]
        var prefixUri = prefixes[prefixName]

        if(uri.indexOf(prefixUri) === 0) {

            return {
                prefix: prefixName,
                uri: uri.slice(prefixUri.length)
            }

        }
    }

    return {
        prefix: '',
        uri: uri
    }
}

module.exports = prefixify

