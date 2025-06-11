function updateSparql(value, oldVal, field, pred, cb) {
    $.ajax({
        type: 'POST',
        url: window.location.href + "/edit/" + field,
        data: { object: value, previous: oldVal, pred: pred },
        success: (data, status, xhr) => cb(data),
        error: () => console.log("Failed to update SPARQL")
    })
}

function addSparql(value, field, pred, cb) {
    $.ajax({
        type: 'POST',
        url: window.location.href + "/add/" + field,
        data: { object: value, pred: pred },
        success: (data, status, xhr) => cb(data),
        error: () => console.log("Failed to add SPARQL")
    })
}

function removeSparql(value, field, pred, cb, finalCb) {
    $.ajax({
        type: 'POST',
        url: window.location.href + "/remove/" + field,
        data: { object: value, pred: pred },
        success: (data, status, xhr) => cb(data),
        error: () => console.log("Failed to remove SPARQL"),
        complete: finalCb
    })
}

function save($input, $elem, toEdit, oldVal, pred) {
    let newVal = $input.val()
    $input.prop('disabled', true)

    appendEditor(0, $elem)

    updateSparql(newVal, oldVal, toEdit, pred, (newText) => {
        location.reload()
    })
}

function add($input, $elem, toEdit) {
    let newVal = $input.val()
    $input.prop('disabled', true)

//    appendEditor(0, $elem)

    addSparql(newVal, toEdit, '', (newText) => {
        location.reload()
    })
}

function addPair($input0, $input1, $elem, toEdit) {
    let newPred = $input0.val()
    let newVal = $input1.val()
//    $input0.prop('disabled', true)
//    $input1.prop('disabled', true)

//    appendEditor(0, $elem)

    addSparql(newVal, toEdit, newPred, (newText) => {
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
  let text = $elem.attr("editText") || $elem.text().trim()
  let pred = $elem.attr("editPred") || ''

  if (toEdit === null) {
    return
  }
  if ((toEdit !== 'description' && toEdit !== 'title' && text === '') || (pred.startsWith('http://wiki.synbiohub.org/'))) {
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
    let $input = $("<input/>").val(text)
    
    $elem.replaceWith($input)

    $input.one('blur', () => save($input, $elem, toEdit, text, pred)).focus()
  })
  if ((toEdit === 'description' || toEdit === 'title') && text !== '') {
    appendRemover(idx, elem)
  }
}

function appendRemover(idx, elem) {
  let $elem = $(elem)
  let toRemove = getField($elem)
  let removeClass = 'do-remove-' + toRemove + '-' + idx
  let text = $elem.attr("editText") || $elem.text().trim()
  let pred = $elem.attr("editPred") || ''
  if (text==='') {
    return
  }
  if (pred.startsWith('http://wiki.synbiohub.org/')) {
    return
  }
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

    removeSparql(text, toRemove, pred, () => {
      location.reload()
    })
  })
}

function appendRemovers($elems) {
  let toRemove = getField($elems[0])

  if (toRemove === null) {
      return
  }

  $elems.each((idx, elem) => {
    appendRemover(idx, elem)
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
    let $row = $("<tr><td><td/></tr>")

    if (toAdd === 'annotation') {
      let $cell1 = $row.find("td").last()
      let $cell0 = $row.find("td").first()
      let $input1 = $("<input/>")
      let $input0 = $("<input/>")
      $cell1.append($input1)
      $cell0.append($input0)
      $input1.one('blur', () => addPair($input0, $input1, $last, toAdd)).focus()
      $input0.one('blur', () => addPair($input0, $input1, $last, toAdd)).focus()
      $last.parent().parent().parent().append($row)
    } else {
      let $cell = $row.find("td").last()
      let $input = $("<input/>")
      $cell.append($input)
      $input.one('blur', () => add($input, $last, toAdd)).focus()
      $last.parent().append($row)
    }
  })
}

function updateAddRemove() {
  let $multiples = $(".edit-multiple")
  let fields = new Set($.map($multiples, getField))

  // Remove existing adders and removers
  $("a[class|=add]").remove()
  //$("a[class|=remove]").remove()

  fields.forEach(field => {
    let classname = '.edit-' + field
    let $elems = $(classname)

    appendAdder($elems)

    if ($elems.length > 1 || (field !== 'type' && $elems.length > 0)) {
      appendRemovers($elems)
    }
  })
}

// Entry point for editors
$(window).on('load', function() {
  let $editables = $(".edit")
  $editables.each(appendEditor)

  updateAddRemove()
})
