<%@ taglib prefix="th" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@include file="head.jsp" %>

<c:set var="subtitle" value="Informaci&oacute;n del Servicio"/>
<c:set var="esAlta" value="false"/>

<spring:url value="/connectors/connector/${connector.id}/keystoreOrg" var="keystoreOrg"/>
<spring:url value="/connectors/connector/${connector.id}/keystoreSsl" var="keystoreSsl"/>
<spring:url value="/connectors/connector/${connector.id}/truststore" var="truststore"/>
<spring:url value="/connectors/connector/${connector.id}/wsdl?download=true" var="wsdl"/>

<body>
<section class="main">
    <%@include file="navbar.jsp" %>

    <div class="content">
        <h2 class="title">${subtitle}</h2>
        <article class="margin-b-0">
            <div class="sw-container tab-content" style="min-height: 1000px;">
                <div id="view-connector" class="info-container">
                    <div class="col-sm-12 d-table">
                        <label class="control-label col-sm-2 d-table-cell">Nombre</label>
                        <div class="col-sm-10 d-table-cell">
                            <span name="name">${connector.name}</span>
                        </div>
                    </div>
                    <div class="col-sm-12 d-table">
                        <label class="control-label col-sm-2 d-table-cell">Tipo</label>
                        <div class="col-sm-10 d-table-cell">
                            <span name="type">${connector.type}</span>
                        </div>
                    </div>
                    <div class="col-sm-12 d-table">
                        <label class="control-label col-sm-2 d-table-cell">Versi&oacute;n SAML</label>
                        <div class="col-sm-10 d-table-cell">
                            <span name="samlVersion">${connector.samlVersion}</span>
                        </div>
                    </div>
                    <div class="col-sm-12 d-table">
                        <label class="control-label col-sm-2 d-table-cell">Descripci&oacute;n</label>
                        <div class="col-sm-10 d-table-cell">
                            <span name="description">${connector.description}</span>
                        </div>
                    </div>
                    <div class="col-sm-12 d-table">
                        <label class="control-label col-sm-2 d-table-cell">Path</label>
                        <div class="col-sm-10 d-table-cell">
                            <span name="path">${connector.path}</span>
                        </div>
                    </div>
                    <div class="col-sm-12 d-table">
                        <label class="control-label col-sm-2 d-table-cell">Url</label>
                        <div class="col-sm-10 d-table-cell">
                            <span name="url">${connector.url}</span>
                        </div>
                    </div>

                    <c:if test="${connector.multipleVersion == true}">
                        <div class="col-sm-12 d-table">
                            <label class="control-label col-sm-2 d-table-cell">Url Soap 1.2</label>
                            <div class="col-sm-10 d-table-cell">
                                <span name="urlV2">${connector.urlV2}</span>
                            </div>
                        </div>
                    </c:if>

                    <div class="col-sm-12 d-table">
                        <label class="control-label col-sm-2 d-table-cell">wsa:To</label>
                        <div class="col-sm-10 d-table-cell">
                            <span name="wsaTo">${connector.wsaTo}</span>
                        </div>
                    </div>
                    <div class="col-sm-12 d-table">
                        <label class="control-label col-sm-2 d-table-cell">Username</label>
                        <div class="col-sm-10 d-table-cell">
                            <span name="username">${connector.username}</span>
                        </div>
                    </div>
                    <div class="col-sm-12 d-table">
                        <label class="control-label col-sm-2 d-table-cell">Organismo</label>
                        <div class="col-sm-10 d-table-cell">
                            <span name="issuer">${connector.issuer}</span>
                        </div>
                    </div>
                    <div class="col-sm-12 d-table">
                        <label class="control-label col-sm-2 d-table-cell">Ingresar credenciales de Username
                            Token</label>
                        <div class="col-sm-10 d-table-cell">
                            <input type="checkbox" name="enableUserCredentials"
                                   id="enter_token_credentials"
                                    <c:if test="${connector.enableUserCredentials}"> checked</c:if> disabled="true"/>
                        </div>
                    </div>
                    <div class="col-sm-12 d-table">
                        <label class="control-label col-sm-2 d-table-cell">Habilitar Configuraci&oacute;n
                            de Certificado Local</label>
                        <div class="col-sm-10 d-table-cell">
                            <input type="checkbox" name="enableLocalConfiguration" id="enable_local_configuration"
                                   <c:if test="${connector.enableLocalConfiguration}">checked</c:if>
                                   disabled="true"/>
                        </div>
                    </div>
                    <div class="col-sm-12 d-table">
                        <label class="control-label col-sm-2 d-table-cell">Habilitar cach&eacute; de Tokens</label>
                        <div class="col-sm-10 d-table-cell">
                            <input type="checkbox" name="enableCacheTokens"
                                   <c:if test="${connector.enableCacheTokens}">checked</c:if> disabled="true"/>
                        </div>
                    </div>
                    <div class="col-sm-12 d-table">
                        <label class="control-label col-sm-2 d-table-cell">Publicar por HTTPS</label>
                        <div class="col-sm-10 d-table-cell">
                            <input type="checkbox" name="enableSsl"
                                   <c:if test="${connector.enableSsl}">checked</c:if> disabled="true"/>
                        </div>
                    </div>
                    <div class="col-sm-12 d-table">
                        <label class="control-label col-sm-2 d-table-cell">Usar configuraci&oacute;n
                            STS
                            local</label>
                        <div class="col-sm-10 d-table-cell">
                            <input type="checkbox" name="enableSTSLocal"
                                   id="enable_sts_local"
                                   <c:if test="${connector.enableSTSLocal}">checked</c:if> disabled="true"/>
                        </div>
                    </div>

                    <div class="col-sm-12 d-table">
                        <label class="control-label col-sm-2 d-table-cell">Usar Tipo de Token Local</label>
                        <div class="col-sm-10 d-table-cell">
                            <input type="checkbox" name="enableLocalPolicyName"
                                   id="enable_local_policy_name"
                                   <c:if test="${connector.enableLocalPolicyName}">checked</c:if> disabled="true"/>
                        </div>
                    </div>

                    <div class="col-sm-12 d-table">
                        <label class="control-label col-sm-2 d-table-cell">Habilitar notificaci&oacute;n local de
                            vencimiento</label>
                        <div class="col-sm-10 d-table-cell">
                            <input type="checkbox" name="enableLocalExpirationNotification"
                                   id="enable_local_expiration_notification"
                                   <c:if test="${connector.enableLocalExpirationNotification}">checked</c:if>
                                   disabled="true"/>
                        </div>
                    </div>

                    <div id="enter_token_credentials_div"
                         <c:if test="${connector.enableUserCredentials == false}">style="display: none;"</c:if>>
                        <div class="col-sm-12 d-table">
                            <label class="control-label col-sm-2 d-table-cell">Usuario Username Token</label>
                            <div class="col-sm-10 d-table-cell">
                                <span id="userNameTokenName"
                                      name="userCredentials.userNameTokenName">${connector.userCredentials.userNameTokenName}</span>
                            </div>
                        </div>
                        <div class="col-sm-12 d-table">
                            <label class="control-label col-sm-2 d-table-cell">Usuario Password
                                Token</label>
                            <div class="col-sm-10 d-table-cell">
                                <input class="border-0"
                                       type="password"
                                       id="userNameTokenPassword"
                                       name="userCredentials.userNameTokenPassword"
                                       readonly="true"
                                       value="${connector.userCredentials.userNameTokenPassword}">
                            </div>
                        </div>
                    </div>

                    <div id="enable_local_configuration_div"
                         <c:if test="${connector.enableLocalConfiguration == false}">style="display: none;"</c:if>>
                        <div class="col-sm-12 d-table">
                            <label class="control-label col-sm-2 d-table-cell">Alias del Keystore
                                Organismo</label>
                            <div class="col-sm-10 d-table-cell">
                                <span id="aliasKeystore"
                                      name="localConfiguration.aliasKeystore">${connector.localConfiguration.aliasKeystore}</span>
                            </div>
                        </div>
                        <div class="col-sm-12 d-table">
                            <label class="control-label col-sm-2 d-table-cell"
                                   path="localConfiguration.passwordKeystoreOrg">Password Keystore
                                Organismo</label>
                            <div class="col-sm-10 d-table-cell">
                                <input class="border-0" type="password" id="passwordKeystoreOrg"
                                       name="localConfiguration.passwordKeystoreOrg" readonly=true
                                       value="${connector.localConfiguration.passwordKeystoreOrg}"/>
                            </div>
                        </div>
                        <div class="col-sm-12 d-table">
                            <label class="control-label col-sm-2 d-table-cell">Alias del Keystore
                                SSL</label>
                            <div class="col-sm-10 d-table-cell">
                                <span id="aliasKeystoreSSL"
                                      name="localConfiguration.aliasKeystoreSSL">${connector.localConfiguration.aliasKeystoreSSL}</span>
                            </div>
                        </div>
                        <div class="col-sm-12 d-table">
                            <label class="control-label col-sm-2 d-table-cell">Password Keystore
                                SSL</label>
                            <div class="col-sm-10 d-table-cell">
                                <input class="border-0" type="password" id="passwordKeystoreSsl"
                                       name="localConfiguration.passwordKeystoreSsl" readonly=true
                                       value="${connector.localConfiguration.passwordKeystoreSsl}"/>
                            </div>
                        </div>
                        <div class="col-sm-12 d-table">
                            <label class="control-label col-sm-2 d-table-cell">Password
                                Truststore</label>
                            <div class="col-sm-10 d-table-cell">
                                <input class="border-0" type="password" id="passwordKeystore"
                                       name="localConfiguration.passwordKeystore" readonly=true
                                       value="${connector.localConfiguration.passwordKeystore}"/>
                            </div>
                        </div>
                        <div class="col-sm-12 d-table">
                            <label class="control-label col-sm-2 d-table-cell">Keystore Organismo
                            </label>
                            <div class="col-sm-10 d-table-cell">
                                <a href="${keystoreOrg}" class="pl-0 btn btn-link btn-xs active" role="button">
                                    <img width="12px"
                                         height="10px"
                                         src="${pageContext.request.contextPath}/resources/images/icon-down-arrow.svg"
                                         alt="Descargar Keystore Organismo"
                                         title="Descargar Keystore Organismo">
                                    Descargar
                                </a>

                                <c:if test="${connector.expKeystoreOrgStatus == 'EXPIRE_SOON'}">
                                    <img width="20px"
                                         height="20px"
                                         src="${pageContext.request.contextPath}/resources/images/icon-exp-warn.svg"
                                         alt="Certificado pr&oacute;ximo a vencer"
                                         title="Certificado pr&oacute;ximo a vencer"/>
                                </c:if>

                                <c:if test="${connector.expKeystoreOrgStatus == 'EXPIRED'}">
                                    <img width="20px"
                                         height="20px"
                                         src="${pageContext.request.contextPath}/resources/images/icon-exp-error.svg"
                                         alt="Certificado vencido" title="Certificado vencido"/>
                                </c:if>

                                <c:if test="${not empty connector.expDateKeystoreOrg}">
                                    <p class="help-block"><strong>V&aacute;lido
                                        hasta:</strong> ${connector.expDateKeystoreOrg}</p>
                                </c:if>
                                <p class="help-block linebreak">
                                    <strong>Ruta:</strong> ${connector.localConfiguration.dirKeystoreOrg}</p>
                            </div>
                        </div>
                        <div class="col-sm-12 d-table">
                            <label class="control-label col-sm-2 d-table-cell">Keystore SSL
                            </label>
                            <div class="col-sm-10 d-table-cell">
                                <a href="${keystoreSsl}" class="pl-0 btn btn-link btn-xs active" role="button">
                                    <img width="12px"
                                         height="10px"
                                         src="${pageContext.request.contextPath}/resources/images/icon-down-arrow.svg"
                                         alt="Descargar Keystore SSL"
                                         title="Descargar Keystore SSL">
                                    Descargar
                                </a>

                                <c:if test="${connector.expKeystoreSSLStatus == 'EXPIRE_SOON'}">
                                    <img width="20px"
                                         height="20px"
                                         src="${pageContext.request.contextPath}/resources/images/icon-exp-warn.svg"
                                         alt="Certificado pr&oacute;ximo a vencer"
                                         title="Certificado pr&oacute;ximo a vencer"/>
                                </c:if>

                                <c:if test="${connector.expKeystoreSSLStatus == 'EXPIRED'}">
                                    <img width="20px"
                                         height="20px"
                                         src="${pageContext.request.contextPath}/resources/images/icon-exp-error.svg"
                                         alt="Certificado vencido" title="Certificado vencido"/>
                                </c:if>

                                <p class="help-block"><strong>V&aacute;lido
                                    hasta:</strong> ${connector.expDateKeystoreSSL}</p>
                                <p class="help-block linebreak">
                                    <strong>Ruta:</strong> ${connector.localConfiguration.dirKeystoreSsl}</p>
                            </div>
                        </div>
                        <div class="col-sm-12 d-table">
                            <label class="control-label col-sm-2 d-table-cell">Truststore SSL</label>
                            <div class="col-sm-10 d-table-cell">
                                <a href="${truststore}" class="pl-0 btn btn-link btn-xs active" role="button">
                                    <img width="12px"
                                         height="10px"
                                         src="${pageContext.request.contextPath}/resources/images/icon-down-arrow.svg"
                                         alt="Descargar Truststore"
                                         title="Descargar Truststore">
                                    Descargar
                                </a>

                                <c:if test="${connector.expKeystoreTruststoreStatus == 'EXPIRE_SOON'}">
                                    <img width="20px"
                                         height="20px"
                                         src="${pageContext.request.contextPath}/resources/images/icon-exp-warn.svg"
                                         alt="Certificado pr&oacute;ximo a vencer"
                                         title="Certificado pr&oacute;ximo a vencer"/>
                                </c:if>

                                <c:if test="${connector.expKeystoreTruststoreStatus == 'EXPIRED'}">
                                    <img width="20px"
                                         height="20px"
                                         src="${pageContext.request.contextPath}/resources/images/icon-exp-error.svg"
                                         alt="Certificado vencido" title="Certificado vencido"/>
                                </c:if>

                                <c:if test="${not empty connector.expDateKeystoreTruststore}">
                                <p class="help-block"><strong>V&aacute;lido
                                    hasta:</strong> ${connector.expDateKeystoreTruststore}
                                    </c:if>
                                </p>
                                <p class="help-block linebreak">
                                    <strong>Ruta:</strong> ${connector.localConfiguration.dirKeystore}
                                </p>
                            </div>
                        </div>
                    </div>

                    <div class="col-sm-12 d-table">
                        <label class="control-label col-sm-2 d-table-cell">WSDL
                        </label>
                        <div class="col-sm-10 d-table-cell">
                            <a href="${wsdl}" class="pl-0 btn btn-link btn-xs active" role="button">
                                <img width="12px"
                                     height="10px"
                                     src="${pageContext.request.contextPath}/resources/images/icon-down-arrow.svg"
                                     alt="Descargar WSDL"
                                     title="Descargar WSDL">
                                Descargar
                            </a>
                        </div>
                    </div>

                    <div id="enable_sts_local_url_div"
                         <c:if test="${connector.enableSTSLocal == false}">style="display: none;"</c:if>>
                        <div class="col-sm-12 d-table mt-10">
                            <label class="control-label col-sm-2 d-table-cell">URL STS Local</label>
                            <div class="col-sm-10 d-table-cell">
                                <span id="stsLocalUrl"
                                      name="stsLocalUrl">${connector.stsLocalUrl}</span>
                            </div>
                        </div>
                    </div>

                    <div id="enable_local_policy_name_div"
                         <c:if test="${connector.enableLocalPolicyName == false}">style="display: none;"</c:if>>
                        <div class="col-sm-12 d-table mt-10">
                            <label class="control-label col-sm-2 d-table-cell">Tipo de token local</label>
                            <div class="col-sm-10 d-table-cell">
                                <span id="policyName"
                                      name="policyName">${connector.policyName}</span>
                            </div>
                        </div>
                    </div>

                    <div id="enable_local_timeout_div"
                         <c:if test="${connector.enableLocalServiceTimeOut == false}">style="display: none;"</c:if>>
                        <div class="col-sm-12 d-table mt-10">
                            <label class="control-label col-sm-2 d-table-cell">Timeout en milisegundos *</label>
                            <div class="col-sm-10 d-table-cell">
                                <span id="localServiceTimeOut"
                                      name="localServiceTimeOut">${connector.localServiceTimeOut}</span>
                            </div>

                        </div>
                    </div>

                    <div id="enable_local_expiration_notice_days_div"
                         <c:if test="${connector.enableLocalExpirationNotification == false}">style="display: none;"</c:if>>
                        <div class="col-sm-12 d-table mt-10">
                            <label class="control-label col-sm-2 d-table-cell">D&iacute;as previos al aviso *</label>
                            <div class="col-sm-10 d-table-cell">
                                <span id="localExpirationNoticeDays"
                                      name="localExpirationNoticeDays">${connector.localExpirationNoticeDays}</span>
                            </div>

                        </div>
                    </div>

                    <div class="col-sm-12 d-table mt-10">
                        <label class="control-label col-sm-2 d-table-cell">TAG</label>
                        <div class="col-sm-10 d-table-cell">
                            <span name="tag">${connector.tag}</span>
                        </div>
                    </div>

                    <div class="col-sm-12">
                        <div class="rich-panel-header">Rol - Operaci&oacute;n</div>
                        <div class="align-right">


                            <c:if test="${connector.multipleVersion == true}">

                            <!-- Nav tabs -->
                            <ul class="nav nav-tabs" role="tablist" style="margin-left:17px">
                                <li role="presentation" class="active"><a href="#one" aria-controls="one" role="tab"
                                                                          data-toggle="tab">Soap 1.1</a></li>
                                <li role="presentation"><a href="#two" aria-controls="two" role="tab" data-toggle="tab">Soap
                                    1.2</a></li>
                            </ul>

                            <!-- Tab panes -->
                            <div class="tab-content">
                                <div role="tabpanel" class="tab-pane active" id="one">

                                    </c:if>

                                    <table <c:if test="${connector.multipleVersion == true}">class="w-100"</c:if>>
                                        <tbody>

                                        <tr>
                                            <td>
                                                <table class="table-role-op" border="0" cellpadding="0" cellspacing="0"
                                                       width="100%">
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

                                                        <c:if test="${operation.soapVersion == '1.1'}">

                                                            <tr>
                                                                <td>
                                                                    <div class="col-sm-12">
                                                                        <span name="roleOperations[${i.index}].role">${operation.role}</span>
                                                                    </div>
                                                                </td>
                                                                <td class="linebreak">
                                                                    <div class="col-sm-12">
                                                                        <span name="roleOperations[${i.index}].wsaAction">${operation.wsaAction}</span>
                                                                    </div>
                                                                </td>
                                                                <td>
                                                                    <div class="col-sm-12">
                                                                        <span name="roleOperations[${i.index}].operationFromWSDL">${operation.operationFromWSDL}</span>
                                                                    </div>
                                                                </td>
                                                            </tr>

                                                        </c:if>

                                                    </c:forEach>
                                                    </thead>
                                                </table>
                                            </td>
                                        </tr>

                                        </tbody>
                                    </table>

                                    <c:if test="${connector.multipleVersion == true}">
                                </div>
                                <div role="tabpanel" class="tab-pane" id="two">

                                    <table class="w-100">
                                        <tbody>

                                        <tr>
                                            <td>
                                                <table class="table-role-op" border="0" cellpadding="0" cellspacing="0"
                                                       width="100%">
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

                                                        <c:if test="${operation.soapVersion == '1.2'}">

                                                            <tr>
                                                                <td>
                                                                    <div class="col-sm-12">
                                                                        <span name="roleOperations[${i.index}].role">${operation.role}</span>
                                                                    </div>
                                                                </td>
                                                                <td class="linebreak">
                                                                    <div class="col-sm-12">
                                                                        <span name="roleOperations[${i.index}].wsaAction">${operation.wsaAction}</span>
                                                                    </div>
                                                                </td>
                                                                <td>
                                                                    <div class="col-sm-12">
                                                                        <span name="roleOperations[${i.index}].operationFromWSDL">${operation.operationFromWSDL}</span>
                                                                    </div>
                                                                </td>
                                                            </tr>

                                                        </c:if>

                                                    </c:forEach>
                                                    </thead>
                                                </table>
                                            </td>
                                        </tr>

                                        </tbody>
                                    </table>

                                </div>
                            </div>
                            </c:if>

                        </div>

                    </div>

                    <div class="align-right col-sm-12" style="margin-top: 20px;">
                        <div class="col-sm-3" style="padding-left: 0;">
                            <spring:url value="/connectors"
                                        var="cancelUrl"/>
                            <form action="${cancelUrl}"
                                  method="get"
                                  enctype="multipart/form-data">
                                <input type="submit" value="Volver" class="btn-input">
                            </form>
                        </div>

                        <div class="col-sm-3">
                            <spring:url value="/connectors/connector/${connector.id}/edit"
                                        var="editUrl"/>
                            <form:form action="${editUrl}"
                                       method="get"
                                       enctype="multipart/form-data">
                                <input type="submit" value="Editar" class="btn-input">
                            </form:form>
                        </div>

                        <div class="col-sm-3" style="text-align: right;">
                            <spring:url value="/connectors/connector/${connector.id}/delete"
                                        var="deleteUrl"/>
                            <input type="submit" value="Borrar" class="btn-input" data-toggle="modal"
                                   data-target="#delete_confirmation_${connector.id}">
                        </div>

                        <div class="col-sm-3" style="padding-right: 0; text-align: right;">
                            <spring:url value="/connectors/connector/${connector.id}/export"
                                        var="exportUrl"/>
                            <form:form action="${exportUrl}"
                                       method="get"
                                       enctype="multipart/form-data">
                                <input type="submit" value="Exportar" class="btn-input">
                            </form:form>
                        </div>
                    </div>

                    <div class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
                         id="delete_confirmation_${connector.id}"
                         aria-labelledby="delete_confirmation_${connector.id}">
                        <div class="modal-dialog modal-sm" role="document">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <h4 class="modal-title" id="gridSystemModalLabel">&iquest;Seguro que desea borrar el
                                        servicio?</h4>
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

                <div class="clearfix"></div>
            </div>
        </article>
    </div>
</section>

<%@include file="footer.jsp" %>

<script type="text/javascript">
    $(document).ready(function () {

        // Step show event
        $("#smartwizard").on("showStep", function (e, anchorObject, stepNumber, stepDirection, stepPosition) {
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
