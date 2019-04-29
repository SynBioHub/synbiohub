
function sortArray (obj) {
  if (obj.length === 0) {
    return []
  }

  obj.sort((a, b) => {
    let JSONa = JSON.stringify(a)
    let JSONb = JSON.stringify(b)

    if (JSONa > JSONb) {
      return 1
    } else if (JSONa < JSONb) {
      return -1
    } else {
      return 0
    }
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
      obj[key] = sort(obj[key])
    })
  }

  return obj
}

module.exports = sort
