function updateSparql(value, oldVal, field, cb) {
    $.ajax({
        type: 'POST',
        url: window.location.href + "/edit/" + field,
        data: { object: value, previous: oldVal },
        success: (data, status, xhr) => cb(data),
        error: () => console.log("Failed to update SPARQL")
    })
}

function addSparql(value, field, cb) {
    $.ajax({
        type: 'POST',
        url: window.location.href + "/add/" + field,
        data: { object: value },
        success: (data, status, xhr) => cb(data),
        error: () => console.log("Failed to add SPARQL")
    })
}

function removeSparql(value, field, cb, finalCb) {
    $.ajax({
        type: 'POST',
        url: window.location.href + "/remove/" + field,
        data: { object: value },
        success: (data, status, xhr) => cb(data),
        error: () => console.log("Failed to remove SPARQL"),
        complete: finalCb
    })
}

function save($input, $elem, toEdit, oldVal) {
    let newVal = $input.val()
    $input.prop('disabled', true)

    appendEditor(0, $elem)

    updateSparql(newVal, oldVal, toEdit, (newText) => {
        $elem.text(newText)
        $input.replaceWith($elem)
        appendEditor(0, $elem)
    })
}

function add($input, $elem, toEdit) {
    let newVal = $input.val()
    $input.prop('disabled', true)

    appendEditor(0, $elem)

    addSparql(newVal, toEdit, (newText) => {
        location.reload()
    })
}

function getField(elem, idx) {
    let $elem = $(elem)
    let toEdit = null

    let classes = $elem.attr('class')
    $.each(classes.split(/\s+/), (idx, cls) => {
        if (cls.startsWith('edit-')) {
            toEdit = cls.substring(5)
        }
    })

    return toEdit
}

function appendEditor(idx, elem) {
    let $elem = $(elem)
    let toEdit = getField($elem)

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

      $input.one('blur', () => save($input, $elem, toEdit, text)).focus()
    })
}

function appendRemover($elems) {
  let toRemove = getField($elems[0])

  if (toRemove === null) {
      return
  }

  $elems.each((idx, elem) => {
    let $elem = $(elem)
    let removeClass = 'do-remove-' + toRemove + '-' + idx

    let removeLink = document.createElement('a')
    removeLink.setAttribute('class', "remove-" + toRemove)
    removeLink.setAttribute('style', 'margin-left: 2px')

    let removeButton = document.createElement('span')
    removeButton.setAttribute('class', 'fa fa-trash ' + removeClass)
    removeLink.appendChild(removeButton)

    $elem.append(removeLink)

    $('.' + removeClass).on('click', () => {
        let $trashes = $(".remove-" + toRemove)
        let text = $elem.attr("editText") || $elem.text()

        $trashes.remove()

        removeSparql(text, toRemove, () => {
          $elem.remove()
          updateAddRemove()
        },
        () => {
            updateAddRemove() 
        })
    })
  })
}

function appendAdder($elems) {
  let $last = $elems.filter(":last")
  let toAdd = getField($last)

  if (toAdd === null) {
      return
  }

  let addClass = 'do-add-' + toAdd

  let addLink = document.createElement('a')
  addLink.setAttribute('class', "add-" + toAdd)
  addLink.setAttribute('style', 'margin-left: 2px')

  let addButton = document.createElement('span')
  addButton.setAttribute('class', 'fa fa-plus ' + addClass)
  addLink.appendChild(addButton)

  $last.append(addLink)
  $("." + addClass).on("click", () => {
    let $row = $("<tr><td/><td/><tr>")
    let $cell = $row.find("td").last()
    let $input = $("<input/>")
    $cell.append($input)
    $last.parent().append($row)

    $input.one('blur', () => add($input, $last, toAdd)).focus()
  })
}

function updateAddRemove() {
  let $multiples = $(".edit-multiple")
  let fields = new Set($.map($multiples, getField))

  // Remove existing adders and removers
  $("a[class|=add]").remove()
  $("a[class|=remove]").remove()

  fields.forEach(field => {
    let classname = '.edit-' + field
    let $elems = $(classname)

    appendAdder($elems)

    if ($elems.length > 1) {
      appendRemover($elems)
    }
  })
}

// Entry point for editors
$(window).on('load', function() {
  let $editables = $(".edit")
  $editables.each(appendEditor)

  updateAddRemove()
})
