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
    <%@include file="navbar.jsp" %>

    <c:choose>
        <c:when test="${esAlta == true}">
            <c:set var="subtitle"
                   value="Alta de Servicio"/>
        </c:when>
        <c:otherwise>
            <c:set var="subtitle"
                   value="Editar Servicio"/>
        </c:otherwise>
    </c:choose>

    <div class="content">
        <h2 class="title">${subtitle}</h2>
        <article class="content">
            <div id="smartwizard">
                <ul>
                    <li><a href="#step-1">Paso 1
                        <small>Subir WSDL o ZIP</small>
                    </a></li>
                    <li><a href="#step-2">Paso 2
                        <small>Completar informaci&oacute;n del Servicio</small>
                    </a></li>
                </ul>
                <div>
                    <div id="step-2" class="">
                        <h3 class="text-center">Informaci&oacute;n del Servicio</h3>
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
                            <input type="text" name="multipleVersion" value="${connector.multipleVersion}" hidden/>
                            <div class="form-group col-sm-12 d-table">
                                <form:label class="control-label col-sm-2 d-table-cell" for="name"
                                            path="name">Nombre *</form:label>
                                <div class="col-sm-10 d-table-cell">
                                    <form:input class="form-control" type="text" name="name"
                                                value="${connector.name}"
                                                placeholder="Nombre del servicio" path="name" required="required"
                                                maxlength="100"/>
                                </div>
                            </div>
                            <div class="form-group col-sm-12 d-table">
                                <form:label class="control-label col-sm-2 d-table-cell" for="type" path="type">Tipo<c:if
                                        test="${esAlta == true}"> *</c:if></form:label>
                                <div class="col-sm-10 d-table-cell">
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
                            <div class="form-group col-sm-12 d-table">
                                <form:label class="control-label col-sm-2 d-table-cell" for="samlVersion"
                                            path="samlVersion">Versi&oacute;n SAML<c:if
                                        test="${esAlta == true}"> *</c:if></form:label>
                                <div class="col-sm-10 d-table-cell">
                                    <form:select class="form-control" type="text" name="type" id="samlVersion"
                                                 path="samlVersion">
                                        <option value="1.1"
                                                <c:if test="${connector.samlVersion == '1.1'}">selected</c:if>
                                        >1.1
                                        </option>
                                        <option value="2.0"
                                                <c:if test="${connector.samlVersion == '2.0'}">selected</c:if>
                                        >2.0
                                        </option>
                                    </form:select>
                                </div>
                            </div>
                            <div class="form-group col-sm-12 d-table">
                                <form:label class="control-label col-sm-2 d-table-cell" for="description"
                                            path="description">Descripci&oacute;n</form:label>
                                <div class="col-sm-10 d-table-cell">
                                            <textarea class="form-control" rows="5" name="description"
                                                      value=""
                                                      placeholder="Descripci&oacute;n"
                                                      path="description"
                                                      maxlength="800">${connector.description}</textarea>
                                </div>
                            </div>
                            <div class="form-group col-sm-12 d-table">
                                <form:label class="control-label col-sm-2 d-table-cell" for="path"
                                            path="path">Path *</form:label>
                                <div class="col-sm-10 d-table-cell">
                                    <form:input class="form-control" type="text" name="path" path="path"
                                                value="${connector.path}"
                                                placeholder="Path" onkeypress="removeWhitespaces(this)"
                                                onchange="this.onkeypress()"
                                                required="required" maxlength="512"/>
                                </div>
                            </div>
                            <div class="form-group col-sm-12 d-table">
                                <form:label class="control-label col-sm-2 d-table-cell" for="url"
                                            path="url">Url *</form:label>
                                <div class="col-sm-10 d-table-cell">
                                    <form:input class="form-control" type="text" name="url" path="url"
                                                value="${connector.url}" onkeypress="removeWhitespaces(this)"
                                                onchange="this.onkeypress()"
                                                placeholder="URL" required="required" maxlength="512"/>
                                </div>
                            </div>

                            <c:if test="${connector.multipleVersion == true}">
                                <div class="form-group col-sm-12 d-table">
                                    <form:label class="control-label col-sm-2 d-table-cell" for="urlV2"
                                                path="urlV2">Url Soap 1.2 *</form:label>
                                    <div class="col-sm-10 d-table-cell">
                                        <form:input class="form-control" type="text" name="urlV2" path="urlV2"
                                                    value="${connector.urlV2}" onkeypress="removeWhitespaces(this)"
                                                    onchange="this.onkeypress()"
                                                    placeholder="URL V2" required="required" maxlength="512"/>
                                    </div>
                                </div>
                            </c:if>

                            <div class="form-group col-sm-12 d-table">
                                <form:label class="control-label col-sm-2 d-table-cell" for="wsaTo"
                                            path="wsaTo">wsa:To *</form:label>
                                <div class="col-sm-10 d-table-cell">
                                    <form:input class="form-control" type="text" name="wsaTo" path="wsaTo"
                                                value="${connector.wsaTo}" onkeypress="removeWhitespaces(this)"
                                                onchange="this.onkeypress()"
                                                placeholder="wsa:To" required="required" maxlength="512"/>
                                </div>
                            </div>
                            <div class="form-group col-sm-12 d-table">
                                <form:label class="control-label col-sm-2 d-table-cell" for="username"
                                            path="username">Username *</form:label>
                                <div class="col-sm-10 d-table-cell">
                                    <form:input class="form-control" type="text" name="username" path="username"
                                                value="${connector.username}"
                                                placeholder="Username" required="required" maxlength="100"/>
                                </div>
                            </div>
                            <div class="form-group col-sm-12 d-table">
                                <form:label class="control-label col-sm-2 d-table-cell" for="issuer"
                                            path="issuer">Organismo *</form:label>
                                <div class="col-sm-10 d-table-cell">
                                    <form:input class="form-control" type="text" name="issuer" path="issuer"
                                                value="${connector.issuer}"
                                                placeholder="Organismo" required="required" maxlength="100"/>
                                </div>
                            </div>
                            <div class="form-group col-sm-12 d-table">
                                <label class="control-label col-sm-2 d-table-cell" for="enter_token_credentials">Ingresar
                                    credenciales de
                                    Username
                                    Token</label>
                                <div class="col-sm-2 d-table-cell w-input">
                                    <form:checkbox class="form-control" name="enableUserCredentials"
                                                   path="enableUserCredentials"
                                                   id="enter_token_credentials"
                                                   onclick="toggleEnableUserCredentials();"/>
                                </div>
                                <div class="col-sm-8"></div>
                            </div>
                            <div class="form-group col-sm-12 d-table">
                                <label class="control-label col-sm-2 d-table-cell" for="enableLocalConfiguration">Habilitar
                                    Configuraci&oacute;n
                                    de Certificado Local</label>
                                <div class="col-sm-2 d-table-cell w-input">

                                    <c:choose>
                                        <c:when test="${isGlobalConfigurationEnabled == false}">
                                            <form:checkbox class="form-control" name="enableLocalConfiguration"
                                                           path="enableLocalConfiguration"
                                                           id="enable_local_configuration"
                                                           onclick="toggleEnableConfigurations();"
                                                           style="visibility: hidden;"/>
                                            <input type="checkbox"
                                                   class="form-control"
                                                   disabled checked
                                                   style="position: absolute;top: 35%;"/>

                                            <span data-toggle="tooltip" data-placement="bottom"
                                                  title="No se ha definido la configuraci&oacute;n global para '${connector.type}'. Se debe definir una configuraci&oacute;n local."
                                                  style="position: absolute;right: -20px;top: 35%;">
                                                <i class="fa fa-question-circle"></i>
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <form:checkbox class="form-control" name="enableLocalConfiguration"
                                                           path="enableLocalConfiguration"
                                                           id="enable_local_configuration"
                                                           onclick="toggleEnableConfigurations();"/>
                                        </c:otherwise>
                                    </c:choose>

                                </div>
                                <div class="col-sm-8"></div>
                            </div>
                            <div class="form-group col-sm-12 d-table">
                                <form:label class="control-label col-sm-2 d-table-cell" for="enableCacheTokens"
                                            path="enableCacheTokens">Habilitar cach&eacute; de Tokens</form:label>
                                <div class="col-sm-2 d-table-cell w-input">
                                    <form:checkbox class="form-control" name="enableCacheTokens"
                                                   path="enableCacheTokens"
                                                   value="${connector.enableCacheTokens}"/>
                                </div>
                                <div class="col-sm-8"></div>
                            </div>
                            <div class="form-group col-sm-12 d-table">
                                <form:label class="control-label col-sm-2 d-table-cell" for="enableSsl"
                                            path="enableSsl">Publicar por HTTPS</form:label>
                                <div class="col-sm-2 d-table-cell w-input">
                                    <form:checkbox class="form-control" name="enableSsl"
                                                   path="enableSsl"
                                                   value="${connector.enableSsl}"/>
                                </div>
                                <div class="col-sm-8"></div>
                            </div>
                            <div class="form-group col-sm-12 d-table">
                                <label class="control-label col-sm-2 d-table-cell" for="enableSTSLocal">Usar configuraci&oacute;n
                                    STS
                                    local</label>
                                <div class="col-sm-2 d-table-cell w-input">
                                    <c:choose>
                                        <c:when test="${isGlobalConfigurationEnabled == false}">
                                            <form:checkbox class="form-control" name="enableSTSLocal"
                                                           path="enableSTSLocal"
                                                           id="enable_sts_local"
                                                           onclick="toggleEnableSTSLocalUrl();"
                                                           style="visibility: hidden;"/>
                                            <input type="checkbox"
                                                   class="form-control"
                                                   disabled checked
                                                   style="position: absolute;top: 35%;"/>

                                            <span data-toggle="tooltip" data-placement="bottom"
                                                  title="No se ha definido la configuraci&oacute;n global para '${connector.type}'. Se debe definir una configuraci&oacute;n local."
                                                  style="position: absolute;right: -20px;top: 35%;">
                                                    <i class="fa fa-question-circle"></i>
                                                </span>
                                        </c:when>
                                        <c:otherwise>
                                            <form:checkbox class="form-control" name="enableSTSLocal"
                                                           path="enableSTSLocal"
                                                           id="enable_sts_local"
                                                           onclick="toggleEnableSTSLocalUrl();"/>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="col-sm-8"></div>
                            </div>
                            <div class="form-group col-sm-12 d-table">
                                <label class="control-label col-sm-2 d-table-cell" for="enableLocalPolicyName">Usar Tipo
                                    de Token Local</label>

                                <div class="col-sm-2 d-table-cell w-input">
                                    <c:choose>
                                        <c:when test="${isGlobalConfigurationEnabled == false}">
                                            <form:checkbox class="form-control" name="enableLocalPolicyName"
                                                           path="enableLocalPolicyName"
                                                           id="enable_local_policy_name"
                                                           onclick="toggleEnableLocalPolicyName();"
                                                           style="visibility: hidden;"/>
                                            <input type="checkbox"
                                                   class="form-control"
                                                   disabled checked
                                                   style="position: absolute;top: 35%;"/>

                                            <span data-toggle="tooltip" data-placement="bottom"
                                                  title="No se ha definido la configuraci&oacute;n global para '${connector.type}'. Se debe definir una configuraci&oacute;n local."
                                                  style="position: absolute;right: -20px;top: 35%;">
                                                    <i class="fa fa-question-circle"></i>
                                                </span>
                                        </c:when>
                                        <c:otherwise>
                                            <form:checkbox class="form-control" name="enableLocalPolicyName"
                                                           path="enableLocalPolicyName"
                                                           id="enable_local_policy_name"
                                                           onclick="toggleEnableLocalPolicyName();"/>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <div class="col-sm-8"></div>
                            </div>
                            <div class="form-group col-sm-12 d-table">
                                <label class="control-label col-sm-2 d-table-cell"
                                       for="enableLocalExpirationNotification">Habilitar notificaci&oacute;n local de
                                    vencimiento</label>
                                <div class="col-sm-2 d-table-cell w-input">
                                    <form:checkbox class="form-control" name="enableLocalExpirationNotification"
                                                   path="enableLocalExpirationNotification"
                                                   id="enableLocalExpirationNotification"
                                                   onclick="toggleEnableLocalExpirationNotification();"/>
                                </div>
                                <div class="col-sm-8"></div>
                            </div>

                            <div class="form-group col-sm-12 d-table">
                                <label class="control-label col-sm-2 d-table-cell"
                                       for="enableLocalServiceTimeOut">Habilitar Timeout local en milisegundos</label>
                                <div class="col-sm-2 d-table-cell w-input">
                                    <c:choose>
                                        <c:when test="${isGlobalConfigurationEnabled == false}">
                                            <form:checkbox class="form-control" name="enableLocalServiceTimeOut"
                                                           path="enableLocalServiceTimeOut"
                                                           id="enableLocalServiceTimeOut"
                                                           onclick="toggleEnableLocalTimeout();"
                                                           style="visibility: hidden;"/>
                                            <input type="checkbox"
                                                   class="form-control"
                                                   disabled checked
                                                   style="position: absolute;top: 35%;"/>

                                            <span data-toggle="tooltip" data-placement="bottom"
                                                  title="No se ha definido la configuraci&oacute;n global para '${connector.type}'. Se debe definir una configuraci&oacute;n local."
                                                  style="position: absolute;right: -20px;top: 35%;">
                                                    <i class="fa fa-question-circle"></i>
                                                </span>
                                        </c:when>
                                        <c:otherwise>
                                            <form:checkbox class="form-control" name="enableLocalServiceTimeOut"
                                                           path="enableLocalServiceTimeOut"
                                                           id="enableLocalServiceTimeOut"
                                                           onclick="toggleEnableLocalTimeout();"/>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="col-sm-8"></div>
                            </div>

                            <div id="enter_token_credentials_div"
                                 <c:if test="${connector.enableUserCredentials == false}">style="display: none;"</c:if>>
                                <div class="form-group col-sm-12 d-table">
                                    <form:label class="control-label col-sm-2 d-table-cell" for="username_token"
                                                path="userCredentials.userNameTokenName">Usuario Username Token *</form:label>
                                    <div class="col-sm-10 d-table-cell">
                                        <form:input class="form-control" type="text" id="userNameTokenName"
                                                    name="userCredentials.userNameTokenName"
                                                    path="userCredentials.userNameTokenName"
                                                    value="${connector.userCredentials.userNameTokenName}"
                                                    placeholder="Usuario Username Token"/>
                                    </div>
                                </div>
                                <div class="form-group col-sm-12 d-table">
                                    <form:label class="control-label col-sm-2 d-table-cell" for="password_token"
                                                path="userCredentials.userNameTokenPassword">Usuario Password
                                        Token *</form:label>
                                    <div class="col-sm-10 d-table-cell">
                                        <form:input class="form-control" type="password" id="userNameTokenPassword"
                                                    name="userCredentials.userNameTokenPassword"
                                                    path="userCredentials.userNameTokenPassword"
                                                    value="${connector.userCredentials.userNameTokenPassword}"
                                                    placeholder="Usuario Password Token" autocomplete="off"/>
                                    </div>
                                </div>
                            </div>

                            <div id="enable_local_configuration_div" class="ml-10"
                                 <c:if test="${connector.enableLocalConfiguration == false}">style="display: none;"</c:if>>
                                <div class="form-group col-sm-12 d-table">
                                    <form:label class="control-label col-sm-2 d-table-cell" for="alias_issuer_keystore"
                                                path="localConfiguration.aliasKeystore">Alias del Keystore
                                        Organismo *</form:label>
                                    <div class="col-sm-10 d-table-cell">
                                        <form:input class="form-control" type="text" id="aliasKeystore"
                                                    name="localConfiguration.aliasKeystore"
                                                    path="localConfiguration.aliasKeystore"
                                                    value="${connector.localConfiguration.aliasKeystore}"
                                                    placeholder="Alias del Keystore Organismo" maxlength="100"/>
                                    </div>
                                </div>
                                <div class="form-group col-sm-12 d-table">
                                    <form:label class="control-label col-sm-2 d-table-cell"
                                                for="password_issuer_keystore"
                                                path="localConfiguration.passwordKeystoreOrg">Password Keystore
                                        Organismo *</form:label>
                                    <div class="col-sm-10 d-table-cell">
                                        <form:input class="form-control" type="password" id="passwordKeystoreOrg"
                                                    name="localConfiguration.passwordKeystoreOrg"
                                                    path="localConfiguration.passwordKeystoreOrg"
                                                    value="${connector.localConfiguration.passwordKeystoreOrg}"
                                                    placeholder="Password Keystore Organismo" autocomplete="off"
                                                    maxlength="50"/>
                                    </div>
                                </div>
                                <div class="form-group col-sm-12 d-table">
                                    <form:label class="control-label col-sm-2 d-table-cell" for="alias_issuer_keystore_ssl"
                                                path="localConfiguration.aliasKeystoreSSL">Alias del Keystore
                                        SSL *</form:label>
                                    <div class="col-sm-10 d-table-cell">
                                        <form:input class="form-control" type="text" id="aliasKeystoreSSL"
                                                    name="localConfiguration.aliasKeystoreSSL"
                                                    path="localConfiguration.aliasKeystoreSSL"
                                                    value="${connector.localConfiguration.aliasKeystoreSSL}"
                                                    placeholder="Alias del Keystore SSL" maxlength="100"/>
                                    </div>
                                </div>
                                <div class="form-group col-sm-12 d-table">
                                    <form:label class="control-label col-sm-2 d-table-cell" for="password_ssl_keystore"
                                                path="localConfiguration.passwordKeystoreSsl">Password Keystore
                                        SSL *</form:label>
                                    <div class="col-sm-10 d-table-cell">
                                        <form:input class="form-control" type="password" id="passwordKeystoreSsl"
                                                    name="localConfiguration.passwordKeystoreSsl"
                                                    path="localConfiguration.passwordKeystoreSsl"
                                                    value="${connector.localConfiguration.passwordKeystoreSsl}"
                                                    placeholder="Password Keystore SSL" autocomplete="off"
                                                    maxlength="50"/>
                                    </div>
                                </div>
                                <div class="form-group col-sm-12 d-table">
                                    <form:label class="control-label col-sm-2 d-table-cell" for="password_truststore"
                                                path="localConfiguration.passwordKeystore">Password
                                        Truststore *</form:label>
                                    <div class="col-sm-10 d-table-cell">
                                        <form:input class="form-control" type="password" id="passwordKeystore"
                                                    name="localConfiguration.passwordKeystore"
                                                    path="localConfiguration.passwordKeystore"
                                                    value="${connector.localConfiguration.passwordKeystore}"
                                                    placeholder="Password Truststore" autocomplete="off"
                                                    maxlength="50"/>
                                    </div>
                                </div>
                                <div class="form-group col-sm-12 d-table">
                                    <form:label class="control-label col-sm-2 d-table-cell" for="issuer_keystore"
                                                path="localConfiguration.dirKeystoreOrg">Keystore Organismo
                                        <c:if test="${esAlta == true}"> *</c:if>
                                    </form:label>
                                    <div class="col-sm-10 d-table-cell">
                                        <input type="file" id="keystoreOrgFile" name="keystoreOrgFile"
                                               placeholder="Keystore Organismo File"/>
                                        <input hidden="hidden"
                                               value="${connector.localConfiguration.dirKeystoreOrg}"/>
                                        <c:if test="${esAlta == false && not empty connector.localConfiguration.dirKeystoreOrg}">
                                            <a href="${keystoreOrg}" class="pl-0 btn btn-link btn-xs active"
                                               role="button">
                                                <img width="12px"
                                                     height="10px"
                                                     src="${pageContext.request.contextPath}/resources/images/icon-down-arrow.svg"
                                                     alt="Descargar Keystore Organismo"
                                                     title="Descargar Keystore Organismo">
                                                Descargar
                                            </a>
                                            <button type="button" class="btn btn-link btn-xs" data-toggle="modal"
                                                    data-target="#keystore_modal_keystoreOrgFile">
                                                <img width="12px"
                                                     height="10px"
                                                     src="${pageContext.request.contextPath}/resources/images/icon-search.svg"
                                                     alt="Ver detalles Keystore Organismo"
                                                     title="Ver detalles Keystore Organismo">
                                                Ver detalles
                                            </button>
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
                                                <strong>Ruta:</strong> ${connector.localConfiguration.dirKeystoreOrg}
                                            </p>
                                        </c:if>
                                    </div>
                                </div>
                                <div class="form-group col-sm-12 d-table">
                                    <form:label class="control-label col-sm-2 d-table-cell" for="ssl_keystore"
                                                path="localConfiguration.dirKeystoreSsl">Keystore SSL
                                        <c:if test="${esAlta == true}"> *</c:if>
                                    </form:label>
                                    <div class="col-sm-10 d-table-cell">
                                        <input type="file" id="keystoreSSLFile" name="keystoreSSLFile"
                                               placeholder="Keystore SSL File"/>
                                        <input hidden="hidden"
                                               value="${connector.localConfiguration.dirKeystoreSsl}"/>
                                        <c:if test="${esAlta == false && not empty connector.localConfiguration.dirKeystoreSsl}">
                                            <a href="${keystoreSsl}" class="pl-0 btn btn-link btn-xs active"
                                               role="button">
                                                <img width="12px"
                                                     height="10px"
                                                     src="${pageContext.request.contextPath}/resources/images/icon-down-arrow.svg"
                                                     alt="Descargar Keystore SSL"
                                                     title="Descargar Keystore SSL">
                                                Descargar
                                            </a>
                                            <button type="button" class="btn btn-link btn-xs" data-toggle="modal"
                                                    data-target="#keystore_modal_keystoreSSLFile">
                                                <img width="12px"
                                                     height="10px"
                                                     src="${pageContext.request.contextPath}/resources/images/icon-search.svg"
                                                     alt="Ver detalles Keystore SSL"
                                                     title="Ver detalles Keystore SSL">
                                                Ver detalles
                                            </button>
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
                                            <c:if test="${not empty connector.expDateKeystoreSSL}">
                                                <p class="help-block"><strong>V&aacute;lido
                                                    hasta:</strong> ${connector.expDateKeystoreSSL}</p>
                                            </c:if>
                                            <p class="help-block linebreak">
                                                <strong>Ruta:</strong> ${connector.localConfiguration.dirKeystoreSsl}
                                            </p>
                                        </c:if>
                                    </div>
                                </div>
                                <div class="form-group col-sm-12 d-table">
                                    <form:label class="control-label col-sm-2 d-table-cell" for="ssl_truststore"
                                                path="localConfiguration.dirKeystore">Truststore SSL
                                        <c:if test="${esAlta == true}"> *</c:if>
                                    </form:label>
                                    <div class="col-sm-10 d-table-cell">
                                        <input type="file" id="keystoreTruststoreFile" name="keystoreTruststoreFile"
                                               placeholder="Keystore Trust File"/>
                                        <input hidden="hidden"
                                               value="${connector.localConfiguration.dirKeystore}"/>
                                        <c:if test="${esAlta == false && not empty connector.localConfiguration.dirKeystore}">
                                            <a href="${truststore}" class="pl-0 btn btn-link btn-xs active"
                                               role="button">
                                                <img width="12px"
                                                     height="10px"
                                                     src="${pageContext.request.contextPath}/resources/images/icon-down-arrow.svg"
                                                     alt="Descargar Truststore"
                                                     title="Descargar Truststore">
                                                Descargar
                                            </a>
                                            <button type="button" class="btn btn-link btn-xs" data-toggle="modal"
                                                    data-target="#keystore_modal_keystoreFile">
                                                <img width="12px"
                                                     height="10px"
                                                     src="${pageContext.request.contextPath}/resources/images/icon-search.svg"
                                                     alt="Ver detalles Truststore"
                                                     title="Ver detalles Truststore">
                                                Ver detalles
                                            </button>

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
                                                    hasta:</strong> ${connector.expDateKeystoreTruststore}</p>
                                            </c:if>
                                            <p class="help-block linebreak">
                                                <strong>Ruta:</strong> ${connector.localConfiguration.dirKeystore}</p>
                                        </c:if>
                                    </div>
                                </div>
                                <c:if test="${esAlta == false}">
                                    <div class="form-group col-sm-12 d-table">
                                        <form:label class="control-label col-sm-2 d-table-cell" for="ssl_truststore"
                                                    path="localConfiguration.dirKeystore">WSDL
                                        </form:label>
                                        <div class="col-sm-10 d-table-cell">
                                            <a href="${wsdl}" class="pl-0 btn btn-link btn-xs active" role="button">
                                                <img width="12px"
                                                     height="10px"
                                                     src="${pageContext.request.contextPath}/resources/images/icon-down-arrow.svg"
                                                     alt="Descargar WSDL" title="Descargar WSDL">
                                                Descargar
                                            </a>
                                        </div>
                                    </div>
                                </c:if>
                            </div>

                            <div id="enable_sts_local_url_div"
                                 <c:if test="${connector.enableSTSLocal == false}">style="display: none;"</c:if>>
                                <div class="form-group col-sm-12 d-table mt-10">
                                    <form:label class="control-label col-sm-2 d-table-cell" for="sts_local_url"
                                                path="stsLocalUrl">URL STS Local *</form:label>
                                    <div class="col-sm-10 d-table-cell">
                                        <form:input class="form-control" type="text" id="stsLocalUrl"
                                                    name="stsLocalUrl"
                                                    path="stsLocalUrl"
                                                    value="${connector.stsLocalUrl}"
                                                    onkeypress="removeWhitespaces(this)"
                                                    placeholder="URL STS Local" maxlength="512"/>
                                    </div>
                                </div>
                            </div>

                            <div id="enable_local_policy_name_div"
                                 <c:if test="${connector.enableLocalPolicyName == false}">style="display: none;"</c:if>>

                                <div class="form-group col-sm-12 d-table">
                                    <form:label class="control-label col-sm-2 d-table-cell" for="policy_name"
                                                path="policyName">Tipo de token local *</form:label>
                                    <div class="col-sm-10 d-table-cell">
                                        <form:input class="form-control" type="text" id="policyName"
                                                    name="policyName"
                                                    path="policyName"
                                                    value="${connector.policyName}"
                                                    onkeypress="removeWhitespaces(this)"
                                                    placeholder="Tipo de token local" maxlength="100"
                                                    style="width: 97%; display: inline"/>

                                        <span data-toggle="tooltip" data-placement="bottom"
                                              title="El campo Tipo de token debe contener el valor &#8217;urn:tokensimple&#8217; para testing y &#8217;urn:std15&#8217; para producci&oacute;n, si se utiliza SAML1.1. En caso que se utilice SAML2.0, para ambos ambientes corresponde &#8217;urn:std15&#8217;."><i
                                                class="fa fa-question-circle"></i></span>

                                    </div>

                                </div>
                            </div>

                            <div id="enable_local_timeout_div"
                                 <c:if test="${connector.enableLocalServiceTimeOut != true}">style="display: none;"</c:if>>

                                <div class="form-group col-sm-12 d-table">
                                    <form:label class="control-label col-sm-2 d-table-cell"
                                                for="localServiceTimeOut"
                                                path="localServiceTimeOut">Timeout en milisegundos *</form:label>
                                    <div class="col-sm-10 d-table-cell">
                                        <form:input class="form-control" type="text" id="localServiceTimeOut"
                                                    name="localServiceTimeOut"
                                                    path="localServiceTimeOut"
                                                    value="${connector.localServiceTimeOut}"
                                                    placeholder="Tiempo de espera en milisegundos." pattern="[0-9]{1,6}"
                                                    style="width: 97%; display: inline"/>

                                        <span data-toggle="tooltip" data-placement="bottom"
                                              title="El campo Timeout en milisegundos determina el tiempo de espera de respuesta del servicio.">
                                            <i class="fa fa-question-circle"></i>
                                        </span>

                                        <p class="help-block linebreak">
                                            Introduzca entre 1 y 6 d&iacute;gitos
                                        </p>

                                    </div>

                                </div>
                            </div>

                            <div id="enable_local_expiration_notice_days_div"
                                 <c:if test="${connector.enableLocalExpirationNotification != true}">style="display: none;"</c:if>>

                                <div class="form-group col-sm-12 d-table">
                                    <form:label class="control-label col-sm-2 d-table-cell"
                                                for="localExpirationNoticeDays"
                                                path="localExpirationNoticeDays">D&iacute;as previos al aviso *</form:label>
                                    <div class="col-sm-10 d-table-cell">
                                        <form:input class="form-control" type="text" id="localExpirationNoticeDays"
                                                    name="localExpirationNoticeDays"
                                                    path="localExpirationNoticeDays"
                                                    value="${connector.localExpirationNoticeDays}"
                                                    placeholder="Dias previos al aviso" pattern="[0-9]{1,3}"
                                                    style="width: 97%; display: inline"/>

                                        <span data-toggle="tooltip" data-placement="bottom"
                                              title="El campo D&iacute;as previos al aviso determina con cuantos d&iacute;as de antelaci&oacute;n se muestra la notificaci&oacute;n del vencimiento de los certificados."><i
                                                class="fa fa-question-circle"></i></span>

                                        <p class="help-block linebreak">
                                            Introduzca entre 1 y 3 d&iacute;gitos
                                        </p>
                                    </div>

                                </div>
                            </div>

                            <div class="form-group col-sm-12 d-table">
                                <form:label class="control-label col-sm-2 d-table-cell" for="tag"
                                            path="tag">TAG</form:label>
                                <div class="col-sm-10 d-table-cell">
                                    <form:input class="form-control" type="text" name="tag" path="tag"
                                                value="${connector.tag}"
                                                placeholder="Tag" autocomplete="off" maxlength="100"/>
                                </div>
                            </div>

                            <div class="form-group">
                                <div class="rich-panel-header">Rol - Operaci&oacute;n</div>

                                <div class="align-right">

                                    <c:if test="${connector.multipleVersion == true}">

                                    <!-- Nav tabs -->
                                    <ul class="nav nav-tabs" role="tablist" style="margin-left:17px">
                                        <li role="presentation" class="active"><a href="#one" aria-controls="one"
                                                                                  role="tab" data-toggle="tab">Soap
                                            1.1</a></li>
                                        <li role="presentation"><a href="#two" aria-controls="two" role="tab"
                                                                   data-toggle="tab">Soap 1.2</a></li>
                                    </ul>

                                    <!-- Tab panes -->
                                    <div class="tab-content">
                                        <div role="tabpanel" class="tab-pane active" id="one">

                                            </c:if>
                                            <table
                                                    <c:if test="${connector.multipleVersion == true}">class="w-100"</c:if>>
                                                <tbody>
                                                <tr>
                                                    <td>
                                                        <table class="table-role-op" border="0" cellpadding="0"
                                                               cellspacing="0" width="100%">
                                                            <colgroup span="3"></colgroup>
                                                            <thead>
                                                            <tr>
                                                                <th>Rol</th>
                                                                <th>wsa:Action</th>
                                                                <th>Operaci&oacute;n
                                                                </th>
                                                            </tr>

                                                            <c:forEach var="operation"
                                                                       items="${connector.roleOperations}"
                                                                       varStatus="i">

                                                                <c:if test="${operation.soapVersion == '1.1'}">

                                                                    <input type="text"
                                                                           name="roleOperations[${i.index}].soapVersion"
                                                                           value="${operation.soapVersion}" hidden/>

                                                                    <tr>
                                                                        <td>
                                                                            <div class="col-sm-12">
                                                                                <input class="form-control" type="text"
                                                                                       name="roleOperations[${i.index}].role"
                                                                                       value="${operation.role}"/>
                                                                            </div>
                                                                        </td>
                                                                        <td>
                                                                            <div class="col-sm-12">
                                                                                <input class="form-control" type="text"
                                                                                       name="roleOperations[${i.index}].wsaAction"
                                                                                       value="${operation.wsaAction}"/>
                                                                            </div>
                                                                        </td>
                                                                        <td>
                                                                            <div class="col-sm-12">
                                                                                <input class="form-control" type="text"
                                                                                       name="roleOperations[${i.index}].operationFromWSDL"
                                                                                       value="${operation.operationFromWSDL}"
                                                                                       readonly/>
                                                                            </div>
                                                                        </td>
                                                                        <input type="text"
                                                                               name="roleOperations[${i.index}].operationInputName"
                                                                               value="${operation.operationInputName}"
                                                                               hidden/>
                                                                    </tr>

                                                                </c:if>

                                                            </c:forEach>
                                                            </thead>
                                                            <tbody></tbody>
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
                                                        <table class="table-role-op" border="0" cellpadding="0"
                                                               cellspacing="0" width="100%">
                                                            <colgroup span="3"></colgroup>
                                                            <thead>
                                                            <tr>
                                                                <th>Rol</th>
                                                                <th>wsa:Action</th>
                                                                <th>Operaci&oacute;n
                                                                </th>
                                                            </tr>

                                                            <c:forEach var="operation"
                                                                       items="${connector.roleOperations}"
                                                                       varStatus="i">

                                                                <c:if test="${operation.soapVersion == '1.2'}">

                                                                    <input type="text"
                                                                           name="roleOperations[${i.index}].soapVersion"
                                                                           value="${operation.soapVersion}" hidden/>

                                                                    <tr>
                                                                        <td>
                                                                            <div class="col-sm-12">
                                                                                <input class="form-control" type="text"
                                                                                       name="roleOperations[${i.index}].role"
                                                                                       value="${operation.role}"/>
                                                                            </div>
                                                                        </td>
                                                                        <td>
                                                                            <div class="col-sm-12">
                                                                                <input class="form-control" type="text"
                                                                                       name="roleOperations[${i.index}].wsaAction"
                                                                                       value="${operation.wsaAction}"/>
                                                                            </div>
                                                                        </td>
                                                                        <td>
                                                                            <div class="col-sm-12">
                                                                                <input class="form-control" type="text"
                                                                                       name="roleOperations[${i.index}].operationFromWSDL"
                                                                                       value="${operation.operationFromWSDL}"
                                                                                       readonly/>
                                                                            </div>
                                                                        </td>
                                                                        <input type="text"
                                                                               name="roleOperations[${i.index}].operationInputName"
                                                                               value="${operation.operationInputName}"
                                                                               hidden/>
                                                                    </tr>

                                                                </c:if>

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
                                    </c:if>

                                </div>
                            </div>

                            <c:forEach var="keystoreModalData" items="${keystoreModalDataColl}">
                                <div class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
                                     id="keystore_modal_${keystoreModalData.nombre}"
                                     aria-labelledby="keystore_modal_${keystoreModalData.nombre}">
                                    <div class="modal-dialog modal-sm" role="document">
                                        <div class="modal-content">
                                            <div class="modal-header">
                                                <button type="button" class="close" data-dismiss="modal">&times;
                                                </button>
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
                        <div class="form-group align-right" style="padding-top: 10px;">
                            <div class="col-sm-3" style="padding-left: 8px;">
                                <form:form id="cancelContainer" action="${cancelUrl}"
                                           method="post"
                                           enctype="multipart/form-data">
                                    <input type="text" name="prefixNameConnector" value="${prefixNameConnector}"
                                           hidden/>
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
                                <div class="col-sm-2">
                                    <input type="submit" value="Borrar" class="btn-input" data-toggle="modal"
                                           data-target="#delete_confirmation_${connector.id}">
                                </div>
