
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


$('.sbh-datatable').DataTable()

$(".chosen-select").chosen()

require('./autocomplete')
require('./dataIntegration')
require('./visbol')
require('./sse')





$(document).on('click', '#sbh-add-description', function() {

    var $textarea = $('<textarea class="form-control"></textarea>')
    var $saveButton = $('<button class="btn btn-primary">').text('Save Description')
    var $cancelButton = $('<button class="btn btn-default">').text('Cancel')

    var $div = $('<div></div>').append($textarea).append($saveButton).append($cancelButton)

    var $orig = $(this)
    $(this).replaceWith($div)

    $cancelButton.click(function() {
        $div.replaceWith($orig)
    })

    $saveButton.click(function() {

        var desc = $textarea.val()

        $.post('/updateMutableDescription', {
            uri: meta.uri,
            desc: desc,
        }, function(res) {
            $div.replaceWith($(res))
        })

    })


    $textarea.focus()

})


$(document).on('click', '#sbh-edit-description', function() {

    var $textarea = $('<textarea class="form-control"></textarea>').val($('#sbh-description').data('src'))
    var $saveButton = $('<button class="btn btn-primary">').text('Save Description')
    var $cancelButton = $('<button class="btn btn-default">').text('Cancel')

    var $div = $('<div></div>').append($textarea).append($saveButton).append($cancelButton)

    var $orig = $('#sbh-description')
    $('#sbh-description').replaceWith($div)

    $cancelButton.click(function() {
        $div.replaceWith($orig)
    })

    $saveButton.click(function() {

        var desc = $textarea.val()

        $.post('/updateMutableDescription', {
            uri: meta.uri,
            desc: desc,
        }, function(res) {
            $div.replaceWith($(res))
        })

    })


    $textarea.focus()
})

$(document).on('click', '#sbh-add-notes', function() {

    var $textarea = $('<textarea class="form-control"></textarea>')
    var $saveButton = $('<button class="btn btn-primary">').text('Save Notes')
    var $cancelButton = $('<button class="btn btn-default">').text('Cancel')

    var $div = $('<div></div>').append($textarea).append($saveButton).append($cancelButton)

    var $orig = $(this)
    $(this).replaceWith($div)

    $cancelButton.click(function() {
        $div.replaceWith($orig)
    })

    $saveButton.click(function() {

        var notes = $textarea.val()

        $.post('/updateMutableNotes', {
            uri: meta.uri,
            notes: notes,
        }, function(res) {
            $div.replaceWith($(res))
        })

    })


    $textarea.focus()

})


$(document).on('click', '#sbh-edit-notes', function() {

    var $textarea = $('<textarea class="form-control"></textarea>').val($('#sbh-notes').data('src'))
    var $saveButton = $('<button class="btn btn-primary">').text('Save Notes')
    var $cancelButton = $('<button class="btn btn-default">').text('Cancel')

    var $div = $('<div></div>').append($textarea).append($saveButton).append($cancelButton)

    var $orig = $('#sbh-notes')
    $('#sbh-notes').replaceWith($div)

    $cancelButton.click(function() {
        $div.replaceWith($orig)
    })

    $saveButton.click(function() {

        var notes = $textarea.val()

        $.post('/updateMutableNotes', {
            uri: meta.uri,
            notes: notes,
        }, function(res) {
            $div.replaceWith($(res))
        })

    })


    $textarea.focus()
})

$(document).on('click', '#sbh-add-source', function() {

    var $textarea = $('<textarea class="form-control"></textarea>')
    var $saveButton = $('<button class="btn btn-primary">').text('Save Source')
    var $cancelButton = $('<button class="btn btn-default">').text('Cancel')

    var $div = $('<div></div>').append($textarea).append($saveButton).append($cancelButton)

    var $orig = $(this)
    $(this).replaceWith($div)

    $cancelButton.click(function() {
        $div.replaceWith($orig)
    })

    $saveButton.click(function() {

        var source = $textarea.val()

        $.post('/updateMutableSource', {
            uri: meta.uri,
            source: source,
        }, function(res) {
            $div.replaceWith($(res))
        })

    })


    $textarea.focus()

})


$(document).on('click', '#sbh-edit-source', function() {

    var $textarea = $('<textarea class="form-control"></textarea>').val($('#sbh-source').data('src'))
    var $saveButton = $('<button class="btn btn-primary">').text('Save Source')
    var $cancelButton = $('<button class="btn btn-default">').text('Cancel')

    var $div = $('<div></div>').append($textarea).append($saveButton).append($cancelButton)

    var $orig = $('#sbh-source')
    $('#sbh-source').replaceWith($div)

    $cancelButton.click(function() {
        $div.replaceWith($orig)
    })

    $saveButton.click(function() {

        var source = $textarea.val()

        $.post('/updateMutableSource', {
            uri: meta.uri,
            source: source,
        }, function(res) {
            $div.replaceWith($(res))
        })

    })


    $textarea.focus()
})

