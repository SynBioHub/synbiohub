function fetchPluginStream(streamId, $element) {
    // Make the request, retrying if necessary
    $.ajax({
        type: 'GET',
        url: '/stream/' + streamId,
        success: function(data) {
            $($element.closest(".col-md-12")).html(data.body)
        },
        statusCode: {
            503: function(jqXHR) {
                var retry = jqXHR.getResponseHeader("Retry-After")
                retry = parseInt(retry, 10) * 1000
                
                setTimeout(fetchPluginStream, retry, streamId, $element)
            },
            404: function(jqXHR) {
                $($element.closest(".panel")).remove()
            }
        }
    })
}

$(".stream-id").each((idx, $element) => {
    let streamId = $element.innerHTML

    fetchPluginStream(streamId, $element)
})

