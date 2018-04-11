const pug = require('pug')
const { fetchSBOLSource } = require('../fetch/fetch-sbol-source')
const serializeSBOL = require('../serializeSBOL')
const buildCombineArchive = require('../buildCombineArchive')
const config = require('../config')
const getUrisFromReq = require('../getUrisFromReq')
const fs = require('mz/fs')

module.exports = function (req, res) {

    req.setTimeout(0) // no timeout

    const { graphUri, uri, designId, share } = getUrisFromReq(req, res)

    var fileName
    var archiveName

    fetchSBOLSource(uri, graphUri).then(sbolFilename => {
	fileName = sbolFilename
	console.log("sbol file for archive:" + sbolFilename)
        return buildCombineArchive(sbolFilename, []);
    }).then(result => {
        archiveName = result.resultFilename;
        var stat = fs.statSync(archiveName);
	console.log("creating archive:" + archiveName)

        res.writeHead(200, {'Content-Type': 'application/zip', 'Content-Length': stat.size})

        var readStream = fs.createReadStream(archiveName)

        readStream.pipe(res)
            .on('finish', () => {
		console.log('finish download of combine archive')
            })

    }).then(() => {
	console.log("unlinking:"+fileName)
	fs.unlink(fileName)
	console.log("unlinking:"+archiveName)
        fs.unlink(archiveName)
    }).catch((err) => {
        if (req.url.endsWith('/sbol')) {
            return res.status(404).send(uri + ' not found')
        } else {
            var locals = {
                config: config.get(),
                section: 'errors',
                user: req.user,
                errors: [err.stack]
            }
            res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
        }
    })
};


