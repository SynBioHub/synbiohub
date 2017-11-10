
$(document).on('click', '[data-uri]', function () {

    window.location = $(this).attr('data-uri')


})


$("body").tooltip({
    selector: '[data-toggle="tooltip"]',
    container: 'body'
})

$('.sbh-download-picture').click(function () {

    var element = document.getElementById('design').childNodes[0]

    saveSvgAsPng(element, 'figure.png')

})


$('.sbh-datatable').DataTable()

$(document).on('click', '.sbh-datatable .save', function () {

    const $row = $(this).closest('tr')

    const inputs = $row.find('input')

    const userInfo = {
        id: parseInt($(inputs[0]).val()),
        username: $(inputs[1]).val(),
        name: $(inputs[2]).val(),
        email: $(inputs[3]).val(),
        affiliation: $(inputs[4]).val(),
        isMember: $(inputs[5]).prop('checked'),
        isCurator: $(inputs[6]).prop('checked'),
        isAdmin: $(inputs[7]).prop('checked'),
        isMaintainer: $(inputs[8]).prop('checked')
    }

    $.post('/admin/updateUser', userInfo)

})

$(document).on('click', '.removeFromWoR', function() {
    let worSecret = $('#worSecret').val();
    let worUrl = $('#worUrl').val();
    let worId = $('#worId').val();

    let completeUrl = worUrl + '/instances/' + worId + '/';

    $.ajax({
        beforeSend: function(request) {
            request.setRequestHeader('updateSecret', worSecret)
        },
        method: 'DELETE',
        url: completeUrl,
        complete: (data, status, jqXHR) => {
            window.location.reload(true);
        }
    })
})

$(document).on('blur', '#user_edit #email', function() {
    $username = $('#username');
    $email = $(this).closest('#email');

    let email = $email.val();
    let username = email.split('@')[0].replace(/\W/g, '');

    $username.val(username);
})

$(document).on('blur', '#new #name', function() {
    $id = $('input#id');
    $name = $(this).closest('input#name');

    let name = $name.val();
    let id = "";

    for(let idx = 0; idx < name.length; idx++) {
        let c = name.charAt(idx);

        if(id.length > 0 && c.match(/[A-Za-z_0-9]/g)) {
               id = id + name.charAt(idx);
        } else if(id.length == 0 && c.match(/[A-Za-z_]/g)) {
            id = id + name.charAt(idx);
        }
    }

    $id.val(id);
})

$(document).on('click', '.sbh-datatable .delete', function () {
    const $row = $(this).closest('tr')

    const inputs = $row.find('input')

    const userInfo = {
        id: parseInt($(inputs[0]).val()),
        username: $(inputs[1]).val(),
        name: $(inputs[2]).val(),
        email: $(inputs[3]).val(),
        affiliation: $(inputs[4]).val(),
        isMember: $(inputs[5]).prop('checked'),
        isCurator: $(inputs[6]).prop('checked'),
        isAdmin: $(inputs[7]).prop('checked')
    }

    var dt = $(this).closest('.sbh-datatable').DataTable()

    $.post('/admin/deleteUser', userInfo, function () {

        dt.row($row).remove().draw()

    })
})

if (typeof meta !== 'undefined') {
    $('.sbh-collection-members-datatable').DataTable({
        processing: true,
        serverSide: true,

        searching: !meta.remote,
        ordering: !meta.remote,

        ajax: {
            url: '/api/datatables',
            type: 'GET',
            data: function (d) {
                d.type = 'collectionMembers'
                d.collectionUri = meta.uri
                d.graphUri = meta.graphUri
            }
        }

    })
}

$('.sbh-registries-datatable').DataTable({
    processing: false,
    serverSide: false,

    searching: false,
    ordering: false,
    paging: false
})

$(document).on('click', '.save-registry', function () {
    $row = $(this).closest('tr')

    var registryInfo = {
        uri: $row.find('#uri').val(),
        url: $row.find('#url').val()
    }

    $.post('/admin/saveRegistry', registryInfo, function() { })
})

