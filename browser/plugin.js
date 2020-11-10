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
            var $contentPanel = $($element.closest(".stream-content"))
            var content = data.body || data || " "
            
            // Kind of a hack to make sure it's HTML
            if (content.charAt(0) !== "<") {
                content = "<p>" + content + "</p>"
            }

            if ($contentPanel.length === 0) {
                window.location.reload(true)
            } else {
                $contentPanel.html(content)
                deleteStream(streamId)
            }
        },
        statusCode: {
            503: function(jqXHR) {
                var retry = jqXHR.getResponseHeader("Retry-After")
                retry = parseInt(retry, 10) * 1000
                
                setTimeout(fetchPluginStream, retry, streamId, $element)
            },
            410: function(){
                deleteStream(streamId)
//                window.location.reload(true)
            },
            404: function(jqXHR) {
                var $panel = $element.closest(".panel")
               
                if ($panel.length === 0) {
                    $(".stream-loader").remove()
                    $(".stream-loader-wrapper").text("An error has occurred. Please try again later.")
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

