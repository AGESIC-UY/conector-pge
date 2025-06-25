<footer>
    <p>Powered by Agesic - Conector PDI V. 5.0</p>
</footer>

<script>
    function checkFileSize(inputFile) {
        $('#info').css('display', 'none')
        $('#info strong').remove();
        $('#info br').remove();

        const maxUploadSize = '<c:out value="${max_upload_size}"/>';
        if (inputFile.files && inputFile.files[0].size >= maxUploadSize) {
            inputFile.value = null;

            $('#info').css('display', 'block')
                .removeClass("alert-")
                .addClass("alert-danger")
                .append("<strong>El archivo que intenta subir es demasiado grande.</strong><br/>")
                .append("<strong>Su tama&ntilde;o debe ser menor que " + (maxUploadSize / 1024 / 1024) + " MB</strong></br>");
        }
    }
</script>
