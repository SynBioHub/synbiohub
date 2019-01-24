
const config = require('./config')

var sha1 = require('sha1')

function shareImages (req, wikiText) {
  if (req.url.toString().endsWith('/share') && wikiText) {
    // const regex = /<img src=\"\/user\/([^/]*)\/([^/]*)\/attachment_([^/]*)\/([^/]*)\/download\">/g;
    // const subst = `<img src="/user/$1/$2/attachment_$3/$4/download/hash/share">`;
    // console.log(metaData.mutableDescription.replace(regex,subst))
    const regex = /<img src=\"\/user\/[^/]*\/[^/]*\/attachment_[^/]*\/[^/]*\/download\">/g

    var matches = wikiText.match(regex) || []

    for (i = 0; i < matches.length || 0; i++) {
      var imgUri = matches[i].replace('<img src=\"\/', '')
      imgUri = imgUri.replace('/download\">', '')
      var hash = sha1('synbiohub_' + sha1(config.get('databasePrefix') + imgUri) + config.get('shareLinkSalt'))
      var subst = '<img src=\"\/' + imgUri + '/' + hash + '/share/download\">'
      wikiText = wikiText.replace(matches[i], subst)
    }
  }

  return wikiText
}

module.exports = shareImages
