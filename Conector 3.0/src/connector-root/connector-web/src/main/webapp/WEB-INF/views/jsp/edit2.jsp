<%@ taglib prefix="th" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@include file="head.jsp" %>

<spring:url value="/connectors/connector/${connector.id}/keystoreOrg" var="keystoreOrg"/>
<spring:url value="/connectors/connector/${connector.id}/keystoreSsl" var="keystoreSsl"/>
<spring:url value="/connectors/connector/${connector.id}/truststore" var="truststore"/>
<spring:url value="/connectors/connector/${connector.id}/wsdl" var="wsdl"/>
<body onload="initializeRequired();">
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
        <h2 class="title">${subtitle}</h2>
        <article class="">
            <div id="smartwizard">
                <ul>
                    <li><a href="#step-1">Paso 1
                        <small>Subir WSDL o ZIP</small>
                    </a></li>
                    <li><a href="#step-2">Paso 2
                        <small>Completar informaci&oacute;n Conector</small>
                    </a></li>
                </ul>
                <div>
                    <div id="step-2" class="">
                        <h2>Informaci&oacute;n del Conector</h2>
                        <c:choose>
                            <c:when test="${esAlta == true}">
                                <c:set var="formAction"
                                       value="${pageContext.request.contextPath}/connectors/add"/>
                            </c:when>
                            <c:otherwise>
                                <c:set var="formAction"
                                       value="${pageContext.request.contextPath}/connectors/connector/${connector.id}/update"/>
                            </c:otherwise>
                        </c:choose>
                        <form:form class="form-horizontal" name="form_info_conector" id="form_info_conector"
                                   action="${formAction}"
                                   method="post"
                                   enctype="multipart/form-data" modelAttribute="connector">
                            <input type="text" name="prefixNameConnector" value="${prefixNameConnector}" hidden/>
                            <div class="form-group">
                                <form:label class="control-label col-sm-2" for="name" path="name">Nombre *</form:label>
                                <div class="col-sm-10">
                                    <form:input class="form-control" type="text" name="name"
                                                value="${connector.name}"
                                                placeholder="Nombre conector" path="name" required="required" maxlength="100"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <form:label class="control-label col-sm-2" for="type" path="type">Tipo<c:if
                                        test="${esAlta == true}"> *</c:if></form:label>
                                <div class="col-sm-10">
                                    <form:select class="form-control" type="text" name="type" id="type"
                                                 path="type">
                                        <option value="Produccion"
                                                <c:if test="${connector.type == 'Produccion'}">selected</c:if>
                                        >Producci&oacute;n
                                        </option>
                                        <option value="Testing"
                                                <c:if test="${connector.type == 'Testing'}">selected</c:if>
                                        >Testing
                                        </option>
                                    </form:select>
                                </div>
                            </div>
                            <div class="form-group">
                                <form:label class="control-label col-sm-2" for="description"
                                            path="description">Descripci&oacute;n</form:label>
                                <div class="col-sm-10">
                                            <textarea class="form-control" rows="5" name="description"
                                                      value=""
                                                      placeholder="Descripci&oacute;n"
                                                      path="description"
                                                      maxlength="800">${connector.description}</textarea>
                                </div>
                            </div>
                            <div class="form-group">
                                <form:label class="control-label col-sm-2" for="path" path="path">Path *</form:label>
                                <div class="col-sm-10">
                                    <form:input class="form-control" type="text" name="path" path="path"
                                                value="${connector.path}"
                                                placeholder="Path" onkeypress="removeWhitespaces(this)"
                                                onchange="this.onkeypress()"
                                                required="required" maxlength="512"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <form:label class="control-label col-sm-2" for="url" path="url">Url *</form:label>
                                <div class="col-sm-10">
                                    <form:input class="form-control" type="text" name="url" path="url"
                                                value="${connector.url}" onkeypress="removeWhitespaces(this)"
                                                onchange="this.onkeypress()"
                                                placeholder="URL" required="required" maxlength="512"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <form:label class="control-label col-sm-2" for="wsaTo" path="wsaTo">wsa:To *</form:label>
                                <div class="col-sm-10">
                                    <form:input class="form-control" type="text" name="wsaTo" path="wsaTo"
                                                value="${connector.wsaTo}" onkeypress="removeWhitespaces(this)"
                                                onchange="this.onkeypress()"
                                                placeholder="wsa:To" required="required" maxlength="512"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <form:label class="control-label col-sm-2" for="username"
                                            path="username">Username *</form:label>
                                <div class="col-sm-10">
                                    <form:input class="form-control" type="text" name="username" path="username"
                                                value="${connector.username}"
                                                placeholder="Username" required="required" maxlength="100"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <form:label class="control-label col-sm-2" for="issuer" path="issuer">Organismo *</form:label>
                                <div class="col-sm-10">
                                    <form:input class="form-control" type="text" name="issuer" path="issuer"
                                                value="${connector.issuer}"
                                                placeholder="Organismo" required="required" maxlength="100"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="control-label col-sm-2" for="enter_token_credentials">Ingresar credenciales de
                                    Username
                                    Token</label>
                                <div class="col-sm-2">
                                    <form:checkbox class="form-control" name="enableUserCredentials"
                                                   path="enableUserCredentials"
                                                   id="enter_token_credentials"
                                                   onclick="toggleEnableUserCredentials();"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="control-label col-sm-2" for="enableLocalConfiguration">Habilitar Configuraci&oacute;n
                                    Local</label>
                                <div class="col-sm-2">
                                    <form:checkbox class="form-control" name="enableLocalConfiguration"
                                                   path="enableLocalConfiguration"
                                                   id="enable_local_configuration"
                                                   onclick="toggleEnableConfigurations();"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <form:label class="control-label col-sm-2" for="enableCacheTokens"
                                            path="enableCacheTokens">Habilitar cach&eacute; de Tokens</form:label>
                                <div class="col-sm-2">
                                    <form:checkbox class="form-control" name="enableCacheTokens"
                                                   path="enableCacheTokens"
                                                   value="${connector.enableCacheTokens}"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <form:label class="control-label col-sm-2" for="enableSsl"
                                            path="enableSsl">Publicar por HTTPS</form:label>
                                <div class="col-sm-2">
                                    <form:checkbox class="form-control" name="enableSsl"
                                                   path="enableSsl"
                                                   value="${connector.enableSsl}"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="control-label col-sm-2" for="enableSTSLocal">Usar configuraci&oacute;n
                                    STS
                                    local</label>
                                <div class="col-sm-2">
                                    <form:checkbox class="form-control" name="enableSTSLocal"
                                                   path="enableSTSLocal"
                                                   id="enable_sts_local"
                                                   onclick="toggleEnableSTSLocalUrl();"/>
                                </div>
                            </div>

                            <div id="enter_token_credentials_div"
                                 <c:if test="${connector.enableUserCredentials == false}">style="display: none;"</c:if>>
                                <div class="form-group">
                                    <form:label class="control-label col-sm-2" for="username_token"
                                                path="userCredentials.userNameTokenName">Usuario Username Token *</form:label>
                                    <div class="col-sm-10">
                                        <form:input class="form-control" type="text" id="userNameTokenName"
                                                    name="userCredentials.userNameTokenName"
                                                    path="userCredentials.userNameTokenName"
                                                    value="${connector.userCredentials.userNameTokenName}"
                                                    placeholder="Usuario Username Token"/>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <form:label class="control-label col-sm-2" for="password_token"
                                                path="userCredentials.userNameTokenPassword">Usuario Password
                                        Token *</form:label>
                                    <div class="col-sm-10">
                                        <form:input class="form-control" type="password" id="userNameTokenPassword"
                                                    name="userCredentials.userNameTokenPassword"
                                                    path="userCredentials.userNameTokenPassword"
                                                    value="${connector.userCredentials.userNameTokenPassword}"
                                                    placeholder="Usuario Password Token" autocomplete="off"/>
                                    </div>
                                </div>
                            </div>

                            <div id="enable_local_configuration_div"
                                 <c:if test="${connector.enableLocalConfiguration == false}">style="display: none;"</c:if>>
                                <div class="form-group">
                                    <form:label class="control-label col-sm-2" for="alias_issuer_keystore"
                                                path="localConfiguration.aliasKeystore">Alias del Keystore
                                        Organismo *</form:label>
                                    <div class="col-sm-10">
                                        <form:input class="form-control" type="text" id="aliasKeystore"
                                                    name="localConfiguration.aliasKeystore"
                                                    path="localConfiguration.aliasKeystore"
                                                    value="${connector.localConfiguration.aliasKeystore}"
                                                    placeholder="Alias del Keystore Organismo" maxlength="100"/>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <form:label class="control-label col-sm-2" for="password_issuer_keystore"
                                                path="localConfiguration.passwordKeystoreOrg">Password Keystore
                                        Organismo *</form:label>
                                    <div class="col-sm-10">
                                        <form:input class="form-control" type="password" id="passwordKeystoreOrg"
                                                    name="localConfiguration.passwordKeystoreOrg"
                                                    path="localConfiguration.passwordKeystoreOrg"
                                                    value="${connector.localConfiguration.passwordKeystoreOrg}"
                                                    placeholder="Password Keystore Organismo" autocomplete="off" maxlength="50"/>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <form:label class="control-label col-sm-2" for="password_ssl_keystore"
                                                path="localConfiguration.passwordKeystoreSsl">Password Keystore
                                        SSL *</form:label>
                                    <div class="col-sm-10">
                                        <form:input class="form-control" type="password" id="passwordKeystoreSsl"
                                                    name="localConfiguration.passwordKeystoreSsl"
                                                    path="localConfiguration.passwordKeystoreSsl"
                                                    value="${connector.localConfiguration.passwordKeystoreSsl}"
                                                    placeholder="Password Keystore SSL" autocomplete="off" maxlength="50"/>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <form:label class="control-label col-sm-2" for="password_truststore"
                                                path="localConfiguration.passwordKeystore">Password
                                        Truststore *</form:label>
                                    <div class="col-sm-10">
                                        <form:input class="form-control" type="password" id="passwordKeystore"
                                                    name="localConfiguration.passwordKeystore"
                                                    path="localConfiguration.passwordKeystore"
                                                    value="${connector.localConfiguration.passwordKeystore}"
                                                    placeholder="Password Truststore" autocomplete="off" maxlength="50"/>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <form:label class="control-label col-sm-2" for="issuer_keystore"
                                                path="localConfiguration.dirKeystoreOrg">Keystore Organismo
                                        <c:if test="${esAlta == true}"> *</c:if>
                                    </form:label>
                                    <div class="col-sm-10">
                                        <input type="file" id="keystoreOrgFile" name="keystoreOrgFile"
                                               placeholder="Keystore Organismo File"/>
                                        <input hidden="hidden"
                                               value="${connector.localConfiguration.dirKeystoreOrg}"/>
                                        <c:if test="${esAlta == false}">
                                            <a href="${keystoreOrg}"><img style="height:25px"
                                                                          src="${pageContext.request.contextPath}/resources/images/icon-down-arrow.svg"
                                                                          alt="Descargar Keystore Organismo"
                                                                          title="Descargar Keystore Organismo"></a>

                                            <a href="#"><img style="height:25px"
                                                             src="${pageContext.request.contextPath}/resources/images/icon-search.svg"
                                                             alt="Ver detalles keystore"
                                                             data-toggle="modal"
                                                             data-target="#keystore_modal_keystoreOrgFile"
                                                             title="Ver detalles keystore"></a>
                                        </c:if>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <form:label class="control-label col-sm-2" for="ssl_keystore"
                                                path="localConfiguration.dirKeystoreSsl">Keystore SSL
                                        <c:if test="${esAlta == true}"> *</c:if>
                                    </form:label>
                                    <div class="col-sm-10">
                                        <input type="file" id="keystoreSSLFile" name="keystoreSSLFile"
                                               placeholder="Keystore SSL File"/>
                                        <input hidden="hidden"
                                               value="${connector.localConfiguration.dirKeystoreSsl}"/>
                                        <c:if test="${esAlta == false}">
                                            <a href="${keystoreSsl}"><img style="height:25px"
                                                                          src="${pageContext.request.contextPath}/resources/images/icon-down-arrow.svg"
                                                                          alt="Descargar Keystore SSL"
                                                                          title="Descargar Keystore SSL"></a>
                                            <a href="#"><img style="height:25px"
                                                             src="${pageContext.request.contextPath}/resources/images/icon-search.svg"
                                                             data-toggle="modal"
                                                             data-target="#keystore_modal_keystoreSSLFile"
                                                             alt="Ver detalles keystore"
                                                             title="Ver detalles keystore"></a>
                                        </c:if>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <form:label class="control-label col-sm-2" for="ssl_truststore"
                                                path="localConfiguration.dirKeystore">Truststore SSL
                                        <c:if test="${esAlta == true}"> *</c:if>
                                    </form:label>
                                    <div class="col-sm-10">
                                        <input type="file" id="keystoreTruststoreFile" name="keystoreTruststoreFile"
                                               placeholder="Keystore Trust File"/>
                                        <input hidden="hidden"
                                               value="${connector.localConfiguration.dirKeystore}"/>
                                        <c:if test="${esAlta == false}">
                                            <a href="${truststore}"><img style="height:25px"
                                                                         src="${pageContext.request.contextPath}/resources/images/icon-down-arrow.svg"
                                                                         alt="Descargar Truststore"
                                                                         title="Descargar Truststore"></a>
                                            <a href="#"><img
                                                    style="height:25px"
                                                    src="${pageContext.request.contextPath}/resources/images/icon-search.svg"
                                                    alt="Ver detalles Truststore"
                                                    data-toggle="modal" data-target="#keystore_modal_keystoreFile"
                                                    title="Ver detalles Truststore"></a>
                                        </c:if>
                                    </div>
                                </div>
                                <c:if test="${esAlta == false}">
                                    <div class="form-group">
                                        <form:label class="control-label col-sm-2" for="ssl_truststore"
                                                    path="localConfiguration.dirKeystore">WSDL
                                        </form:label>
                                        <div class="col-sm-10">
                                            <a href="${wsdl}"><img height="25px"
                                                                   src="${pageContext.request.contextPath}/resources/images/icon-down-arrow.svg"
                                                                   alt="Descargar WSDL" title="Descargar WSDL"></a>
                                        </div>
                                    </div>
                                </c:if>
                            </div>

                            <div id="enable_sts_local_url_div"
                                 <c:if test="${connector.enableSTSLocal == false}">style="display: none;"</c:if>>
                                <div class="form-group">
                                    <form:label class="control-label col-sm-2" for="sts_local_url"
                                                path="stsLocalUrl">URL STS Local *</form:label>
                                    <div class="col-sm-10">
                                        <form:input class="form-control" type="text" id="stsLocalUrl"
                                                    name="stsLocalUrl"
                                                    path="stsLocalUrl"
                                                    value="${connector.stsLocalUrl}"
                                                    onkeypress="removeWhitespaces(this)"
                                                    placeholder="URL STS Local" maxlength="512"/>
                                    </div>
                                </div>
                            </div>

                            <div class="form-group">
                                <form:label class="control-label col-sm-2" for="tag" path="tag">TAG</form:label>
                                <div class="col-sm-10">
                                    <form:input class="form-control" type="text" name="tag" path="tag"
                                                value="${connector.tag}"
                                                placeholder="Tag" autocomplete="off" maxlength="100"/>
                                </div>
                            </div>

                            <div class="form-group">
                                <div class="rich-panel-header">Rol - Operaci&oacute;n</div>
                                <div>
                                    <table style="width:70%">
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
                                                                    <input class="form-control" type="text"
                                                                           name="roleOperations[${i.index}].role"
                                                                           value="${operation.role}"/>
                                                                </div>
                                                            </td>
                                                            <td>
                                                                <div class="col-sm-10">
                                                                    <input class="form-control" type="text"
                                                                           name="roleOperations[${i.index}].wsaAction"
                                                                           value="${operation.wsaAction}"/>
                                                                </div>
                                                            </td>
                                                            <td>
                                                                <div class="col-sm-10">
                                                                    <input class="form-control" type="text"
                                                                           name="roleOperations[${i.index}].operationFromWSDL"
                                                                           value="${operation.operationFromWSDL}" readonly/>
                                                                </div>
                                                            </td>
                                                            <input type="text"
                                                                   name="roleOperations[${i.index}].operationInputName"
                                                                   value="${operation.operationInputName}" hidden/>
                                                        </tr>
                                                    </c:forEach>
                                                    </thead>
                                                    <tbody></tbody>
                                                </table>
                                            </td>
                                        </tr>
                                        </tbody>
                                    </table>
                                </div>

                            </div>

                            <c:forEach var="keystoreModalData" items="${keystoreModalDataColl}">
                                <div class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
                                     id="keystore_modal_${keystoreModalData.nombre}"
                                     aria-labelledby="keystore_modal_${keystoreModalData.nombre}">
                                    <div class="modal-dialog modal-sm" role="document">
                                        <div class="modal-content">
                                            <div class="modal-header">
                                                <button type="button" class="close" data-dismiss="modal">&times;</button>
                                                <h4 class="modal-title"
                                                    id="keystoreModalLabel_${keystoreModalData.nombre}">${keystoreModalData.nombreModal}</h4>
                                            </div>
                                            <div class="modal-body">
                                                <c:forEach var="certificado" items="${keystoreModalData.certificados}">
                                                    <table>
                                                        <tbody>
                                                        <tr>
                                                            <td>Alias:</td>
                                                            <td>${certificado.alias}</td>
                                                        </tr>
                                                        <tr>
                                                            <td>Tipo:</td>
                                                            <td>${certificado.tipo}</td>
                                                        </tr>
                                                        <tr>
                                                            <td>Proveedor:</td>
                                                            <td>${certificado.proveedor}</td>
                                                        </tr>
                                                        <tr>
                                                            <td>Fecha Creacion:</td>
                                                            <td>${certificado.fechaCreacion}</td>
                                                        </tr>
                                                        <tr>
                                                            <td>Fecha Vencimiento:</td>
                                                            <td>${certificado.fechaVencimiento}</td>
                                                        </tr>
                                                        </tbody>
                                                    </table>
                                                    <hr>
                                                </c:forEach>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </form:form>

                        <c:choose>
                            <c:when test="${esAlta == true}">
                                <spring:url value="/connectors/connector/${connector.id}/cancel"
                                            var="cancelUrl"/>
                            </c:when>
                            <c:otherwise>
                                <spring:url value="/connectors/connector/cancel"
                                            var="cancelUrl"/>
                                <spring:url value="/connectors/connector/${connector.id}/delete"
                                            var="deleteUrl"/>
                                <spring:url value="/connectors/connector/${connector.id}/testToProd"
                                            var="testToProdUrl"/>
                            </c:otherwise>
                        </c:choose>
                        <div class="form-group" style="padding-top: 10px;">
                            <div class="col-sm-3">
                                <form:form action="${cancelUrl}"
                                           method="post"
                                           enctype="multipart/form-data">
                                    <input type="text" name="prefixNameConnector" value="${prefixNameConnector}" hidden/>
                                    <input type="submit" value="Cancelar" class="btn-input">
                                </form:form>
                            </div>
                            <div class="col-sm-3">
                                <c:choose>
                                    <c:when test="${esAlta == true}">
                                        <input type="submit" name="button_alta" value="Alta" class="btn-input"
                                               form="form_info_conector">
                                    </c:when>
                                    <c:otherwise>
                                        <input type="submit" name="button_actualizar" value="Actualizar"
                                               class="btn-input" form="form_info_conector" onclick="preSubmit()">
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <c:if test="${esAlta == false}">
                                <div class="col-sm-3">
                                    <input type="submit" value="Borrar" class="btn-input" data-toggle="modal"
                                           data-target="#delete_confirmation_${connector.id}">
                                </div>
                                <c:if test="${connector.type == 'Testing'}">
                                    <div class="col-sm-3">
                                        <form:form action="${testToProdUrl}"
                                                   method="get"
                                                   enctype="multipart/form-data">
                                            <input type="submit" value="Pasar a Producci&oacute;n" class="btn-input">
                                        </form:form>
                                    </div>
                                </c:if>
                                <div class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
                                     id="delete_confirmation_${connector.id}"
                                     aria-labelledby="delete_confirmation_${connector.id}">
                                    <div class="modal-dialog modal-sm" role="document">
                                        <div class="modal-content">
                                            <div class="modal-header">
                                                <h4 class="modal-title" id="gridSystemModalLabel">&iquest;Seguro que desea borrar
                                                    el conector?</h4>
                                            </div>
                                            <div class="modal-footer">
                                                <button type="button" class="btn btn-default" data-dismiss="modal">No</button>
                                                <button type="button" class="btn btn-danger"
                                                        onclick="location.href='${deleteUrl}'">Si
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:if>
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

