
const { getCollectionMembersRecursive } = require('../query/collection')
const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')

const getGraphUriFromTopLevelUri = require('../getGraphUriFromTopLevelUri')

function fetchCollectionFASTA (collectionUri, user) {
  const graphUri = getGraphUriFromTopLevelUri(collectionUri, user)

  return getCollectionMembersRecursive(collectionUri, graphUri).then((members) => {
    const flattenedMembers = []

    members.forEach(flattenMember)

    function flattenMember (member) {
      flattenedMembers.push(member)

      if (member.members !== undefined) {
        Array.prototype.push.apply(flattenedMembers, member.members)

        member.members.forEach(flattenMember)
      }
    }

    const sequences = flattenedMembers.filter((member) => {
      return member.type === 'http://sbols.org/v2#Sequence'
    })

    return Promise.all(
      sequences.map((sequence) => fetchSBOLObjectRecursive('Sequence', sequence.uri, graphUri))
    )
  }).then((sequenceResults) => {
    // console.log('ress')
    // console.dir(sequenceResults)

    const sequences = sequenceResults.map((result) => result.object)
    // console.log('seqs')
    // console.log(JSON.stringify(sequences, null, 2))
    // wtf its all URIs
    // also _rootUri is set, looks like it hit the local thingy

    const fasta = []

    sequences.forEach((sequence) => {
      fasta.push('>' + sequence.name)
      fasta.push(sequence.elements)
    })

    return Promise.resolve(fasta.join('\n'))
  })
}

module.exports = {
  fetchCollectionFASTA: fetchCollectionFASTA
}
