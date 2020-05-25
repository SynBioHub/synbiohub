$("input[form=sbh-attachment-lookup][name=url]").on('blur', (event) => {
    let url = $(event.target).val()
    let $type = $("input[form=sbh-attachment-lookup][name=type]")
    let $name = $("input[form=sbh-attachment-lookup][name=name]")
    let $submit = $(":button[form=sbh-attachment-lookup][type=submit]")

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
            let filename = path.substring(path.lastIndexOf('/'+1), path.lastIndexOf('.'))

            $type.val(extension)
            $name.val(filename)
        } 
    }).finally(() => {
        $type.attr('readonly', false)
        $submit.attr('disabled', false)
    })
})
