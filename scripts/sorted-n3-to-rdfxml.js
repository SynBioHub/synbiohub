
/* see n3_to_rdfxml.sh */

fs = require('mz/fs')
byline = require('byline')

function readFile(parseTriple) {

    return new Promise((resolve, reject) => {

        readStream = byline(fs.createReadStream('sorted.n3'))

        readStream.on('data', (line) => {

            parseTriple(line.toString())

        })

        readStream.on('finish', () => {

            resolve()

        })

    })


}

function createPrefixes() {

    return readFile(createPrefixFromTripleLine)

}

const uriTripleRegex = /<(.*)>[ ]+<(.*)>[ ]+<(.*)>[ ]+\.[ ]*$/
const literalTripleRegex = /<(.*)>[ ]+<(.*)>[ ]+(.*)[ ]*\.[ ]*$/
const predicateRegex = /^<[^>]*>[ ]+<([^>]*)>.*$/

function createPrefixFromTripleLine(line) {

    const predicateMatch = line.match(predicateRegex)

    if(predicateMatch !== null) {
        createPrefix(predicateMatch[1])
    }

}

prefixList = [
    {
        prefix: 'rdf',
        uri: 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'
    }
]

prefixNum = 0

function createPrefix(uri) {

    for(var i = 0; i < prefixList.length; ++ i) {

        const prefix = prefixList[i].uri

        if(uri.indexOf(prefix) === 0) {

            return

        }
    }

    var fragmentStart = uri.lastIndexOf('#')

    if(fragmentStart === -1)
        fragmentStart = uri.lastIndexOf('/')

    if(fragmentStart === -1) {
        throw new Error('cannot prefixify uri ' + uri)
    }

    prefixList.push({
        prefix: 'ns' + (prefixNum ++ ),
        uri: uri.slice(0, fragmentStart + 1)
    })

}

currentSubject = null

createPrefixes().then(() => {

    //console.log(JSON.stringify(prefixList))

    return writeXML()

})

function writeXML() {

    console.log('<?xml version="1.0" ?>')
    console.log('<rdf:RDF ' + prefixList.map((prefix) => {
        return 'xmlns:' + prefix.prefix + '="' + prefix.uri + '"'
    }).join(' ') + '>')

    return readFile(writeTriple).then(() => {

        if(currentSubject !== null)
            console.log('</rdf:Description>')
        console.log('</rdf:RDF>')

    })

}

function writeTriple(line) {

    const uriTripleMatch = line.match(uriTripleRegex)

    if(uriTripleMatch !== null) {
        writeUriTriple(uriTripleMatch[1], uriTripleMatch[2], uriTripleMatch[3])
        return
    }

    const literalTripleMatch = line.match(literalTripleRegex)

    if(literalTripleMatch !== null) {
        writeLiteralTriple(literalTripleMatch[1], literalTripleMatch[2], literalTripleMatch[3])
        return
    }

}

function updateSubject(s) {

    if(currentSubject === null) {
        console.log('<rdf:Description rdf:about="' + s + '">')
        currentSubject = s
    } else {
        if(currentSubject !== s) {
            console.log('</rdf:Description>')
            console.log('<rdf:Description rdf:about="' + s + '">')
            currentSubject = s
        }
    }

}

function writeUriTriple(s, p, o) {

    updateSubject(s)

    const tagName = predicateUriToTagName(p) 

    console.log('    <' + tagName + ' rdf:resource="' + o +  '" />')
}

function writeLiteralTriple(s, p, v) {

    updateSubject(s)

    const tagName = predicateUriToTagName(p) 
    const value = JSON.parse(v)

    console.log('    <' + tagName + '>' + value + '</' + tagName + '>')

}

function predicateUriToTagName(uri) {

    for(var i = 0; i < prefixList.length; ++ i) {

        if(uri.indexOf(prefixList[i].uri) === 0) {

            return prefixList[i].prefix + ':' + uri.slice(prefixList[i].uri.length)

        }
    }
}


