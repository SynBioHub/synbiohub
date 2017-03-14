
function getCount(type, stores) {

    return new Promise((resolve, reject) => {

        async.eachSeries(stores, (store, next) => {

	    var query = loadTemplate('./sparql/Count.sparql', {
		type: type
	    })

            sparql.queryJson(query, store).then((result) => {

                if(result && result[0]) {

                    resolve(result)

                } else {

                    next()

                }

            })

        }, (err) => {

            reject(err)
                        
        })

    })

}
