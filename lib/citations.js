
var request = require('request'),
    xml2js = require('xml2js'),
    async = require('async');

exports = module.exports = function retrieveCitations(citations, callback) {

    /* Parse citation list strings
     */
    // if(typeof citations === 'string') {
    //     citations = citations.split(',').map(function(pubmedID) {
    //         return pubmedID.trim();
    //     }).filter(function(pubmedID) {
    //         return pubmedID !== '';
    //     });
    // }
    //console.log(citations)

    /* Retrieve citation metadata from pubmed for all citations
     */
    async.map(citations, function(pubmedID, callback) {

        retrieveCitationInfo('pubmed', pubmedID.citation, function(err, citationList) {

            //console.log(citationList);

            callback(err, citationList);
        });

    }, function(err, citationLists) {

        if(err) {

            callback(err);

        } else {
            
            /* Join all the citations into one big list
             */
            var citations = [];

            citationLists.forEach(function(citationList) {
                citations = citations.concat(citationList);
            });

            callback(null, citations);
        }
    });

}

/* Temporary solution, will be replaced with the BioJS module
 */
function retrieveCitationInfo(db, id, callback) {

    request.get({
        url: 'http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi',
        qs: {
            db: db,
            id: id,
            retmode: 'xml'
        }
    }, function(err, response, body) {

        if(err)
        {
            return callback(err);
        }

        xml2js.parseString(body, function(err, result) {

            console.log(JSON.stringify(result));

            result.PubmedArticleSet.PubmedArticle.forEach(function(pubmedArticle) {

                callback(null, pubmedArticle.MedlineCitation.map(function(citation) {

                    var article = citation.Article[0];

                    var meta = { 

                        publicationTitle: article.ArticleTitle[0],

                        publicationAuthors: article.AuthorList[0].Author.map(function(author) {
                            return author.LastName[0] + ", " + author.Initials[0] + "."
                        }).join(', '),

                        publicationJournalInfo: citation.MedlineJournalInfo[0].MedlineTA[0],

                        pubmedID: citation.PMID[0]._,

                        publicationDOI: '',

			publicationYear: article.ArticleDate[0].Year[0]

                    };

                    if(article.ELocationID && article.ELocationID.length > 0)
                    {
                        article.ELocationID.forEach(function(location) {

                            if(location.$.EIdType == 'doi')
                                meta.publicationDOI = location._;
                        });
                    }

                    return meta;

                }));
            });

        });

    });
}


