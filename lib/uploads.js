
const tmp = require('tmp-promise')
const fs = require('mz/fs')
const zlib = require('zlib')
const shastream = require('sha1-stream')
const mkdirp = require('mkdirp-promise')
const assert = require('assert')

function createUpload(inputStream) {

    console.log('Creating upload...')

    return tmp.file({

        discardDescriptor: true

    }).then((tempFile) => {

        const tempFileName = tempFile.path
        const gzip = zlib.createGzip()

        const writeStream = fs.createWriteStream(tempFileName)

        console.log('Created temp file at ' + tempFileName)

        const shaStream = shastream.createStream('sha1')

        var hash

        shaStream.on('digest', (_hash) => {
            hash = _hash
        })

        return awaitFinished(
            inputStream.pipe(shaStream).pipe(gzip).pipe(writeStream)
        ).then(() => {

            assert(hash !== undefined)

            console.log('Calculated hash: ' + hash)

            const { filename } = getPaths(hash)

            return doesFileExist(filename).then((exists) =>  {

                if(exists) {

                    console.log('Upload already exists')

                    tempFile.cleanup()

                    return Promise.resolve(hash)

                } else {

                    console.log('Upload does not already exist')

                    const { hashPrefix, dir, filename } = getPaths(hash)

                    console.log('Moving file to ' + filename)

                    return mkdirp(dir).then(() => {

                        return fs.rename(tempFileName, filename)

                    }).then(() => {

                        return Promise.resolve(hash)

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

                if(err.code === 'ENOENT') {
                    resolve(false)
                } else {
                    reject(err)
                }

            })

        })

    }
}

function createReadStream(hash) {

    const gunzip = zlib.createGunzip()

    const { filename } = getPaths(hash)

    return fs.createReadStream(filename).pipe(gunzip)

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
    createReadStream: createReadStream
}

