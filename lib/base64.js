
function encode(str) {
    return (new Buffer('' + str).toString('base64')).replace(/=/g, '')
}

function decode(str) {
    return (new Buffer('' + str, 'base64')).toString('ascii')
}

module.exports = {
    encode: encode,
    decode: decode
}

