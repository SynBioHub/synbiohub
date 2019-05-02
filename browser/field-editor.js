function updateSparql(value, field) {
    console.log(window.location.href + "/edit/" + field)
    $.ajax({
        type: 'POST',
        url: window.location.href + "/edit/" + field,
        data: { object: value },
        success: () => console.log("Successfully updated SPARQL"),
        error: () => console.log("Failed to update SPARQL")
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

    let editClass = 'do-edit-' + toEdit

    let editLink = document.createElement('a')
    editLink.setAttribute('style', 'margin-left: 10px')

    let editButton = document.createElement('span')
    editButton.setAttribute('class', 'fa fa-pencil ' + editClass)
    editLink.appendChild(editButton)

    $elem.append(editLink)

    $("." + editClass).on('click', () => {
      let text = $elem.text()
      let $input = $("<input/>").val(text)

      $elem.replaceWith($input)

      let save = () => {
        let newVal = $input.val()
        let $e = $("<h1 class=\"" + classes + "\" />").text(newVal)
        $input.replaceWith($e)
        appendEditor(0, $e)

        updateSparql(newVal, toEdit)
      }

      $input.one('blur', save).focus()
    })
}

// Entry point for editors
$(window).on('load', function() {
  let $editables = $(".edit")

  $editables.each(appendEditor)
})
