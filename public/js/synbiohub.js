
$(document).on('click', '[data-uri]', function() {

    window.location = $(this).attr('data-uri')


})


$("body").tooltip({
    selector: '[data-toggle="tooltip"]',
    container: 'body'
})