<script>
    function initializeRequired() {
        var esAlta = '<c:out value="${esAlta}"/>';
        var enableUserCredentials = '<c:out value="${connector.enableUserCredentials}"/>';
        var enableLocalConfiguration = '<c:out value="${connector.enableLocalConfiguration}"/>';
        var enableSTSLocal = '<c:out value="${connector.enableSTSLocal}"/>';
        if (enableUserCredentials == "true") {
            toggleRequiredUserCredentials();
        }
        if (enableLocalConfiguration == "true") {
            toggleRequiredConfigurations();
        }
        if (enableSTSLocal == "true") {
            toggleRequiredSTSLocal();
        }
        if (esAlta == "false") {
            $("#type").prop('disabled', true);
        }
    }
    ;
    function toggleRequiredConfigurations() {
        toggleRequiredNotRequired('aliasKeystore');
        toggleRequiredNotRequired('passwordKeystoreOrg');
        toggleRequiredNotRequired('passwordKeystoreSsl');
        toggleRequiredNotRequired('passwordKeystore');
        <c:if test="${esAlta == true}">
        toggleRequiredNotRequired('keystoreOrgFile');
        toggleRequiredNotRequired('keystoreSSLFile');
        toggleRequiredNotRequired('keystoreTruststoreFile');
        </c:if>
    }
</script>

</body>
</html>
