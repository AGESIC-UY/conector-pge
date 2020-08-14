<footer>
    <p>Powered by Conector Spike y Kreitech - PGE V. 4.0</p>
</footer>

<script>
    function checkFileSize(inputFile) {
        $('#info').css('display', 'none');
        $('#info strong').remove();
        $('#info br').remove();

        var maxUploadSize = '<c:out value="${max_upload_size}"/>';
        if (inputFile.files && inputFile.files[0].size >= maxUploadSize) {
            inputFile.value = null;

            $('#info').css('display', 'block');
            $("#info").removeClass("alert-");
            $("#info").addClass("alert-danger");
            $("#info").append("<strong>El archivo que intenta subir es demasiado grande.</strong><br/>");
            $("#info").append("<strong>Su tama&ntilde;o debe ser menor que "+(maxUploadSize/1024/1024)+" MB</strong></br>");
        }
    }
</script>
