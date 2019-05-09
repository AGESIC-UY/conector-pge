<%@ taglib prefix="th" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@include file="head.jsp" %>
<c:set var="subtitle" value="Informaci&oacute;n del Conector"/>
<c:set var="esAlta" value="false"/>
<spring:url value="/connectors/connector/${connector.id}/keystoreOrg" var="keystoreOrg"/>
<spring:url value="/connectors/connector/${connector.id}/keystoreSsl" var="keystoreSsl"/>
<spring:url value="/connectors/connector/${connector.id}/truststore" var="truststore"/>
<spring:url value="/connectors/connector/${connector.id}/wsdl?download=true" var="wsdl"/>
<body>
<section class="main">
    <header></header>
    <%@include file="navbar.jsp" %>

    <div class="content">
        <h2 class="title">${subtitle}</h2>
        <article class="">
            <div class="sw-container tab-content" style="min-height: 1000px;">
                <div>
                    <!--<h2>Informaci&oacute;n del Conector</h2>-->
                    <div class="col-sm-10">
                        <label class="control-label col-sm-2">Nombre</label>
                        <div class="col-sm-8">
                            <span name="name">${connector.name}</span>
                        </div>
                    </div>
                    <div class="col-sm-10">
                        <label class="control-label col-sm-2">Tipo</label>
                        <div class="col-sm-8">
                            <span name="type">${connector.type}</span>
                        </div>
                    </div>
                    <div class="col-sm-10">
                        <label class="control-label col-sm-2">Descripci&oacute;n</label>
                        <div class="col-sm-8">
                            <span name="description">${connector.description}</span>
                        </div>
                    </div>
                    <div class="col-sm-10">
                        <label class="control-label col-sm-2">Path</label>
                        <div class="col-sm-8">
                            <span name="path">${connector.path}</span>
                        </div>
                    </div>
                    <div class="col-sm-10">
                        <label class="control-label col-sm-2">Url</label>
                        <div class="col-sm-8">
                            <span name="url">${connector.url}</span>
                        </div>
                    </div>
                    <div class="col-sm-10">
                        <label class="control-label col-sm-2">wsa:To</label>
                        <div class="col-sm-8">
                            <span name="wsaTo">${connector.wsaTo}</span>
                        </div>
                    </div>
                    <div class="col-sm-10">
                        <label class="control-label col-sm-2">Username</label>
                        <div class="col-sm-8">
                            <span name="username">${connector.username}</span>
                        </div>
                    </div>
                    <div class="col-sm-10">
                        <label class="control-label col-sm-2">Organismo</label>
                        <div class="col-sm-8">
                            <span name="issuer">${connector.issuer}</span>
                        </div>
                    </div>
                    <div class="col-sm-10">
                        <label class="control-label col-sm-2">Ingresar credenciales de Username Token</label>
                        <div class="col-sm-8">
                            <input type="checkbox" name="enableUserCredentials"
                                   id="enter_token_credentials"
                                    <c:if test="${connector.enableUserCredentials}"> checked</c:if> disabled="true"/>
                        </div>
                    </div>
                    <div class="col-sm-10">
                        <label class="control-label col-sm-2">Habilitar Configuraci&oacute;n
                            Local</label>
                        <div class="col-sm-8">
                            <input type="checkbox" name="enableLocalConfiguration" id="enable_local_configuration"
                                   <c:if test="${connector.enableLocalConfiguration}">checked</c:if>
                                   disabled="true"/>
                        </div>
                    </div>
                    <div class="col-sm-10">
                        <label class="control-label col-sm-2">Habilitar cach&eacute; de Tokens</label>
                        <div class="col-sm-8">
                            <input type="checkbox" name="enableCacheTokens"
                                   <c:if test="${connector.enableCacheTokens}">checked</c:if> disabled="true"/>
                        </div>
                    </div>
                    <div class="col-sm-10">
                        <label class="control-label col-sm-2">Publicar por HTTPS</label>
                        <div class="col-sm-8">
                            <input type="checkbox" name="enableSsl"
                                   <c:if test="${connector.enableSsl}">checked</c:if> disabled="true"/>
                        </div>
                    </div>
                    <div class="col-sm-10">
                        <label class="control-label col-sm-2">Usar configuraci&oacute;n
                            STS
                            local</label>
                        <div class="col-sm-8">
                            <input type="checkbox" name="enableSTSLocal"
                                   id="enable_sts_local"
                                   <c:if test="${connector.enableSTSLocal}">checked</c:if> disabled="true"/>
                        </div>
                    </div>

                    <div id="enter_token_credentials_div"
                         <c:if test="${connector.enableUserCredentials == false}">style="display: none;"</c:if>>
                        <div class="col-sm-10">
                            <label class="control-label col-sm-2">Usuario Username Token</label>
                            <div class="col-sm-10">
                                <span id="userNameTokenName"
                                      name="userCredentials.userNameTokenName">${connector.userCredentials.userNameTokenName}</span>
                            </div>
                        </div>
                        <div class="col-sm-10">
                            <label class="control-label col-sm-2">Usuario Password
                                Token</label>
                            <div class="col-sm-10">
                                <span type="password" id="userNameTokenPassword"
                                      name="userCredentials.userNameTokenPassword">
                                    ${connector.userCredentials.userNameTokenPassword}</span>
                            </div>
                        </div>
                    </div>

                    <div id="enable_local_configuration_div"
                         <c:if test="${connector.enableLocalConfiguration == false}">style="display: none;"</c:if>>
                        <div class="col-sm-10">
                            <label class="control-label col-sm-2">Alias del Keystore
                                Organismo</label>
                            <div class="col-sm-10">
                                <span id="aliasKeystore"
                                      name="localConfiguration.aliasKeystore">${connector.localConfiguration.aliasKeystore}</span>
                            </div>
                        </div>
                        <div class="col-sm-10">
                            <label class="control-label col-sm-2"
                                   path="localConfiguration.passwordKeystoreOrg">Password Keystore
                                Organismo</label>
                            <div class="col-sm-10">
                                <span type="password" id="passwordKeystoreOrg"
                                      name="localConfiguration.passwordKeystoreOrg">${connector.localConfiguration.passwordKeystoreOrg}</span>
                            </div>
                        </div>
                        <div class="col-sm-10">
                            <label class="control-label col-sm-2">Password Keystore
                                SSL</label>
                            <div class="col-sm-10">
                                <span type="password" id="passwordKeystoreSsl"
                                      name="localConfiguration.passwordKeystoreSsl">${connector.localConfiguration.passwordKeystoreSsl}</span>
                            </div>
                        </div>
                        <div class="col-sm-10">
                            <label class="control-label col-sm-2">Password
                                Truststore</label>
                            <div class="col-sm-10">
                                <span type="password" id="passwordKeystore"
                                      name="localConfiguration.passwordKeystore">${connector.localConfiguration.passwordKeystore}</span>
                            </div>
                        </div>
                        <div class="col-sm-10">
                            <label class="control-label col-sm-2">Keystore Organismo
                            </label>
                            <div class="col-sm-10">
                                <a href="${keystoreOrg}"><img height="25px"
                                                              src="${pageContext.request.contextPath}/resources/images/icon-down-arrow.svg"
                                                              alt="Descargar Keystore Organismo"
                                                              title="Descargar Keystore Organismo"></a>
                            </div>
                        </div>
                        <div class="col-sm-10">
                            <label class="control-label col-sm-2">Keystore SSL
                            </label>
                            <div class="col-sm-10">
                                <a href="${keystoreSsl}"><img height="25px"
                                                              src="${pageContext.request.contextPath}/resources/images/icon-down-arrow.svg"
                                                              alt="Descargar Keystore SSL"
                                                              title="Descargar Keystore SSL"></a>
                            </div>
                        </div>
                        <div class="col-sm-10">
                            <label class="control-label col-sm-2">Truststore SSL
                            </label>
                            <div class="col-sm-10">
                                <a href="${truststore}"><img height="25px"
                                                             src="${pageContext.request.contextPath}/resources/images/icon-down-arrow.svg"
                                                             alt="Descargar Truststore"
                                                             title="Descargar Truststore"></a>
                            </div>
                        </div>
                    </div>

                    <div class="col-sm-10">
                        <label class="control-label col-sm-2">WSDL
                        </label>
                        <div class="col-sm-10">
                            <a href="${wsdl}"><img height="25px"
                                                   src="${pageContext.request.contextPath}/resources/images/icon-down-arrow.svg"
                                                   alt="Descargar WSDL" title="Descargar WSDL"></a>
                        </div>
                    </div>

                    <div id="enable_sts_local_url_div"
                         <c:if test="${connector.enableSTSLocal == false}">style="display: none;"</c:if>>
                        <div class="col-sm-10">
                            <label class="control-label col-sm-2">URL STS Local</label>
                            <div class="col-sm-10">
                                <span id="stsLocalUrl"
                                      name="stsLocalUrl">${connector.stsLocalUrl}</span>
                            </div>
                        </div>
                    </div>

                    <div class="col-sm-10">
                        <label class="control-label col-sm-2">TAG</label>
                        <div class="col-sm-10">
                            <span name="tag">${connector.tag}</span>
                        </div>
                    </div>

                    <div class="col-sm-10">
                        <div class="rich-panel-header">Rol - Operaci&oacute;n</div>
                        <div>
                            <table style="width:100%">
                                <tbody>
                                <tr>
                                    <td><img alt="" class="rich-spacer" height="1" width="50px">
                                    </td>
                                    <td>
                                        <table border="0" cellpadding="0" cellspacing="0" width="100%">
                                            <colgroup span="3"></colgroup>
                                            <thead>
                                            <tr>
                                                <th>Rol</th>
                                                <th>wsa:Action</th>
                                                <th>Operaci&oacute;n
                                                </th>
                                            </tr>

                                            <c:forEach var="operation" items="${connector.roleOperations}"
                                                       varStatus="i">
                                                <tr>
                                                    <td>
                                                        <div class="col-sm-10">
                                                            <span name="roleOperations[${i.index}].role">${operation.role}</span>
                                                        </div>
                                                    </td>
                                                    <td class="linebreak">
                                                        <div class="col-sm-10">
                                                            <span name="roleOperations[${i.index}].wsaAction">${operation.wsaAction}</span>
                                                        </div>
                                                    </td>
                                                    <td>
                                                        <div class="col-sm-10">
                                                            <span name="roleOperations[${i.index}].operationFromWSDL">${operation.operationFromWSDL}</span>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                            </thead>
                                        </table>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                        <p class="col-sm-10">
                        <div class="col-sm-offset-1 col-sm-2">
                            <spring:url value="/connectors"
                                        var="cancelUrl"/>
                            <form action="${cancelUrl}"
                                  method="get"
                                  enctype="multipart/form-data">
                                <input type="submit" value="Volver" class="btn-input">
                            </form>
                        </div>

                        <div class="col-sm-offset-1 col-sm-2">
                            <spring:url value="/connectors/connector/${connector.id}/edit"
                                        var="editUrl"/>
                            <form:form action="${editUrl}"
                                       method="get"
                                       enctype="multipart/form-data">
                                <input type="submit" value="Editar" class="btn-input">
                            </form:form>
                        </div>

                        <div class="col-sm-offset-1 col-sm-2">
                            <spring:url value="/connectors/connector/${connector.id}/delete"
                                        var="deleteUrl"/>
                            <input type="submit" value="Borrar" class="btn-input" data-toggle="modal"
                                   data-target="#delete_confirmation_${connector.id}">
                        </div>

                        <div class="col-sm-offset-1 col-sm-2">
                            <spring:url value="/connectors/connector/${connector.id}/export"
                                        var="exportUrl"/>
                            <form:form action="${exportUrl}"
                                       method="get"
                                       enctype="multipart/form-data">
                                <input type="submit" value="Exportar" class="btn-input">
                            </form:form>
                        </div>
                        </p>

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
                    </div>
                </div>
            </div>
        </article>

    </div>
</section>

<footer>
    <p>Powered by Pyxis PGE V. 3.0</p>
</footer>
<script type="text/javascript">
    $(document).ready(function () {

        // Step show event
        $("#smartwizard").on("showStep", function (e, anchorObject, stepNumber, stepDirection, stepPosition) {
            //alert("You are on step "+stepNumber+" now");
            if (stepPosition === 'first') {
                $("#prev-btn").addClass('disabled');
            } else if (stepPosition === 'final') {
                $("#next-btn").addClass('disabled');
            } else {
                $("#prev-btn").removeClass('disabled');
                $("#next-btn").removeClass('disabled');
            }
        });

        // Smart Wizard
        $('#smartwizard').smartWizard({
            selected: 1,
            theme: 'dots',
            transitionEffect: 'fade',
            showStepURLhash: false,
            lang: {
                next: 'Siguiente',
                previous: 'Anterior',
            },
            toolbarSettings: {
                toolbarPosition: 'none',
                toolbarButtonPosition: 'center',
            }
        });
    });
</script>

</body>
</html>
