
const config = require('./config')

function filterAnnotations(annotations) {

    annotations.forEach((annotation) => {
        annotation.nameDef = annotation.name
        annotation.name = annotation.name.slice(annotation.name.lastIndexOf('/')+1)
        if (annotation.type === 'uri' && annotation.value.toString().startsWith(config.get('databasePrefix'))) {
            annotation.uri = annotation.value.toString()
            annotation.url = '/' + annotation.value.toString().replace(config.get('databasePrefix'),'')
            annotation.value = annotation.value.substring(0,annotation.value.lastIndexOf('/'))
            annotation.value = annotation.value.slice(annotation.value.lastIndexOf('/')+1)
        } else if (annotation.type === 'uri') {
	    annotation.uri = annotation.value.toString()
	    annotation.url = annotation.value.toString()
            annotation.value = annotation.value.slice(annotation.value.lastIndexOf('/')+1)
        }
    })
    annotations = annotations.filter(function(annotation) {
        return (!annotation.name.toString().startsWith('created')) && (!annotation.name.toString().startsWith('modified')) && (!annotation.name.toString().startsWith('creator')) && (!annotation.name.toString().startsWith('igem#dominant')) && (!annotation.name.toString().startsWith('igem#discontinued')) && (!annotation.name.toString().startsWith('igem#group_u_list')) && (!annotation.name.toString().startsWith('igem#m_user_id')) && (!annotation.name.toString().startsWith('igem#owner_id')) && (!annotation.name.toString().startsWith('igem#owning_group_id')) && (!annotation.name.toString().startsWith('synbiohub#')) && (!annotation.name.toString().startsWith('OBI_0001617'))
    })

    return annotations

}

module.exports = filterAnnotations


