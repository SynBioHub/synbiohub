
var SBOLDocument = require('sboljs');
var sparql = require('./sparql');

function sbolToSparql(sbol) {

	var filter = [];
	
	if(sbol.componentDefinitions.length > 0) {
		
		var componentDefinition = sbol.componentDefinitions[0];
		
		componentDefinition.types.forEach(function(type) {
			filter.push('?ComponentDefinition sbol2:type ' + sparql.escapeIRI(type) + ' .')		
		})
		
		componentDefinition.roles.forEach(function(role) {
			filter.push('?ComponentDefinition sbol2:role ' + sparql.escapeIRI(role) + ' .')		
		})
		
		componentDefinition.annotations.forEach(function(annotation) {
			
			// TODO literal annotations
			
			filter.push('?ComponentDefinition ' + sparql.escapeIRI(annotation.name) + ' ' + sparql.escapeIRI(annotation.value) + ' .')
		})
		var searchPrefix='http://www.openrdf.org/contrib/lucenesail#';
		if(componentDefinition.name !== '') {
			filter.push('?ComponentDefinition  <' + searchPrefix + 'matches> ?nameMatch .');
			filter.push('?nameMatch <' + searchPrefix + 'property> dcterms:title .');
			filter.push('?nameMatch  <' + searchPrefix + 'query> \'' + componentDefinition.name + '\' .');
			//E.g:   ?ComponentDefinition search:matches ?nameMatch .                    
  			//		 ?nameMatch search:property dcterms:title .
     		//		 ?nameMatch search:query 'spo0A*' .
		}
		
		//if(componentDefinition.description !== '') {
		//	filter.push(sparql.escape('?ComponentDefinition text:query (dcterms:description %L) .', componentDefinition.description))
		//}
	}
	
	return filter.join('\n');
}

module.exports = sbolToSparql;