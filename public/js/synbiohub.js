
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
