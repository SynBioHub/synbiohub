
var stack = require('./stack')

function getSBOL(prefix, uri, store, callback) {

    stack.getComponentSBOL(prefix, uri, (err, sbol, componentDefinition) => {

        if(sbol) {

            callback(null, sbol, componentDefinition)

        } else {

            if(store) {

                store.getComponentSBOL(uri, (err, sbol, componentDefinition) => {

                    if(sbol) {

                        callback(null, sbol, componentDefinition)

                    } else {

                        callback(new Error('not found in stack or user store?'))

                    }

                })

            } else {

                callback(new Error('not found, no user store to look in?'))

            }

        }

    })



}

module.exports = getSBOL





