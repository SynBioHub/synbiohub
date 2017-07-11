
const config = require('./config')

var sha1 = require('sha1');

function filterAnnotations(req,annotations) {

    annotations.forEach((annotation) => {
        annotation.nameDef = annotation.name
        annotation.name = annotation.name.slice(annotation.name.lastIndexOf('/')+1)
        if (annotation.type === 'uri') {
	    if (annotation.value.toString().startsWith(config.get('databasePrefix'))) {
		annotation.uri = annotation.value.toString()
		annotation.url = '/' + annotation.value.toString().replace(config.get('databasePrefix'),'')
		if (annotation.value.toString().startsWith(config.get('databasePrefix')+'user/') && req.url.toString().endsWith('/share')) {
                    annotation.url += '/' + sha1('synbiohub_' + sha1(annotation.uri) + config.get('shareLinkSalt')) + '/share'
                }
		var remaining = annotation.value.substring(0,annotation.value.lastIndexOf('/'))
		annotation.value = annotation.value.substring(annotation.value.lastIndexOf('/')+1)
		annotation.value = remaining.slice(remaining.lastIndexOf('/')+1) + '/' + annotation.value
            } else {
		annotation.uri = annotation.value.toString()
		annotation.url = annotation.value.toString()
		annotation.value = annotation.value.slice(annotation.value.lastIndexOf('/')+1)
	    }
        }
    })
    annotations = annotations.filter(function(annotation) {
        return (!annotation.name.toString().startsWith('created')) && (!annotation.name.toString().startsWith('modified')) && (!annotation.name.toString().startsWith('creator')) && (!annotation.name.toString().startsWith('igem#dominant')) && (!annotation.name.toString().startsWith('igem#discontinued')) && (!annotation.name.toString().startsWith('igem#group_u_list')) && (!annotation.name.toString().startsWith('igem#m_user_id')) && (!annotation.name.toString().startsWith('igem#owner_id')) && (!annotation.name.toString().startsWith('igem#owning_group_id')) && (!annotation.name.toString().startsWith('OBI_0001617')) && (!annotation.name.toString().startsWith('rdf-schema#label'))
    })

    return annotations

}

module.exports = filterAnnotations