$(document).on('click', '.delete-registry', function () {
    $row = $(this).closest('tr')

     var registryInfo = {
        uri: $row.find('#uri').val(),
        url: $row.find('#url').val()
    }

    var dt = $(this).closest('.sbh-registries-datatable').DataTable()

    $.post('/admin/deleteRegistry', registryInfo, function() {

        dt.row($row).remove().draw()

    })
})

$(".chosen-select").chosen()

require('./autocomplete')
require('./dataIntegration')
require('./visbol')
require('./sse')
require('./setup')

function createWikiEditor($el, saveButtonText, updateEndpoint) {

    var $buttons = {
        bold: $('<button class="btn"><span class="fa fa-bold"></span></button>').click(function () {
            $textarea.val($textarea.val() + '<b></b>').focus()
            return false
        }),
        italic: $('<button class="btn"><span class="fa fa-italic"></span></button>').click(function () {
            $textarea.val($textarea.val() + '<i></i>').focus()
            return false
        }),
        underline: $('<button class="btn"><span class="fa fa-underline"></span></button>').click(function () {
            $textarea.val($textarea.val() + '<u></u>').focus()
            return false
        }),
        image: $('<button class="btn sbh-wiki-add-image-button"><span class="fa fa-picture-o"></span></button>').click(insertImage),
        link: $('<button class="btn"><span class="fa fa-globe"></span></button>').click(function () {
            $textarea.val($textarea.val() + '<a href="http://example.com">link text</a>').focus()
            return false
        })
    }

    /* TODO hackkk
     */
    function getImageAttachments() {

        return $('.attachments-table tr').filter(function (i, tr) {
            return $(tr).children('td').first().text() === 'Image'
        }).map(function (i, tr) {
            return {
                name: $($(tr).children('td')[1]).text(),
                url: $(tr).find('a').attr('href') + '/download'
            }
        })
    }

    function insertImage() {

        var $dropdown = $('<div class="dropdown"></div>')
        var $dropdownMenu = $('<div class="dropdown-menu"></div>')
        $dropdown.append($dropdownMenu)

        getImageAttachments().each(function (i, attachment) {

            var $menuItem = $('<a class="dropdown-item"></a>')

            $menuItem.click(function () {
                $textarea.val($textarea.val() + '<img src="' + attachment.url + '"></img>').focus()
                $dropdown.detach()
                return false
            })

            $menuItem.text(attachment.name)
            $menuItem.attr('href', attachment.url)

            $dropdownMenu.append($menuItem)
            $dropdownMenu.append('<br/>')

        })

        $dropdownMenu.show()

        $('body').append($dropdown)
        $dropdown.offset($buttons.image.offset())

        setTimeout(function () {
            $('body').click(function () {
                $dropdown.detach()
            })
        }, 50)

    }

    var $topbar = $('<div></div>')
        .append($buttons.bold)
        .append($buttons.italic)
        .append($buttons.underline)
        .append($buttons.image)
        .append($buttons.link)

    var $textarea = $('<textarea class="form-control"></textarea>')
    var $saveButton = $('<button class="btn btn-primary">').text(saveButtonText)
    var $cancelButton = $('<button class="btn btn-default">').text('Cancel')

    $textarea.val($el.attr('data-src'))

    var $div = $('<div></div>')
        .append($topbar)
        .append($textarea)
        .append($saveButton)
        .append($cancelButton)

    var $orig = $el
    $el.replaceWith($div)

    $cancelButton.click(function () {
        $div.replaceWith($orig)
    })

    $saveButton.click(function () {

        var value = $textarea.val()

        $.post(updateEndpoint, {
            uri: meta.uri,
            value: value,
        }, function (res) {
            $div.replaceWith($(res))
        })

    })

    $textarea.focus()
}



$(document).on('click', '#sbh-add-description', function () {
    createWikiEditor($('#sbh-description'), 'Save Description', '/updateMutableDescription')
    return false
})


$(document).on('click', '#sbh-edit-description', function () {
    createWikiEditor($('#sbh-description'), 'Save Description', '/updateMutableDescription')
    return false
})

