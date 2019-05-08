<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@include file="head.jsp" %>

<body>
<section class="main">
    <header></header>
    <%@include file="navbar.jsp" %>

    <div class="content">
        <h2 class="title">Importar Conector</h2>
        <article class="">
            <form enctype="multipart/form-data"
                  action="${pageContext.request.contextPath}/connectors/import"
                  id="formuploadajax" method="post">
                <input id="uploaded_file" type="file" name="uploaded_file"
                       onchange="ValidateSingleInput(this);" required/>
                <input type="submit" class="btn-input" value="Importar"
                       onclick="ValidateSingleInput(document.getElementById('uploaded_file'));"/>
            </form>
        </article>

        <article class="">
            <form action="${pageContext.request.contextPath}/connectors/add" method="get" enctype="multipart/form-data">
                <input type="submit" value="Nuevo Conector" class="pull-right btn-input"/>
            </form>

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
                <div class="form-group">
                    <label>Tag:</label>
                    <input class="form-control" type="text" name="tag"
                           size="40" value="${tag}"/>
                </div>
                <input type="image" src="${pageContext.request.contextPath}/resources/images/icon-search.svg" name=""
                       title="Filtrar por tag"
                       class="btn-search">

                <div class="text-center">

                </div>
            </form>
        </article>

        <article class="">
            <table class="table table-striped table-hover table-condensed">
                <thead>
                <tr>
                    <th>Nombre</th>
                    <th>Descripci&oacute;n</th>
                    <th>Path</th>
                    <th>Acciones</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="connector" items="${connectors}">
                    <tr>
                        <td><p class="overflow ellipsis" data-toggle="tooltip" data-placement="bottom" title="${connector.name}">${connector.name}</p></td>
                        <td><p class="overflow-description ellipsis" data-toggle="tooltip" data-placement="bottom" title="${connector.description}">${connector.description}</p></td>
                        <td><p class="overflow ellipsis" data-toggle="tooltip" data-placement="bottom" title="${connector.path}">${connector.path}<p></td>
                        <td class="td-actions">
                            <spring:url value="/connectors/connector/${connector.id}" var="viewUrl"/>
                            <spring:url value="/connectors/connector/${connector.id}/edit" var="editUrl"/>
                            <spring:url value="/connectors/connector/${connector.id}/delete" var="deleteUrl"/>
                            <spring:url value="/connectors/connector/${connector.id}/wsdl" var="wsdlPathUrl"/>
                            <spring:url value="/connectors/connector/${connector.id}/export" var="exportUrl"/>

                            <a href="${viewUrl}"><img
                                    src="${pageContext.request.contextPath}/resources/images/icon-search.svg" alt="Ver conector"
                                    title="Ver Conector"></a>

                            <a href="${editUrl}"><img
                                    src="${pageContext.request.contextPath}/resources/images/icon-pencil.svg"
                                    alt="Editar conector" title="Editar Conector"></a>

                            <a href="#"><img
                                    src="${pageContext.request.contextPath}/resources/images/icon-error.svg"
                                    data-toggle="modal" data-target="#delete_confirmation_${connector.id}"
                                    alt="Borrar conector" title="Borrar Conector"></a>

                            <a href="${wsdlPathUrl}"
                               target="_blank">WSDL</a>
                            <a href="${exportUrl}">Exportar</a>

                            <div class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
                                 id="delete_confirmation_${connector.id}"
                                 aria-labelledby="delete_confirmation_${connector.id}">
                                <div class="modal-dialog modal-sm" role="document">
                                    <div class="modal-content">
                                        <div class="modal-header">
                                            <h4 class="modal-title" id="gridSystemModalLabel">&iquest;Seguro que desea borrar el
                                                conector?</h4>
                                        </div>
                                        <div class="modal-footer">
                                            <button type="button" class="btn btn-default" data-dismiss="modal">No</button>
                                            <button type="button" class="btn btn-danger" onclick="location.href='${deleteUrl}'">
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

        <nav aria-label="Page navigation" class="text-center">
            <ul class="pagination">
                <c:choose>
                    <c:when test="${filtered}">
                        <li>
                            <c:choose>
                                <c:when test="${actual_page == 1}">
                                    <a href="${pageContext.request.contextPath}/connectors/filtered/1?type=${type}&tag=${tag}""
                                    aria-label="Previous">
                                    <span aria-hidden="true">&laquo;</span>
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/connectors/filtered/${actual_page - 1}?type=${type}&tag=${tag}""
                                    aria-label="Previous">
                                    <span aria-hidden="true">&laquo;</span>
                                    </a>
                                </c:otherwise>
                            </c:choose>
                        </li>
                        <c:forEach begin="1" end="${total_pages}" varStatus="loop">
                            <li>
                                <a href="${pageContext.request.contextPath}/connectors/filtered/${loop.index}?type=${type}&tag=${tag}">${loop.index}</a>
                            </li>
                        </c:forEach>
                        <li>
                            <c:choose>
                                <c:when test="${actual_page == total_pages}">
                                    <a href="${pageContext.request.contextPath}/connectors/filtered/${total_pages}?type=${type}&tag=${tag}""
                                    aria-label="Next">
                                    <span aria-hidden="true">&raquo;</span>
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/connectors/filtered/${actual_page + 1}?type=${type}&tag=${tag}""
                                    aria-label="Next">
                                    <span aria-hidden="true">&raquo;</span>
                                    </a>
                                </c:otherwise>
                            </c:choose>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li>
                            <c:choose>
                                <c:when test="${actual_page == 1}">
                                    <a href="${pageContext.request.contextPath}/connectors/1"
                                       aria-label="Previous">
                                        <span aria-hidden="true">&laquo;</span>
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/connectors/${actual_page - 1}"
                                       aria-label="Previous">
                                        <span aria-hidden="true">&laquo;</span>
                                    </a>
                                </c:otherwise>
                            </c:choose>
                        </li>
                        <c:forEach begin="1" end="${total_pages}" varStatus="loop">
                            <li><a href="${pageContext.request.contextPath}/connectors/${loop.index}">${loop.index}</a>
                            </li>
                        </c:forEach>
                        <li>
                            <c:choose>
                                <c:when test="${actual_page == total_pages}">
                                    <a href="${pageContext.request.contextPath}/connectors/${total_pages}"
                                       aria-label="Next">
                                        <span aria-hidden="true">&raquo;</span>
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/connectors/${actual_page + 1}"
                                       aria-label="Next">
                                        <span aria-hidden="true">&raquo;</span>
                                    </a>
                                </c:otherwise>
                            </c:choose>
                        </li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </nav>

    </div>
</section>

<footer>
    <p>Powered by Pyxis PGE V. 3.0</p>
</footer>

</body>
</html>
