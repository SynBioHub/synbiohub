const request = require('request')

var postprocess_igem = require('../../postprocess_igem')

module.exports = function fetchFromIgem(topLevel) {

    if(topLevel.wasDerivedFrom.toString().indexOf('http://parts.igem.org/') === 0) {

	return Promise.all([

	    new Promise((resolve, reject) => {

		request.get(topLevel.wasDerivedFrom.toString() + '?action=render', function(err, res, body) {

		    if(err) {
			resolve()
			//reject(err)
			return
		    }

		    if(res.statusCode >= 300) {
			resolve()
			//reject(new Error('HTTP ' + res.statusCode))
			return
		    }

		    iGemMainPage = body
		    if (iGemMainPage != '') {
			iGemMainPage = postprocess_igem(iGemMainPage.toString())
		    }

		    resolve(iGemMainPage)
		})
	    }),


	    new Promise((resolve, reject) => {

		request.get(topLevel.wasDerivedFrom.toString() + ':Design?action=render', function(err, res, body) {

		    if(err) {
			//reject(err)
			resolve()
			return
		    }

		    if(res.statusCode >= 300) {
			//reject(new Error('HTTP ' + res.statusCode))
			resolve()
			return
		    }

		    iGemDesign = body
		    if (iGemDesign != '') {
			iGemDesign = postprocess_igem(iGemDesign.toString())
		    }

		    resolve(iGemDesign)
		})
	    }),


	    new Promise((resolve, reject) => {

		request.get(topLevel.wasDerivedFrom.toString() + ':Experience?action=render', function(err, res, body) {

		    if(err) {
			//reject(err)
			resolve()
			return
		    }

		    if(res.statusCode >= 300) {
			//reject(new Error('HTTP ' + res.statusCode))
			resolve()
			return
		    }

		    iGemExperience = body
		    if (iGemExperience != '') {
			iGemExperience = postprocess_igem(iGemExperience.toString())
		    }

		    resolve(iGemExperience)
		})
	    })

	])
    } else {
	return Promise.resolve()
    }

}

