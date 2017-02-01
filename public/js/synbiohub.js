
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


