function deleteStream(streamId) {
    $.ajax({
        type: 'DELETE',
        url: '/api/stream/' + streamId
    })
}

function fetchPluginStream(streamId, $element) {
    // Make the request, retrying if necessary
    $.ajax({
        type: 'GET',
        url: '/api/stream/' + streamId,
        success: function(data) {
            $($element.closest(".stream-content")).html(data.body)
            deleteStream(streamId)
        },
        statusCode: {
            503: function(jqXHR) {
                var retry = jqXHR.getResponseHeader("Retry-After")
                retry = parseInt(retry, 10) * 1000
                
                setTimeout(fetchPluginStream, retry, streamId, $element)
            },
            404: function(jqXHR) {
                var $panel = $element.closest(".panel")
               
                if ($panel == null) {
                    $(".stream-loader").remove()
                    $(".stream-content").text("An error has occurred. Please try again later.")
                } else {
                    $($element.closest(".panel")).remove()
                }
            }
        }
    })
}

$(".stream-id").each((idx, $element) => {
    let streamId = $element.innerHTML

    fetchPluginStream(streamId, $element)
})

