const fastSort = require('fast-sort')

function sortArray (obj) {
  if (obj.length === 0) {
    return []
  }

  fastSort(obj).asc('id', 'url')

  obj.forEach(item => {
    sort(item)
  })

  return obj
}

function sort (obj) {
  if (obj === undefined || obj === null) {
    return null
  }

  if (Array.isArray(obj)) {
    obj = sortArray(obj)
  } else if (typeof obj === 'object') {
    Object.keys(obj).forEach(key => {
      if (key !== 'displayList') {
        obj[key] = sort(obj[key])
      }
    })
  }

  return obj
}

module.exports = sort
