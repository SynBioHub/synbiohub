function updateSparql(value, field, cb) {
    console.log(window.location.href + "/edit/" + field)
    $.ajax({
        type: 'POST',
        url: window.location.href + "/edit/" + field,
        data: { object: value },
        success: (data, status, xhr) => cb(data),
        error: () => console.log("Failed to update SPARQL")
    })
}

function save($input, $elem, toEdit) {
    let newVal = $input.val()
    $input.prop('disabled', true)

    appendEditor(0, $elem)

    updateSparql(newVal, toEdit, (newText) => {
        $elem.text(newText)
        $input.replaceWith($elem)
        appendEditor(0, $elem)
    })
}

function appendEditor(idx, elem) {
    let $elem = $(elem)
    let toEdit = null

    let classes = $elem.attr('class')
    $.each(classes.split(/\s+/), (idx, cls) => {
        if (cls.startsWith('edit-')) {
            toEdit = cls.substring(5)
        }
    })

    if (toEdit === null) {
        return
    }

    let editClass = 'do-edit-' + toEdit + idx

    let editLink = document.createElement('a')
    editLink.setAttribute('style', 'margin-left: 10px')

    let editButton = document.createElement('span')
    editButton.setAttribute('class', 'fa fa-pencil ' + editClass)
    editLink.appendChild(editButton)

    $elem.append(editLink)

    $("." + editClass).on('click', () => {
      let text = $elem.attr("editText") || $elem.text()
      let $input = $("<input/>").val(text)

      $elem.replaceWith($input)

      $input.one('blur', () => save($input, $elem, toEdit)).focus()
    })
}

// Entry point for editors
$(window).on('load', function() {
  let $editables = $(".edit")
  console.log($editables)
  $editables.each(appendEditor)
})