<%--                                Ocultar botón Pasar a Producción por el momento y luego definir bien la logica a seguir--%>
<%--                                <c:if test="${connector.type == 'Testing'}">--%>
<%--                                    <div class="col-sm-4" style="padding-right: 0; text-align: right;">--%>
<%--                                        <form:form id="toProdContainer" action="${testToProdUrl}"--%>
<%--                                                   method="get"--%>
<%--                                                   enctype="multipart/form-data">--%>
<%--                                            <input type="submit" value="Pasar a Producci&oacute;n" class="btn-input">--%>
<%--                                        </form:form>--%>
<%--                                    </div>--%>
<%--                                </c:if>--%>
                                <div class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
                                     id="delete_confirmation_${connector.id}"
                                     aria-labelledby="delete_confirmation_${connector.id}">
                                    <div class="modal-dialog modal-sm" role="document">
                                        <div class="modal-content">
                                            <div class="modal-header">
                                                <h4 class="modal-title" id="gridSystemModalLabel">&iquest;Seguro que
                                                    desea borrar
                                                    el servicio?</h4>
                                            </div>
                                            <div class="modal-footer">
                                                <button type="button" class="btn btn-default" data-dismiss="modal">No
                                                </button>
                                                <button type="button" class="btn btn-danger"
                                                        onclick="location.href='${deleteUrl}'">Si
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:if>

                            <div class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
                                 id="expiration_alert">
                                <div class="modal-dialog modal-md" role="document">
                                    <div class="modal-content">
                                        <div class="modal-header">
                                            <h4 class="modal-title" id="">Revise la fecha de expiraci&oacuten de los
                                                certificados</h4>
                                        </div>
                                        <div class="modal-body">
                                            <p id="certOrg"></p>
                                            <p id="certSSL"></p>
                                            <p id="certTrust"></p>
                                        </div>
                                        <div class="modal-footer">
                                            <button type="button" class="btn btn-default" data-dismiss="modal">Aceptar
                                            </button>
                                        </div>
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

