
$(document).on('click', '[data-uri]', function() {

    window.location = $(this).attr('data-uri')


})


$("body").tooltip({
    selector: '[data-toggle="tooltip"]',
    container: 'body'
})

$('.sbh-download-picture').click(function() {

    var element = document.getElementById('design').childNodes[0]

    saveSvgAsPng(element, 'figure.png')

})

$('.sbh-autocomplete').typeahead({
    hint: false,
    highlight: true,
    minLength: 1

}, {
    name: 'my-dataset',
    source: function(query, syncResults, asyncResults) {

        $.getJSON('/autocomplete/' + query, function(res) {

            asyncResults(res.map((r) => r.name))

        })


    }

})

$('.twitter-typeahead').css('display', 'inline')

$('.sbh-datatable').DataTable()




const visbol = require('visbol')
const sbolv = require('visbol/font/sbolv/main')

if(document.getElementById('design')
    && typeof meta !== 'undefined'
    && meta.displayList) {

    var design = new visbol.Design({
        element: document.getElementById('design'),
        font: sbolv
    });

    design.setDisplayList(meta.displayList);

}



