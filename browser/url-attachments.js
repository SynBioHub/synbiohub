$("input[form=sbh-attachment-lookup][name=url]").on('blur', (event) => {
    let url = $(event.target).val()
    let $type = $("input[form=sbh-attachment-lookup][name=type]")
    let $submit = $(":button[form=sbh-attachment-lookup][type=submit]")

    console.log(event.target)
    console.log($type)
    console.log(url)

    fetch(url, { 
        method: "GET",
        headers: {
            "Accept": "*/*"
        }
    }).then(response => {
        if (!response.ok) {
            throw new Error('failed request')
        }

        let type = response.headers.get('Content-Type')
        type = type.substring(type.indexOf('/')+1)

        if (type.indexOf(';') >= 0) {
            type = type.substring(type.indexOf(';'))
        }
        
        $type.val(type)
    }).catch(() => {
        let path = new URL(url).pathname.substring(1)
        
        if(path.indexOf('.') >= 0) {
            let extension = path.substring(path.lastIndexOf('.')+1)
            $type.val(extension)
        } 
    }).finally(() => {
        $type.attr('readonly', false)
        $submit.attr('disabled', false)
    })
})
