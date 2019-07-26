let loglevels = [ 'error', 'warn', 'info', 'debug' ];

$("input#verbosity").on('input', () => {
    let allowed = $("input#verbosity").val();

    $.each(loglevels, (idx, level) => {
        if(idx < allowed) {
            $(`.logline[level=${level}]`).css('display', '')
        } else {
            $(`.logline[level=${level}]`).css('display', 'none')
        }
    })
})
