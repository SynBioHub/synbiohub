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

    fetchSBOLSource(uri, graphUri).then(sbolFilename => {
        return buildCombineArchive(sbolFilename, []);
    }).then(result => {
        var archiveName = result.resultFilename;
        var stat = fs.statSync(archiveName);

        res.writeHead(200, {'Content-Type': 'application/zip', 'Content-Length': stat.size})

        var readStream = fs.createReadStream(archiveName)

        readStream.pipe(res)
            .on('finish', () => {
                fs.unlink(archiveName)
            })

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


