
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


// https://www.abeautifulsite.net/whipping-file-inputs-into-shape-with-bootstrap-3
//
$(function() {

  // We can attach the `fileselect` event to all file inputs on the page
  $(document).on('change', ':file', function() {
    var input = $(this),
        numFiles = input.get(0).files ? input.get(0).files.length : 1,
        label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
    input.trigger('fileselect', [numFiles, label]);
  });

  // We can watch for our custom `fileselect` event like this
  $(document).ready( function() {
      $(':file').on('fileselect', function(event, numFiles, label) {

          var input = $(this).parents('.input-group').find(':text'),
              log = numFiles > 1 ? numFiles + ' files selected' : label;

          console.log($(this).closest('form').length)
          console.log($(this).closest('form').find('button').length)
          $(this).closest('form').find('button[type=submit]').prop('disabled', false).addClass('btn-success')

          if( input.length ) {
              input.val(log);
          } else {
              if( log ) alert(log);
          }

      });
  });
  
});

$('#sbh-attachment-form').submit(function(e) {

    e.preventDefault()

    var formData = new FormData($(this)[0])

    console.log($(this))
    console.log($(this)[0])
    console.log($(this).attr('action'))

    console.log(formData)

    $.ajax({
        url: $(this).attr('action'),
        method: 'post',
        data: formData,
        cache: false,
        contentType: false,
        processData: false,
        success: function(data) {
            $('.attachments-table').replaceWith($('<div></div>').html(data).find('.attachments-table'))

            var form = $(':file').val('').closest('form')
            form.find('button[type=submit]').prop('disabled', true).removeClass('btn-success')

            $(':file').parents('.input-group').find(':text').val('')
        }
    })


    return false

})


