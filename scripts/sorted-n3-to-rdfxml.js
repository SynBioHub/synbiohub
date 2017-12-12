
/* see n3_to_rdfxml.sh */

fs = require('mz/fs')
byline = require('byline')

const filename = process.argv[2]

process.stderr.write('Sorted n3: ' + filename + '\n')

function readFile(parseTriple) {

    return new Promise((resolve, reject) => {

        readStream = byline(fs.createReadStream(filename))

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

const uriTripleRegex = /<([^><]*)>[ \t]+<([^><]*)>[ \t]+<([^><]*)>[ \t]+\.[ \t]*$/
const literalTripleRegex = /<([^><]*)>[ \t]+<([^>]*)>[ \t]+(.*)[ \t]*\.[ \t]*$/
const predicateRegex = /^<[^>]*>[ \t]+<([^>]*)>.*$/

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
    },


    /* libSBOLj prefix bug workaround
     * https://github.com/SynBioDex/libSBOLj/issues/442
     *
     * Any namespaces we use that libSBOLj also uses have to have exactly
     * the same prefix in loaded RDF files.
     */
    {
        prefix: 'dc',
        uri: 'http://purl.org/dc/elements/1.1/'
    },
    {
        prefix: 'dcterms',
        uri: 'http://purl.org/dc/terms/'
    },
    {
        prefix: 'prov',
        uri: 'http://www.w3.org/ns/prov#'
    },
    {
        prefix: 'sbol',
        uri: 'http://sbols.org/v2#'
    },
    {
        prefix: 'sbh',
        uri: 'http://wiki.synbiohub.org/wiki/Terms/synbiohub#'
    },
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

    process.stderr.write(JSON.stringify(prefixList))

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

    console.log('    <' + tagName + ' rdf:resource="' + escapeUri(o) +  '" />')
}

function writeLiteralTriple(s, p, v) {

    updateSubject(s)

    const tagName = predicateUriToTagName(p) 

    const hatIndex = v.lastIndexOf('^^')
    const quoteIndex = v.lastIndexOf('"')

    if(hatIndex !== -1 && quoteIndex !== -1 && hatIndex > quoteIndex) {

        const type = v.slice(hatIndex).match(/<(.*)>/)[1]
        const value = JSON.parse(v.substring(0, quoteIndex + 1))

        console.log('    <' + tagName + ' rdf:datatype="' + type + '">' + escapeText(value) + '</' + tagName + '>')

    } else {
     
        const value = JSON.parse(v)

        console.log('    <' + tagName + '>' + escapeText(value) + '</' + tagName + '>')

    }

}

function predicateUriToTagName(uri) {

    for(var i = 0; i < prefixList.length; ++ i) {

        if(uri.indexOf(prefixList[i].uri) === 0) {

            return prefixList[i].prefix + ':' + uri.slice(prefixList[i].uri.length)

        }
    }
}

function escapeText(v) {

    var v2 = ''

    for(var i = 0; i < v.length; ++ i) {

        switch(v[i]) {
            case '"':
                v2 += '&quot;'
                break
            case '>':
                v2 += '&gt;'
                break
            case '<':
                v2 += '&lt;'
                break
            case '&':
                v2 += '&amp;'
                break
            case '\'':
                v2 += '&apos;'
                break
            default:
                v2 += v[i]
                break
        }
    }

    return v2
}

function escapeUri(v) {

    var r = /\\u([\d\w]{4})/gi;
    v = v.replace(r, function (match, grp) {
	return String.fromCharCode(parseInt(grp, 16));
    });

    return v
}



