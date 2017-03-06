
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

		    var meta = { 
                        publicationTitle: '',
                        publicationAuthors: '',
                        publicationJournal: '',
                        pubmedID: '',
                        publicationDOI: '',
			publicationVolume: '',
			publicationIssue: '',
			publicationMonth: '',
			publicationDay: '',
			publicationYear: '',
			publicationPages: ''
		    };

		    if (citation.Article && citation.Article.length > 0) {
			var article = citation.Article[0];

			if (citation.PMID && citation.PMID.length > 0) {
                            meta.pubmedID = citation.PMID[0]._
			}

			if (article.Pagination && article.Pagination.length > 0 && 
			    article.Pagination[0].MedlinePgn && article.Pagination[0].MedlinePgn.length > 0) {
			    meta.publicationPages = article.Pagination[0].MedlinePgn[0]
			}

			if (article.ArticleTitle && article.ArticleTitle.length > 0) {
                            meta.publicationTitle = article.ArticleTitle[0]
			}

			if (article.AuthorList && article.AuthorList.length > 0) {
                            meta.publicationAuthors = article.AuthorList[0].Author.map(function(author) {
				return author.LastName[0] + ", " + author.Initials[0] + "."
                            }).join(', ')
			}

			if (article.Journal && article.Journal.length > 0) {
			    var journal = article.Journal[0]
			    if (journal.Title && journal.Title.length > 0) {
				meta.publicationJournal = journal.Title[0]
			    }

			    if (journal.JournalIssue && journal.JournalIssue.length > 0) {
				var journalIssue = journal.JournalIssue[0]
				if (journalIssue.Volume && journalIssue.Volume.length > 0) {
				    meta.publicationVolume = journalIssue.Volume[0]
				}
				if (journalIssue.Issue && journalIssue.Issue.length > 0) {
				    meta.publicationIssue = journalIssue.Issue[0]
				}
				if (journalIssue.PubDate && journalIssue.PubDate.length > 0) {
				    var pubDate = journalIssue.PubDate[0]
				    if (pubDate.Month && pubDate.Month.length > 0) {
					meta.publicationMonth = pubDate.Month[0]
				    }
				    if (pubDate.Day && pubDate.Day.length > 0) {
					meta.publicationDay = pubDate.Day[0]
				    }
				    if (pubDate.Year && pubDate.Year.length > 0) {
					meta.publicationYear = pubDate.Year[0]
				    }
				}
			    }
			}

			if(article.ELocationID && article.ELocationID.length > 0)
			{
                            article.ELocationID.forEach(function(location) {

				if(location.$.EIdType == 'doi')
                                    meta.publicationDOI = location._;
                            });
			}
		    } 
                    return meta;

                }));
            });

        });

    });
}


