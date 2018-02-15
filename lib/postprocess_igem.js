
var cheerio = require('cheerio')

function postprocess_igem(html) {

	var $ = cheerio.load(html)

	$('#sequencePaneDiv').remove()
	$('script').remove()
	$('.compatibility_div').parent().parent().remove()
	
	$('img').each((i, img) => {

		var $img = $(img)

		var src = $img.attr('src')
		var srcset = $img.attr('srcset')

		if(src) {
			src = src.replace(/(^|\s)\/wiki/g, 'http://parts.igem.org/wiki')
			$img.attr('src', src)
		}

		if(srcset) {
			srcset = srcset.replace(/(^|\s)\/wiki/g, 'http://parts.igem.org/wiki')
			$img.attr('srcset', srcset)
		}
	})

	return $.html()
}

module.exports = postprocess_igem
