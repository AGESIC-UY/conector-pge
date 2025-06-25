<%@ taglib prefix="th" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@include file="head.jsp" %>

<spring:url value="/globalConfiguration/keystoreOrg" var="keystoreOrg"/>
<spring:url value="/globalConfiguration/keystoreSsl" var="keystoreSsl"/>
<spring:url value="/globalConfiguration/truststore" var="truststore"/>

<body>
<section class="main">
    <%@include file="navbar.jsp" %>

    <c:choose>
        <c:when test="${aliasKeystore != null}">
            <c:set var="esAlta"
                   value="false"/>
        </c:when>
        <c:otherwise>
            <c:set var="esAlta" value="true"/>
        </c:otherwise>
    </c:choose>

    <div class="content">
        <div class="content">
            <article class="">
                <div id="smartwizard">
                    <div>
                        <div id="step-2" class="">
                            <h2>Configuraci&oacute;n Global del Conector</h2>

                            <c:set var="typeAction"
                                   value="${pageContext.request.contextPath}/globalConfiguration"/>
                            <form class="form-horizontal"
                                  action="${typeAction}"
                                  method="get"
                                  enctype="multipart/form-data">
                                <div class="form-group">
                                    <label class="control-label col-sm-2" for="type">Tipo *</label>
                                    <div class="col-sm-10">
                                        <select class="form-control" type="text" name="type" id="type" path="type"
                                                onchange="this.form.submit()">
                                            <option value="Testing"
                                                    <c:if test="${type == 'Testing'}">selected</c:if>
                                            >Testing
                                            </option>
                                            <option value="Produccion"
                                                    <c:if test="${type == 'Produccion'}">selected</c:if>
                                            >Producci&oacute;n
                                            </option>
                                        </select>
                                    </div>
                                </div>
                            </form>

                            <c:set var="formAction"
                                   value="${pageContext.request.contextPath}/globalConfiguration"/>
                            <form:form class="form-horizontal" id="form_global_configuration"
                                       action="${formAction}"
                                       method="post"
                                       enctype="multipart/form-data" modelAttribute="globalConfiguration">
                            <form:hidden class="form-control" id="id"
                                         name="id"
                                         path="id"
                                         value="${id}"/>
                            <form:hidden class="form-control" id="type"
                                         name="type"
                                         path="type"
                                         value="${type}"/>
                            <div id="enable_local_configuration_div">
                                <div class="form-group">
                                    <form:label class="control-label col-sm-2" for="alias_issuer_keystore"
                                                path="aliasKeystore">Alias del Keystore
                                        Organismo *</form:label>
                                    <div class="col-sm-10">
                                        <form:input class="form-control" type="text" id="aliasKeystore"
                                                    name="aliasKeystore"
                                                    path="aliasKeystore"
                                                    value="${aliasKeystore}"
                                                    placeholder="Alias del Keystore Organismo" maxlength="100"/>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <form:label class="control-label col-sm-2" for="password_issuer_keystore"
                                                path="passwordKeystoreOrg">Password Keystore
                                        Organismo *</form:label>
                                    <div class="col-sm-10">
                                        <form:input class="form-control" type="password" id="passwordKeystoreOrg"
                                                    name="passwordKeystoreOrg"
                                                    path="passwordKeystoreOrg"
                                                    value="${passwordKeystoreOrg}"
                                                    placeholder="Password Keystore Organismo" autocomplete="off"
                                                    maxlength="50"/>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <form:label class="control-label col-sm-2" for="alias_issuer_keystore_ssl"
                                                path="aliasKeystoreSSL">Alias del Keystore
                                        SSL *</form:label>
                                    <div class="col-sm-10">
                                        <form:input class="form-control" type="text" id="aliasKeystoreSSL"
                                                    name="aliasKeystoreSSL"
                                                    path="aliasKeystoreSSL"
                                                    value="${aliasKeystoreSSL}"
                                                    placeholder="Alias del Keystore SSL" maxlength="100"/>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <form:label class="control-label col-sm-2" for="password_ssl_keystore"
                                                path="passwordKeystoreSsl">Password Keystore
                                        SSL *</form:label>
                                    <div class="col-sm-10">
                                        <form:input class="form-control" type="password" id="passwordKeystoreSsl"
                                                    name="passwordKeystoreSsl"
                                                    path="passwordKeystoreSsl"
                                                    value="${passwordKeystoreSsl}"
                                                    placeholder="Password Keystore SSL" autocomplete="off"
                                                    maxlength="50"/>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <form:label class="control-label col-sm-2" for="password_truststore"
                                                path="passwordKeystore">Password
                                        Truststore *</form:label>
                                    <div class="col-sm-10">
                                        <form:input class="form-control" type="password" id="passwordKeystore"
                                                    name="passwordKeystore"
                                                    path="passwordKeystore"
                                                    value="${passwordKeystore}"
                                                    placeholder="Password Truststore" autocomplete="off"
                                                    maxlength="50"/>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <form:label class="control-label col-sm-2" for="issuer_keystore"
                                                path="dirKeystoreOrg">Keystore Organismo
                                        <c:if test="${esAlta == true}"> *</c:if>
                                    </form:label>
                                    <div class="col-sm-10">
                                        <div class="form-inline">
                                            <div class="col-sm-6 pl-0">
                                                <input type="file" id="keystoreOrgFile" name="keystoreOrgFile"
                                                       placeholder="Keystore Organismo File">
                                                <c:if test="${esAlta == false && not empty expDateKeystoreOrg}">
                                                    <p class="help-block mb-0"><strong>V&aacute;lido
                                                        hasta:</strong> ${expDateKeystoreOrg}</p>
                                                </c:if>
                                            </div>

                                            <c:if test="${esAlta == false && not empty dirKeystoreOrg}">
                                                <div class="col-sm-6">
                                                    <a href="${keystoreOrg}?type=${type}"
                                                       class="btn btn-link btn-xs active" role="button">
                                                        <img class="img-circle"
                                                             width="12px"
                                                             height="10px"
                                                             src="${pageContext.request.contextPath}/resources/images/icon-down-arrow.svg"
                                                             alt="Descargar Keystore Organismo"
                                                             title="Descargar Keystore Organismo">
                                                        Descargar
                                                    </a>

                                                    <c:if test="${expKeystoreOrgStatus == 'EXPIRE_SOON'}">
                                                        <img width="20px"
                                                             height="20px"
                                                             src="${pageContext.request.contextPath}/resources/images/icon-exp-warn.svg"
                                                             alt="Certificado pr&oacute;ximo a vencer"
                                                             title="Certificado pr&oacute;ximo a vencer"/>
                                                    </c:if>

                                                    <c:if test="${expKeystoreOrgStatus == 'EXPIRED'}">
                                                        <img width="20px"
                                                             height="20px"
                                                             src="${pageContext.request.contextPath}/resources/images/icon-exp-error.svg"
                                                             alt="Certificado vencido" title="Certificado vencido"/>
                                                    </c:if>

                                                </div>

                                                <div class="col-sm-12 pl-0">
                                                    <p class="help-block linebreak">
                                                        <strong>Ruta:</strong> ${dirKeystoreOrg}</p>
                                                </div>
                                            </c:if>

                                        </div>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <form:label class="control-label col-sm-2" for="ssl_keystore"
                                                path="dirKeystoreSsl">Keystore SSL
                                        <c:if test="${esAlta == true}"> *</c:if>
                                    </form:label>
                                    <div class="col-sm-10">
                                        <div class="form-inline">
                                            <div class="col-sm-6 pl-0">
                                                <input type="file" id="keystoreSSLFile" name="keystoreSSLFile"
                                                       placeholder="Keystore SSL File"/>
                                                <c:if test="${esAlta == false && not empty expDateKeystoreSsl}">
                                                    <p class="help-block mb-0"><strong>V&aacute;lido
                                                        hasta:</strong> ${expDateKeystoreSsl}</p>
                                                </c:if>
                                            </div>

                                            <c:if test="${esAlta == false && not empty dirKeystoreSsl}">
                                                <div class="col-sm-6">
                                                    <a href="${keystoreSsl}?type=${type}"
                                                       class="btn btn-link btn-xs active" role="button">
                                                        <img class="img-circle"
                                                             width="12px"
                                                             height="10px"
                                                             src="${pageContext.request.contextPath}/resources/images/icon-down-arrow.svg"
                                                             alt="Descargar Keystore SSL"
                                                             title="Descargar Keystore SSL">
                                                        Descargar
                                                    </a>

                                                    <c:if test="${expKeystoreSSLStatus == 'EXPIRE_SOON'}">
                                                        <img width="20px"
                                                             height="20px"
                                                             src="${pageContext.request.contextPath}/resources/images/icon-exp-warn.svg"
                                                             alt="Certificado pr&oacute;ximo a vencer"
                                                             title="Certificado pr&oacute;ximo a vencer"/>
                                                    </c:if>

                                                    <c:if test="${expKeystoreSSLStatus == 'EXPIRED'}">
                                                        <img width="20px"
                                                             height="20px"
                                                             src="${pageContext.request.contextPath}/resources/images/icon-exp-error.svg"
                                                             alt="Certificado vencido" title="Certificado vencido"/>
                                                    </c:if>

                                                </div>
                                                <div class="col-sm-12 pl-0">
                                                    <p class="help-block linebreak">
                                                        <strong>Ruta:</strong> ${dirKeystoreSsl}</p>
                                                </div>
                                            </c:if>
                                        </div>

                                    </div>
                                </div>
                                <div class="form-group">
                                    <form:label class="control-label col-sm-2" for="ssl_truststore"
                                                path="dirKeystore">Truststore SSL
                                        <c:if test="${esAlta == true}"> *</c:if>
                                    </form:label>
                                    <div class="col-sm-10">
                                        <div class="form-inline">
                                            <div class="col-sm-6 pl-0">
                                                <input type="file" id="keystoreTruststoreFile"
                                                       name="keystoreTruststoreFile"
                                                       placeholder="Keystore Trust File"/>
                                                <c:if test="${esAlta == false && not empty expDateKeystoreTruststore}">
                                                    <p class="help-block mb-0"><strong>V&aacute;lido
                                                        hasta:</strong> ${expDateKeystoreTruststore}</p>
                                                </c:if>
                                            </div>
                                            <c:if test="${esAlta == false && not empty dirKeystore}">
                                                <div class="col-sm-6">
                                                    <a href="${truststore}?type=${type}"
                                                       class="btn btn-link btn-xs active" role="button">
                                                        <img class="img-circle"
                                                             width="12px"
                                                             height="10px"
                                                             src="${pageContext.request.contextPath}/resources/images/icon-down-arrow.svg"
                                                             alt="Descargar Truststore"
                                                             title="Descargar Truststore">
                                                        Descargar
                                                    </a>

                                                    <c:if test="${expKeystoreTruststoreStatus == 'EXPIRE_SOON'}">
                                                        <img width="20px"
                                                             height="20px"
                                                             src="${pageContext.request.contextPath}/resources/images/icon-exp-warn.svg"
                                                             alt="Certificado pr&oacute;ximo a vencer"
                                                             title="Certificado pr&oacute;ximo a vencer"/>
                                                    </c:if>

                                                    <c:if test="${expKeystoreTruststoreStatus == 'EXPIRED'}">
                                                        <img width="20px"
                                                             height="20px"
                                                             src="${pageContext.request.contextPath}/resources/images/icon-exp-error.svg"
                                                             alt="Certificado vencido" title="Certificado vencido"/>
                                                    </c:if>

                                                </div>
                                                <div class="col-sm-12 pl-0">
                                                    <p class="help-block linebreak">
                                                        <strong>Ruta:</strong> ${dirKeystore}</p>
                                                </div>
                                            </c:if>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="form-group d-table">
                                <form:label class="control-label col-sm-2 d-table-cell"
                                            for="enableExpirationNotification"
                                            path="enableExpirationNotification">Habilitar notificaci&oacute;n de vencimiento</form:label>
                                <div class="col-sm-10 d-table-cell">
                                    <div class="col-sm-2 d-table-cell w-input">
                                        <form:checkbox class="form-control" name="enableExpirationNotification"
                                                       path="enableExpirationNotification"
                                                       id="enableExpirationNotification"
                                                       onclick="toggleNotifyExpiration();"/>
                                    </div>
                                </div>
                            </div>

                            <div id="expiration_notice_days_div" class="form-group"
                                 <c:if test="${enableExpirationNotification != true}">style="display: none;"</c:if>>
                                <form:label class="control-label col-sm-2" for="expirationNoticeDays"
                                            path="expirationNoticeDays">D&iacute;as previos al aviso *</form:label>
                                <div class="col-sm-10">
                                    <form:input class="form-control" type="text" id="expirationNoticeDays"
                                                name="expirationNoticeDays"
                                                path="expirationNoticeDays"
                                                value="${expirationNoticeDays}"
                                                placeholder="Dias previos al aviso" pattern="[0-9]{1,3}"
                                                required="${enableExpirationNotification}"
                                                style="width: 97%; display: inline"/>

                                    <span data-toggle="tooltip" data-placement="bottom"
                                          title="El campo D&iacute;as previos al aviso determina con cuantos d&iacute;as de antelaci&oacute;n se muestra la notificaci&oacute;n del vencimiento de los certificados."><i
                                            class="fa fa-question-circle"></i></span>

                                    <p class="help-block linebreak">
                                        Introduzca entre 1 y 3 d&iacute;gitos
                                    </p>
                                </div>
                            </div>

                            <div class="form-group">
                                <form:label class="control-label col-sm-2" for="serviceTimeOut"
                                            path="serviceTimeOut">Timeout en milisegundos *</form:label>
                                <div class="col-sm-10">
                                    <form:input class="form-control" type="text" id="serviceTimeOut"
                                                name="serviceTimeOut"
                                                path="serviceTimeOut"
                                                value="${serviceTimeOut}"
                                                placeholder="Tiempo de espera en milisegundos." pattern="[0-9]{1,6}"
                                                required="false"
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

                            <div class="form-group">
                                <form:label class="control-label col-sm-2" for="policy_name"
                                            path="policyName">Tipo de token *</form:label>
                                <div class="col-sm-10">
                                    <form:input class="form-control" type="text" id="policyName"
                                                name="policyName"
                                                path="policyName"
                                                value="${policyName}"
                                                placeholder="Tipo de token" maxlength="100"
                                                style="width: 97%; display: inline"/>

                                    <span data-toggle="tooltip" data-placement="bottom"
                                          title="El campo Tipo de token debe contener el valor &#8217;urn:tokensimple&#8217; para testing y &#8217;urn:std15&#8217; para producci&oacute;n, si se utiliza SAML1.1. En caso que se utilice SAML2.0, para ambos ambientes corresponde &#8217;urn:std15&#8217;."><i
                                            class="fa fa-question-circle"></i></span>
                                </div>
                            </div>
                            <div class="form-group">
                                <form:label class="control-label col-sm-2" for="sts_global_url"
                                            path="stsGlobalUrl">URL STS Global *</form:label>
                                <div class="col-sm-10">
                                    <form:input class="form-control" type="text" id="stsGlobalUrl"
                                                name="stsGlobalUrl"
                                                path="stsGlobalUrl"
                                                value="${stsGlobalUrl}"
                                                onkeypress="removeWhitespaces(this)"
                                                placeholder="URL STS Global" maxlength="512"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <form:label class="control-label col-sm-2" for="host"
                                            path="host">WSDL - IP</form:label>
                                <div class="col-sm-10">
                                    <form:input class="form-control" type="text" id="host"
                                                name="host"
                                                path="host"
                                                value="${host}"
                                                onkeypress="removeWhitespaces(this)"
                                                placeholder="IP del WSDL" maxlength="50"
                                                disabled="true"
                                                style="width: 97%; display: inline"/>
                                    <span data-toggle="tooltip" data-placement="bottom"
                                          title="Este valor se carga del fichero de configuraci&oacute;n (connector-pge.properties) y tambi&eacute;n se configura en el servidor web.">
                                        <i class="fa fa-question-circle"></i>
                                    </span>
                                </div>
                            </div>
                            <div class="form-group">
                                <form:label class="control-label col-sm-2" for="port"
                                            path="port">WSDL - Puerto</form:label>
                                <div class="col-sm-10">
                                    <form:input class="form-control" type="text" id="port"
                                                name="port"
                                                path="port"
                                                value="${port}"
                                                onkeypress="removeWhitespaces(this)"
                                                placeholder="Puerto del WSDL" maxlength="10"
                                                disabled="true"
                                                style="width: 97%; display: inline"/>
                                    <span data-toggle="tooltip" data-placement="bottom"
                                          title="Este valor se carga del fichero de configuraci&oacute;n (connector-pge.properties) y tambi&eacute;n se configura en el servidor web.">
                                        <i class="fa fa-question-circle"></i>
                                    </span>
                                </div>
                            </div>
                            <div class="form-group">
                                <form:label class="control-label col-sm-2" for="portSsl"
                                            path="portSsl">WSDL - Puerto SSL</form:label>
                                <div class="col-sm-10">
                                    <form:input class="form-control" type="text" id="portSsl"
                                                name="portSsl"
                                                path="portSsl"
                                                value="${portSsl}"
                                                onkeypress="removeWhitespaces(this)"
                                                placeholder="Puerto SSL del WSDL" maxlength="10"
                                                disabled="true"
                                                style="width: 97%; display: inline"/>
                                    <span data-toggle="tooltip" data-placement="bottom"
                                          title="Este valor se carga del fichero de configuraci&oacute;n (connector-pge.properties) y tambi&eacute;n se configura en el servidor web.">
                                        <i class="fa fa-question-circle"></i>
                                    </span>
                                </div>
                            </div>
                        </div>

                        </form:form>


                        <div class="form-group" style="padding-top: 10px;">
                            <div class="col-sm-3">
                                <c:choose>
                                    <c:when test="${esAlta == true}">
                                        <input type="submit" name="button_alta" value="Alta" class="btn-input"
                                               form="form_global_configuration">
                                    </c:when>
                                    <c:otherwise>
                                        <input type="submit" name="button_actualizar" value="Actualizar"
                                               class="btn-input" form="form_global_configuration">
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="col-sm-3">
                                <spring:url value="/globalConfiguration/cancel"
                                            var="cancelUrl"/>
                                <form action="${cancelUrl}"
                                      method="post">
                                    <input type="submit" value="Cancelar" class="btn-input">
                                </form>
                            </div>
                        </div>

                    </div>
                </div>
            </article>
        </div>
    </div>
</section>

<%@include file="footer.jsp" %>

<script>
    function toggleNotifyExpiration() {
        toggleDiv('expiration_notice_days_div', 'int');
        toggleRequiredNotRequired('expirationNoticeDays');
    }
</script>

</body>
</html>
