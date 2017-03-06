
var request = require('request'),
    xml2js = require('xml2js'),
    async = require('async');

exports = module.exports = function retrieveCitations(citations) {

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

    return new Promise((resolve, reject) => {

        async.map(citations, function(pubmedID, callback) {

	    retrieveCitationInfo('pubmed', pubmedID.citation, function(err, citationList) {
                callback(err, citationList);
	    });

        }, function(err, citationLists) {

            if(err) {

                reject(err);

            } else {
                
                /* Join all the citations into one big list
                 */
                var citations = [];

                citationLists.forEach(function(citationList) {
                    citations = citations.concat(citationList);
                });

                resolve(citations)
            }
        })

    })

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

            //console.log(JSON.stringify(result));

            result.PubmedArticleSet.PubmedArticle.forEach(function(pubmedArticle) {

                callback(null, pubmedArticle.MedlineCitation.map(function(citation) {

                    var article = citation.Article[0];

                    var meta = { 

                        publicationTitle: article.ArticleTitle[0],

                        publicationAuthors: article.AuthorList[0].Author.map(function(author) {
                            return author.LastName[0] + ", " + author.Initials[0] + "."
                        }).join(', '),

                        publicationJournal: article.Journal[0].Title[0],

                        pubmedID: citation.PMID[0]._,

                        publicationDOI: '',

			publicationVolume: article.Journal[0].JournalIssue[0].Volume[0],

			publicationIssue: article.Journal[0].JournalIssue[0].Issue[0],

			publicationMonth: article.Journal[0].JournalIssue[0].PubDate[0].Month[0],

			publicationDay: article.Journal[0].JournalIssue[0].PubDate[0].Day[0],

			publicationYear: article.Journal[0].JournalIssue[0].PubDate[0].Year[0],

			publicationPages: article.Pagination[0].MedlinePgn[0]

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


