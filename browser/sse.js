
const eventSource = new EventSource('/sse' + window.location.pathname)

eventSource.onmessage = (event) => {

    console.log('sse message!')
    console.log(event.data)

}

