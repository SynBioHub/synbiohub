
const { fetchCollectionFASTA } = require('../fetch/fetch-collection-fasta')

var getUrisFromReq = require('../getUrisFromReq')

module.exports = function (req, res) {
  const { uri } = getUrisFromReq(req, res)

  return fetchCollectionFASTA(uri, req.user).then((fasta) => {
    res.status(200)
    res.header('Content-Disposition', 'attachment; filename="' + req.params.displayId + '.fasta"')
    res.header('Access-Control-Expose-Headers', 'Content-Disposition')
    res.header('content-type', 'text/plain').send(fasta)
  }).catch((err) => {
    res.status(422).send(err.stack)
  })
}
