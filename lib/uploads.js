
const tmp = require('tmp-promise')
const fs = require('mz/fs')
const zlib = require('zlib')
const shastream = require('sha1-stream')
const filetype = require('file-type')
const mkdirp = require('mkdirp-promise')
const assert = require('assert')
const streamMeter = require('stream-meter')
const stream = require('stream')
const mv = require('mv')

function createUpload(inputStream) {

    console.log('Creating upload...')

    return tmp.file({

        discardDescriptor: true

    }).then((tempFile) => {

        const tempFileName = tempFile.path
        const gzip = zlib.createGzip()

        const writeStream = fs.createWriteStream(tempFileName)

        const fileStream = inputStream.pipe(new stream.PassThrough());

        var mimeType

        fileStream.on('data', chunk => {
            let type = filetype(chunk)
            console.log(type)
            if (type) {
                mimeType = "http://purl.org/NET/mediatypes/" + type["mime"]
            } else {
                mimeType = 'http://purl.org/NET/mediatypes/text/plain'
            }
        })

        console.log('Created temp file at ' + tempFileName)

        const shaStream = shastream.createStream('sha1')

        var hash

        shaStream.on('digest', (_hash) => {
            hash = _hash
        })

        const meter = streamMeter()

        return awaitFinished(
            inputStream.pipe(meter).pipe(shaStream).pipe(gzip).pipe(writeStream)
        ).then(() => {

            assert(hash !== undefined)

            console.log('Calculated hash: ' + hash)
            console.log('File size: ' + meter.bytes + ' bytes')

            const { filename } = getPaths(hash)

            return doesFileExist(filename).then((exists) => {

                if (exists) {

                    console.log('Upload already exists')

                    tempFile.cleanup()

                    return Promise.resolve({
                        hash: hash,
                        size: meter.bytes,
                        mime: mimeType
                    })

                } else {

                    console.log('Upload does not already exist')

                    const { hashPrefix, dir, filename } = getPaths(hash)

                    console.log('Moving file to ' + filename)

                    return mkdirp(dir).then(() => {

                        mv(tempFileName, filename, { mkdirp: true }, function (err) {
                            console.log('MV ERROR:' + err)
                        });

                        return Promise.resolve();

                    }).then(() => {

                        return Promise.resolve({
                            hash: hash,
                            size: meter.bytes,
                            mime: mimeType
                        })

                    })
                }
            })
        })

    })

    function awaitFinished(stream) {

        return new Promise((resolve, reject) => {

            stream.on('finish', () => {
                resolve()
            })

            stream.on('error', (err) => {
                reject(err)
            })

        })

    }

    function doesFileExist(filename) {

        return new Promise((resolve, reject) => {

            fs.stat(filename).then((stat) => {

                resolve(true)

            }).catch((err) => {

                if (err.code === 'ENOENT') {
                    resolve(false)
                } else {
                    reject(err)
                }

            })

        })

    }
}

function createUncompressedReadStream(hash) {

    const gunzip = zlib.createGunzip()

    const { filename } = getPaths(hash)

    return fs.createReadStream(filename).pipe(gunzip)

}

function createCompressedReadStream(hash) {

    const gunzip = zlib.createGunzip()

    const { filename } = getPaths(hash)

    return fs.createReadStream(filename)

}

function getPaths(hash) {

    const hashPrefix = hash.slice(0, 2)
    const dir = './uploads/' + hashPrefix
    const filename = dir + '/' + hash.slice(2) + '.gz'

    return {
        hashPrefix: hashPrefix,
        dir: dir,
        filename: filename
    }
}

module.exports = {
    createUpload: createUpload,
    createCompressedReadStream: createCompressedReadStream,
    createUncompressedReadStream: createUncompressedReadStream,
}

