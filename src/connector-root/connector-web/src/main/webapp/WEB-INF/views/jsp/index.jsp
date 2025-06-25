<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@include file="head.jsp" %>

<body>
<section class="main">
    <%@include file="navbar.jsp" %>

    <div class="content">
        <h2 class="title">Importar Servicio</h2>
        <article class="row">
            <div class="col-sm-12 col-md-6">
                <form enctype="multipart/form-data"
                      action="${pageContext.request.contextPath}/connectors/import"
                      id="formuploadajax" method="post">
                    <input id="uploaded_file" class="pl-0 col-md-8" type="file" name="uploaded_file"
                           onchange="checkFileSize(this);" required/>
                    <input type="submit" class="btn-input" value="Importar"
                           onclick="ValidateSingleInput(document.getElementById('uploaded_file'));"/>
                </form>
            </div>
        </article>

        <article class="connector-table_atop">
            <form action="${pageContext.request.contextPath}/connectors/add" method="get" enctype="multipart/form-data">
                <input type="submit" value="Nuevo Servicio" class="pull-right btn-input"/>
            </form>
        </article>

        <article class="pl-0 col-sm-12 col-md-3 connector-table_atop">
            <form action="${pageContext.request.contextPath}/connectors/filtered" method="get"
                  enctype="multipart/form-data"
                  class="form-inline">
                <div class="form-group">
                    <label>Tipo:</label>
                    <select class="form-control" size="1" name="type" onchange="this.form.submit()">
                        <c:choose>
                            <c:when test="${type == 'Testing'}">
                                <option value="Produccion">Producci&oacute;n</option>
                                <option value="Testing" selected="selected">Testing</option>
                            </c:when>
                            <c:otherwise>
                                <option value="Produccion" selected="selected">Producci&oacute;n</option>
                                <option value="Testing">Testing</option>
                            </c:otherwise>
                        </c:choose>
                    </select>
                </div>
            </form>
        </article>

        <article id="connector-table_container">
            <table id="connector-table" class="table table-striped table-hover table-condensed">
                <thead>
                <tr>
                    <th class="th-sm">Nombre
                    </th>
                    <th class="th-sm">Tag
                    </th>
                    <th class="th-sm">Descripci&oacute;n
                    </th>
                    <th class="th-sm">Path
                    </th>
                    <th class="th-sm">Acciones
                    </th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="connector" items="${connectors}">
                    <tr>
                        <td><span data-toggle="tooltip" data-placement="bottom"
                                  title="${connector.name}">${connector.name}</span></td>
                        <td><span data-toggle="tooltip" data-placement="bottom"
                                  title="${connector.tag}">${connector.tag}</span></td>
                        <td><span data-toggle="tooltip" data-placement="bottom"
                                  title="${connector.description}">${connector.description}</span></td>
                        <td><span data-toggle="tooltip" data-placement="bottom"
                                  title="${connector.path}">${connector.path}</span></td>
                        <td class="td-actions">
                            <spring:url value="/connectors/connector/${connector.id}" var="viewUrl"/>
                            <spring:url value="/connectors/connector/${connector.id}/edit" var="editUrl"/>
                            <spring:url value="/connectors/connector/${connector.id}/delete" var="deleteUrl"/>
                            <spring:url value="${connector.wsdlUrlForUI}" var="wsdlPathUrl"/>
                            <spring:url value="/connectors/connector/${connector.id}/export" var="exportUrl"/>

                            <a href="${viewUrl}"><img
                                    src="${pageContext.request.contextPath}/resources/images/icon-search.svg"
                                    alt="Ver servicio"
                                    title="Ver Servicio"></a>

                            <a href="${editUrl}"><img
                                    src="${pageContext.request.contextPath}/resources/images/icon-pencil.svg"
                                    alt="Editar servicio" title="Editar Servicio"></a>

                            <a href="#"><img
                                    src="${pageContext.request.contextPath}/resources/images/icon-error.svg"
                                    data-toggle="modal" data-target="#delete_confirmation_${connector.id}"
                                    alt="Borrar servicio" title="Borrar Servicio"></a>

                            <a href="${wsdlPathUrl}"
                               target="_blank">WSDL</a>
                            <a href="${exportUrl}">Exportar</a>

                            <c:if test="${connector.expirationStatus == 'EXPIRE_SOON'}">
                                <c:if test="${connector.enableLocalConfiguration == 'true'}">
                                    <img width="20px"
                                         height="20px"
                                         src="${pageContext.request.contextPath}/resources/images/icon-exp-warn.svg"
                                         alt="Certificado pr&oacute;ximo a vencer"
                                         title="Certificado pr&oacute;ximo a vencer"/>
                                </c:if>
                                <c:if test="${connector.enableLocalConfiguration == 'false'}">
                                    <img width="20px"
                                         height="20px"
                                         src="${pageContext.request.contextPath}/resources/images/icon-exp-warn.svg"
                                         alt="Certificado global pr&oacute;ximo a vencer"
                                         title="Certificado global pr&oacute;ximo a vencer"/>
                                </c:if>
                            </c:if>

                            <c:if test="${connector.expirationStatus == 'EXPIRED'}">
                                <c:if test="${connector.enableLocalConfiguration == 'true'}">
                                    <img width="20px"
                                         height="20px"
                                         src="${pageContext.request.contextPath}/resources/images/icon-exp-error.svg"
                                         alt="Certificado vencido" title="Certificado vencido"/>
                                </c:if>
                                <c:if test="${connector.enableLocalConfiguration == 'false'}">
                                    <img width="20px"
                                         height="20px"
                                         src="${pageContext.request.contextPath}/resources/images/icon-exp-error.svg"
                                         alt="Certificado global vencido" title="Certificado global vencido"/>
                                </c:if>
                            </c:if>

                            <div class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
                                 id="delete_confirmation_${connector.id}"
                                 aria-labelledby="delete_confirmation_${connector.id}">
                                <div class="modal-dialog modal-sm" role="document">
                                    <div class="modal-content">
                                        <div class="modal-header">
                                            <h4 class="modal-title" id="gridSystemModalLabel">&iquest;Seguro que desea
                                                borrar el
                                                servicio?</h4>
                                        </div>
                                        <div class="modal-footer">
                                            <button type="button" class="btn btn-default" data-dismiss="modal">No
                                            </button>
                                            <button type="button" class="btn btn-danger"
                                                    onclick="location.href='${deleteUrl}'">
                                                Si
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>

                        </td>
                    </tr>
                </c:forEach>

                </tbody>
            </table>
        </article>

    </div>
</section>

<%@include file="footer.jsp" %>

<script type="text/javascript">
    $(document).ready(function () {
        $('#connector-table').DataTable({
            "columnDefs": [{
                "targets": [1],
                "visible": false
            }],
            "language": {
                "paginate": {
                    "previous": "<<",
                    "next": ">>"
                }
            },
            "fnDrawCallback": function (oSettings) {
                if ($('#connector-table tr').length < 2 || $(".dataTables_empty")[0] !== undefined) {
                    $('.dataTables_paginate').hide();
                } else {
                    $('.dataTables_paginate').show();
                }
            }
        });
        $('.dataTables_length').addClass('bs-select');
    });

</script>

</body>
</html>
