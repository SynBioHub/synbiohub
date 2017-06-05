const config = require('../../config')
const extend = require('xtend')

module.exports = function (req, res) {
    var create = true;
    var remotes = config.get('remotes')
    var remote = reqToRemote(req);

    remotes[remote.id] = remote;

    config.set('remotes', remotes)

    res.redirect('/admin/remotes')
}

function addRemote(remote, remotes) {
    return remotes.append(remote)
}

function updateRemote(remote, remotes) {
    
}

function reqToRemote(req) {
    console.log(req.body)
    if (req.body.type == 'ice' || true) {
        return reqToIce(req)
    } else if (req.body.type == 'benchling') {
        return reqToBenchling(req)
    }
}

function reqToBenchling(req) {
    return {
        "id": req.body.id,
        "type": req.body.type,
        "url": req.body.url,
        "X-BENCHLING-API-Token": req.body.benchlingApiToken,
        "rejectUnauthorized": req.body.rejectUnauthorized,
        "folderPrefix": req.body.folderPrefix,
        "sequenceSuffix": req.body.sequenceSuffix,
        "defaultFolderId": req.body.defaultFolderId,
    }
}

function reqToIce(req) {
    return {
        "id": req.body.id,
        "type": req.body.type,
        "url": req.body.url,
        "X-BENCHLING-API-Token": req.body.benchlingApiToken,
        "rejectUnauthorized": req.body.rejectUnauthorized,
        "folderPrefix": req.body.folderPrefix,
        "sequenceSuffix": req.body.sequenceSuffix,
        "defaultFolderId": req.body.defaultFolderId,
        "groupId": req.body.groupId,
        "PI": req.body.pi,
        "PIemail": req.body.piEmail,
        "X-ICE-API-Token-Client": req.body.iceApiTokenClient,
        "X-ICE-API-Token": req.body.iceApiToken,
        "X-ICE-API-Token-Owner": req.body.iceApiTokenOwner,
        "iceCollection": req.body.iceCollection, 
    }
}