<script>
    function initializeRequired() {
        const esAlta = '<c:out value="${esAlta}"/>';
        const enableLocalConfiguration = '<c:out value="${connector.enableLocalConfiguration}"/>';
        const isGlobalConfigurationEnabled = '<c:out value="${isGlobalConfigurationEnabled}"/>';
        if (esAlta === "false") {
            const expKeystoreOrgStatus = '<c:out value="${connector.expKeystoreOrgStatus}"/>';
            const expKeystoreSSLStatus = '<c:out value="${connector.expKeystoreSSLStatus}"/>';
            const expKeystoreTruststoreStatus = '<c:out value="${connector.expKeystoreTruststoreStatus}"/>';

            if (enableLocalConfiguration === "true" && (expKeystoreOrgStatus === "EXPIRED" || expKeystoreOrgStatus === "EXPIRE_SOON" ||
                expKeystoreSSLStatus === "EXPIRED" || expKeystoreSSLStatus === "EXPIRE_SOON" ||
                expKeystoreTruststoreStatus === "EXPIRED" || expKeystoreTruststoreStatus === "EXPIRE_SOON")) {
                $('#certOrg').append("Keystore Organismo: ").append(expKeystoreOrgStatus === "EXPIRED" ? "<b class=\"text-danger\">Expirado.</b>" : (expKeystoreOrgStatus === "EXPIRE_SOON" ? "<b class=\"text-warning\">Expira Pronto.</b>" : "<b class=\"text-success\">OK.</b>"));
                $('#certSSL').append("Keystore SSL: ").append(expKeystoreSSLStatus === "EXPIRED" ? "<b class=\"text-danger\">Expirado.</b>" : (expKeystoreSSLStatus === "EXPIRE_SOON" ? "<b class=\"text-warning\">Expira Pronto.</b>" : "<b class=\"text-success\">OK.</b>"));
                $('#certTrust').append("Truststore SSL: ").append(expKeystoreTruststoreStatus === "EXPIRED" ? "<b class=\"text-danger\">Expirado.</b>" : (expKeystoreTruststoreStatus === "EXPIRE_SOON" ? "<b class=\"text-warning\">Expira Pronto.</b>" : "<b class=\"text-success\">OK.</b>"));
                $('#expiration_alert').modal('show')
            }
        } else {
            //If there is no global configuration defined, it is mandatory to define a local configuration.
            if (isGlobalConfigurationEnabled === "false" && !$('#enable_local_configuration').prop('checked')) {
                $("#enable_local_configuration").trigger('click');
                $("#enable_local_policy_name").trigger('click');
                $("#enable_sts_local").trigger('click');
                $("#enableLocalServiceTimeOut").trigger('click');
            }
        }

        const enableUserCredentials = '<c:out value="${connector.enableUserCredentials}"/>';
        const enableSTSLocal = '<c:out value="${connector.enableSTSLocal}"/>';
        const enableLocalPolicyName = '<c:out value="${connector.enableLocalPolicyName}"/>';
        const enableLocalExpirationNotification = '<c:out value="${connector.enableLocalExpirationNotification}"/>';
        const enableLocalServiceTimeOut = '<c:out value="${connector.enableLocalServiceTimeOut}"/>';
        if (enableUserCredentials === "true") {
            toggleRequiredUserCredentials();
        }
        if (enableLocalConfiguration === "true") {
            toggleRequiredConfigurations();
        }
        if (enableSTSLocal === "true") {
            toggleRequiredSTSLocal();
        }
        if (enableLocalPolicyName === "true") {
            toggleRequiredLocalPolicyName();
        }
        if (enableLocalExpirationNotification === "true") {
            toggleRequiredLocalExpirationNoticeDays();
        }
        if (enableLocalServiceTimeOut === "true") {
            toggleRequiredLocalServiceTimeOut();
        }
        if (esAlta === "false") {
            $("#type").prop('disabled', true);
        }
    }

    function toggleRequiredConfigurations() {
        toggleRequiredNotRequired('aliasKeystore');
        toggleRequiredNotRequired('aliasKeystoreSSL');
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
