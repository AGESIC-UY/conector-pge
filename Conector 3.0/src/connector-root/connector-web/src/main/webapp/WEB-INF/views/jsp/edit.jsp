<%@include file="head.jsp" %>

<body>
<section class="main">
    <header></header>
    <%@include file="navbar.jsp" %>

    <c:choose>
        <c:when test="${esAlta == true}">
            <c:set var="subtitle"
                   value="Alta de Conector"/>
        </c:when>
        <c:otherwise>
            <c:set var="subtitle"
                   value="Editar Conector"/>
        </c:otherwise>
    </c:choose>

    <div class="content">
        <div class="content">
            <h2 class="title">${subtitle}</h2>
            <article class="">
                <!-- SmartWizard html -->
                <div id="smartwizard">
                    <ul>
                        <li><a href="#step-1">Paso 1
                            <small>Subir WSDL o ZIP</small>
                        </a></li>
                        <li><a href="#step-2">Paso 2
                            <small>Completar informaci&oacute;n Conector</small>
                        </a></li>
                    </ul>
                    <div id="step-1" class="">
                        <h2>Sub&iacute; tu WSDL</h2>
                        <article class="">
                            <c:choose>
                                <c:when test="${connector.id != null}">
                                    <c:set var="formAction"
                                           value="${pageContext.request.contextPath}/connectors/connector/${connector.id}/add/uploadFile"/>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="formAction"
                                           value="${pageContext.request.contextPath}/connectors/add/uploadFile"/>
                                </c:otherwise>
                            </c:choose>
                            <form enctype="multipart/form-data"
                                  action="${formAction}"
                                  id="formuploadajax" method="post">
                                <div class="col-sm-offset-1 col-sm-2">
                                    <input id="uploaded_file" type="file" name="uploaded_file"
                                           onchange="ValidateSingleInput(this);" required/>
                                </div>
                                <div class="col-sm-offset-5 col-sm-2">
                                    <input type="submit" class="btn-input" value="Subir archivos"
                                           onclick="ValidateSingleInput(document.getElementById('uploaded_file'));"/>
                                </div>
                                <p class="col-sm-offset-1 col-sm-2">
                                    <c:choose>
                                        <c:when test="${connector.id != null}">
                                            <input type="submit" class="btn-input"
                                                   value="Mantener archivos"
                                                   onclick="mantainFiles()"/>
                                        </c:when>
                                    </c:choose>
                                </p>
                            </form>

                            <div class="col-sm-10">
                                <div class="col-sm-offset-1 col-sm-2">
                                    <spring:url value="/connectors"
                                                var="cancelUrl"/>
                                    <form action="${cancelUrl}"
                                          method="get"
                                          enctype="multipart/form-data">
                                        <input type="submit" value="Cancelar" class="btn-input">
                                    </form>
                                </div>
                            </div>
                        </article>
                    </div>
                </div>
            </article>
        </div>
    </div>
</section>

<footer>
    <p>Powered by Pyxis PGE V. 3.0</p>
</footer>
<script type="text/javascript">
    $(document).ready(function () {
        initializeSmartWizard();
    });
</script>

<script>
    function mantainFiles() {
        <c:set var="formAction" value="${pageContext.request.contextPath}/connectors/add/uploadFile"/>;
        document.getElementById('uploaded_file').required = false;
    }
</script>
</body>
</html>
