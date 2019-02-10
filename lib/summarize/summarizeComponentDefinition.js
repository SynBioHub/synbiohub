
var namespace = require('./namespace')

var summarizeProtein = require('./summarizeProtein')
var summarizeSequence = require('./summarizeSequence')
var summarizeTopLevel = require('./summarizeTopLevel')
var summarizeComponent = require('./summarizeComponent')
var summarizeSequenceAnnotation = require('./summarizeSequenceAnnotation')
var summarizeSequenceConstraint = require('./summarizeSequenceConstraint')
var summarizeRoles = require('./summarizeRoles')
var getDisplayList = require('visbol/lib/getDisplayList').getDisplayList

var config = require('../config')

var uriToUrl = require('../uriToUrl')
var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI

function summarizeComponentDefinition (componentDefinition, req, sbol, remote, graphUri) {
  if (componentDefinition instanceof URI) {
    return uriToMeta(componentDefinition)
  }
  // TODO: debug for sa bug
  // console.log('sum:'+componentDefinition.uri)
  // console.log('co:'+componentDefinition.components.length)
  // console.log('sa:'+componentDefinition.sequenceAnnotations.length)
  // console.log('sc:'+componentDefinition.sequenceConstraints.length)

  var components = summarizeComponents(componentDefinition, req, sbol, remote, graphUri)
  var sequenceConstraints = summarizeSequenceConstraints(componentDefinition, req, sbol, remote, graphUri)
  var sequenceAnnotations = summarizeSequenceAnnotations(componentDefinition, req, sbol, remote, graphUri)

  var displayList
  try {
    displayList = getDisplayList(componentDefinition, config, req.url.toString().endsWith('/share'), 30)
  } catch (error) {
    console.log(error)
  }

  var summary = {
    sbhBookmark: sbhBookmark(componentDefinition),
    sbhStar: sbhStar(componentDefinition),
    igemDominant: igemDominant(componentDefinition),
    igemDiscontinued: igemDiscontinued(componentDefinition),
    isReplacedBy: isReplacedBy(componentDefinition),
    type: type(componentDefinition),
    types: types(componentDefinition),
    roles: summarizeRoles(componentDefinition),
    components: components,
    sequenceConstraints: sequenceConstraints,
    sequenceAnnotations: sequenceAnnotations,
    numSubComponents: componentDefinition.components.length,
    numSubComponentsTotal: 0,
    numSequences: 0,
    displayList: displayList,
    sequences: summarizeSequences(componentDefinition, req, sbol, remote, graphUri),
    BenchlingRemotes: (Object.keys(config.get('remotes')).filter(function (e) { return config.get('remotes')[e].type === 'benchling' }).length > 0),
    ICERemotes: (Object.keys(config.get('remotes')).filter(function (e) { return config.get('remotes')[e].type === 'ice' }).length > 0)
  }

  summary = Object.assign(summary, summarizeTopLevel(componentDefinition, req, sbol, remote, graphUri))

  summary = Object.assign(summary, { remote: summarizeRemote(componentDefinition, req) })

  switch (summary.type) {
    case 'Protein':
      summary.protein = summarizeProtein(componentDefinition)
      break
  }

  var uploadedBy = componentDefinition.getAnnotation(namespace.synbiohub + 'uploadedBy')

  if (uploadedBy) {
    summary.synbiohub = {
      uploadedBy: uploadedBy
    }
  }

  return summary
}

function type (componentDefinition) {
/* TODO pick DNA/RNA/protein if one of those types is in the list
*/
  return types(componentDefinition)[0]
}

function types (componentDefinition) {
  return componentDefinition.types.map((uri) => {
    uri = '' + uri

    var prefix = namespace.biopax

    if (uri.indexOf(prefix) === 0) {
      var biopaxTerm = uri.slice(prefix.length)

      return {
        uri: uri,
        term: uri, // biopaxTerm,
        description: { name: biopaxTerm }
      }
    }

    for (var i = 0; i < namespace.so.length; ++i) {
      prefix = namespace.so[i]

      if (uri.indexOf(prefix) === 0) {
        var soTerm = uri.slice(prefix.length).split('_').join(':')

        var sequenceOntology = require('../ontologies/sequence-ontology')

        return {
          uri: uri,
          term: uri, // soTerm,
          description: sequenceOntology[soTerm]
        }
      }
    }

    return {
      uri: uri
    }
  })
}

