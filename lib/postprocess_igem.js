
var cheerio = require('cheerio')

function postprocess_igem(html) {

	var $ = cheerio.load(html)

	$('#sequencePaneDiv').remove()
	$('script').remove()

	return $.html()
}

module.exports = postprocess_igem
