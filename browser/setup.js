
$('#setupColor').keyup(updateColor)
$('#setupColor').change(updateColor)

function updateColor() {

    var color = $('#setupColor').val()

    $('.btn').css('background-color', color)
    $('.btn').css('border-color', color)

}