function igemDominant (componentDefinition) {
  var dominantStr = componentDefinition.getAnnotations(namespace.igem + 'dominant')
  return {
    description: dominantStr
  }
}

function isReplacedBy (componentDefinition) {
  var isReplacedByUri = componentDefinition.getAnnotations(namespace.dcterms + 'isReplacedBy')
  var isReplacedById
  if (isReplacedByUri !== '') {
    isReplacedByUri = '/' + isReplacedByUri.toString().replace(config.get('databasePrefix'), '')
    isReplacedById = isReplacedByUri.toString().replace('/public/', '').replace('/1', '') + ' '
    isReplacedById = isReplacedById.substring(isReplacedById.indexOf('/') + 1)
    return {
      uri: isReplacedByUri,
      id: isReplacedById
    }
  }
  return {
    uri: isReplacedByUri
  }
}

function sbhBookmark (componentDefinition) {
  var bookmarkStr = componentDefinition.getAnnotations(namespace.synbiohub + 'bookmark')
  return {
    description: bookmarkStr
  }
}

function sbhStar (componentDefinition) {
  var starStr = componentDefinition.getAnnotations(namespace.synbiohub + 'star')
  return {
    description: starStr
  }
}

function igemDiscontinued (componentDefinition) {
  var discontinuedStr = componentDefinition.getAnnotations(namespace.igem + 'discontinued')
  return {
    description: discontinuedStr
  }
}

// function labels (componentDefinition) {
//  return componentDefinition.getAnnotations(namespace.rdfs + 'label')
// }

// function comments (componentDefinition) {
//  return componentDefinition.getAnnotations(namespace.rdfs + 'comment')
// }

function summarizeSequences (componentDefinition, req, sbol, remote, graphUri) {
  var sequences = componentDefinition.sequences.map((sequence) => {
    return summarizeSequence(sequence, req, sbol, remote, graphUri)
  })
  sequences.forEach((sequence) => {
    sequence.url = uriToUrl(sequence)
    if (req.params.version === 'current') {
      sequence.url = sequence.url.toString().replace('/' + sequence.version, '/current')
      sequence.version = 'current'
    }
  })
  return sequences
}

function summarizeComponents (componentDefinition, req, sbol, remote, graphUri) {
  var components = []
  componentDefinition.components.forEach((component) => {
    components.push(summarizeComponent(component, req, sbol, remote, graphUri))
  })
  return components
}

function summarizeSequenceAnnotations (componentDefinition, req, sbol, remote, graphUri) {
  var sequenceAnnotations = []
  componentDefinition.sequenceAnnotations.forEach((sequenceAnnotation) => {
    // TODO: this line of code should not be necessary.  Somehow sequenceAnnotations is getting corrupted.
    if (sequenceAnnotation.uri) {
      sequenceAnnotations.push(summarizeSequenceAnnotation(sequenceAnnotation, req, sbol, remote, graphUri))
    }
  })
  return sequenceAnnotations
}

function summarizeSequenceConstraints (componentDefinition, req, sbol, remote, graphUri) {
  var sequenceConstraints = []
  componentDefinition.sequenceConstraints.forEach((sequenceConstraint) => {
    sequenceConstraints.push(summarizeSequenceConstraint(sequenceConstraint, req, sbol, remote, graphUri))
  })
  return sequenceConstraints
}

function summarizeRemote (componentDefinition, req) {
  var remote
  componentDefinition.annotations.forEach((annotation) => {
    if (annotation.name === 'benchling#edit' && req.params.version === 'current') {
      remote = { name: 'Benchling',
        url: annotation.url
      }
    } else if (annotation.name === 'ice#entry' && req.params.version === 'current') {
      remote = { name: 'ICE',
        url: annotation.url
      }
    }
  })
  return remote
}

module.exports = summarizeComponentDefinition