$(document).on('click', '#sbh-add-notes', function () {
    createWikiEditor($('#sbh-notes'), 'Save Notes', '/updateMutableNotes')
    return false
})


$(document).on('click', '#sbh-edit-notes', function () {
    createWikiEditor($('#sbh-notes'), 'Save Notes', '/updateMutableNotes')
    return false
})

$(document).on('click', '#sbh-add-source', function () {
    createWikiEditor($('#sbh-source'), 'Save Source', '/updateMutableSource')
    return false
})


$(document).on('click', '#sbh-edit-source', function () {
    createWikiEditor($('#sbh-source'), 'Save Source', '/updateMutableSource')
    return false
})


$(document).on('click', '#sbh-add-citations', function () {
    createWikiEditor($('#sbh-citations'), 'Save Citations', '/updateCitations')
    return false
})


$(document).on('click', '#sbh-edit-citations', function () {
    createWikiEditor($('#sbh-citations'), 'Save Citations', '/updateCitations')
    return false
})


// https://www.abeautifulsite.net/whipping-file-inputs-into-shape-with-bootstrap-3
//
$(function () {

    // We can attach the `fileselect` event to all file inputs on the page
    $(document).on('change', ':file', function () {
        var input = $(this),
            numFiles = input.get(0).files ? input.get(0).files.length : 1,
            label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
        input.trigger('fileselect', [numFiles, label]);
    });

    // We can watch for our custom `fileselect` event like this
    $(document).ready(function () {
        $(':file').on('fileselect', function (event, numFiles, label) {

            var input = $(this).parents('.input-group').find(':text'),
                log = numFiles > 1 ? numFiles + ' files selected' : label;

            console.log($(this).closest('form').length)
            console.log($(this).closest('form').find('button').length)
            $(this).closest('form').find('button[type=submit]').prop('disabled', false).addClass('btn-success')

            if (input.length) {
                input.val(log);
            } else {
                //if( log ) alert(log);
            }

        });
    });

});

$('#sbh-attachment-form').submit(function (e) {

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
        success: function (data) {
            $('.attachments-table').replaceWith($('<div></div>').html(data).find('.attachments-table'))

            var form = $(':file').val('').closest('form')
            form.find('button[type=submit]').prop('disabled', true).removeClass('btn-success')

            $(':file').parents('.input-group').find(':text').val('')
        }
    })


    return false

})



$('.sbh-sparql-editor').each((i, textarea) => {

    var cm = CodeMirror.fromTextArea(textarea, {
        lineNumbers: true
    })

})

const extend = require('xtend')

function getFields(type) {
    var fields = {
        id: {
            type: 'text',
            default: '',
            name: 'ID'
        },
        type: {
            type: 'value',
            default: '',
            name: 'Type'
        },
        url: {
            type: 'text',
            default: '',
            name: 'URL'
        },
        sequenceSuffix: {
            type: 'text',
            default: '_sequence',
            name: 'Sequence Suffix'
        },
        defaultFolderId: {
            type: 'text',
            default: '',
            name: 'Default Folder ID'
        },
        isPublic: {
            type: 'checkbox',
            default: false,
            name: 'Public'
        },
        rejectUnauthorized: {
            type: 'checkbox',
            default: false,
            name: 'Reject Unauthorized'
        },
        folderPrefix: {
            type: 'text',
            default: 'folder_',
            name: 'Folder Prefix'
        },
        rootCollectionDisplayId: {
            type: 'text',
            default: '',
            name: 'Root Collection Display ID'
        },
        rootCollectionName: {
            type: 'text',
            default: '',
            name: 'Root Collection Name'
        },
        rootCollectionDescription: {
            type: 'textarea',
            default: '',
            name: 'Root Collection Description'
        }
    }

    var specificFields = {
        ice: {
            iceApiToken: {
                type: 'text',
                default: '',
                name: 'ICE API Token'
            },
            iceApiTokenClient: {
                type: 'text',
                default: '',
                name: 'ICE API Token Client'
            },
            iceApiTokenOwner: {
                type: 'text',
                default: '',
                name: 'ICE API Token Owner'
            },
            iceCollection: {
                type: 'text',
                default: 'FEATURED',
                name: 'ICE Collection'
            },
            groupId: {
                type: 'text',
                default: '',
                name: 'Group ID'
            },
            pi: {
                type: 'text',
                default: '',
                name: 'PI'
            },
            piEmail: {
                type: 'text',
                default: '',
                name: 'PI Email'
            },
            partNumberPrefix: {
                type: 'text',
                default: '',
                name: 'Part Number Prefix'
            }
        },
        benchling: {
            benchlingApiToken: {
                type: 'text',
                default: '',
                name: 'Benchling API Token'
            },
            defaultFolderId: {
                type: 'text',
                default: '',
                name: 'Default Folder ID'
            },
        }
    }

    return extend(fields, specificFields[type])
}

function clearForm() {
    $form = $('#remoteForm').empty();
}

function populateForm(type, data) {
    $form = $('#remoteForm');

    const fields = getFields(type);

    Object.keys(fields).forEach(key => {
        fieldInfo = fields[key];

        $label = $("<label />").attr("for", key).text(fieldInfo.name);
        $input = {
            "text": $("<input />").attr("type", "text").val(fieldInfo.default),
            "checkbox": $("<input />").attr("type", "checkbox").prop("checked", fieldInfo.default),
            "textarea": $("<textarea />").val(fieldInfo.default),
            "value": $("<input />").attr("type", "text").attr('readonly', 'readonly').val(fieldInfo.default),
        }[fieldInfo.type].attr("name", key).addClass("form-control")

        if(data[key]) {
	    if (fieldInfo.type === "checkbox") {
		$input.prop("checked", data[key])
	    } else {
		$input.val(data[key]);
	    }

            if(key == "id") {
                $input.attr('readonly', 'readonly')
            }
        }

        $group = $("<div />").addClass('form-group').append($label, $input)

        $('#remoteForm').append($group)
    })


}

$(document).on('click', '#remoteTypeSelect', function () {
    var type = $(this).val()

    if (type != "") {
        clearForm();
        $('#addRemote').attr('disabled', false);
        populateForm(type, {"type": type})
    } else {
        $('#addRemote').attr('disabled', true);
        clearForm();
    }
})


$(document).on('click', '#remoteEdit', function () {
    clearForm();
    var id = $(this).closest('table').find('#remote-id').text();

    var remote = remotes.find((remote) => {
        return remote.id == id;
    })

    var data = {
        "ice": {
            id: remote["id"],
            type: "ice",
            url: remote["url"],
            rejectUnauthorized: remote["rejectUnauthorized"],
            isPublic: remote["public"] || false,
            folderPrefix: remote["folderPrefix"],
            sequenceSuffix: remote["sequenceSuffix"],
            defaultFolderId: remote["defaultFolderId"],
            rootCollectionDisplayId: remote.rootCollection["displayId"],
            rootCollectionName: remote.rootCollection["name"],
            rootCollectionDescription: remote.rootCollection["description"],
            iceApiToken: remote["X-ICE-API-Token"],
            iceApiTokenClient: remote["X-ICE-API-Token-Client"],
            iceApiTokenOwner: remote["X-ICE-API-Token-Owner"],
            iceCollection: remote["iceCollection"],
            groupId: remote["groupId"],
            pi: remote["PI"],
            partNumberPrefix: remote["partNumberPrefix"],
            piEmail: remote["PIemail"],
        },
        "benchling": {
            id: remote["id"],
            type: "benchling",
            url: remote["url"],
            rejectUnauthorized: remote["rejectUnauthorized"],
            isPublic: remote["public"] || false,
            folderPrefix: remote["folderPrefix"],
            sequenceSuffix: remote["sequenceSuffix"],
            defaultFolderId: remote["defaultFolderId"],
            rootCollectionDisplayId: remote.rootCollection["displayId"],
            rootCollectionName: remote.rootCollection["name"],
            rootCollectionDescription: remote.rootCollection["description"],
            benchlingApiToken: remote["X-BENCHLING-API-Token"],
            defaultFolderId: remote["defaultFolderId"],
        }
    }[remote.type]

    populateForm(remote.type, data)
})
