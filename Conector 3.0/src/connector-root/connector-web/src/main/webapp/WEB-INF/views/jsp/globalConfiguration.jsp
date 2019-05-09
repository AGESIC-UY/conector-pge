<%@ taglib prefix="th" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@include file="head.jsp" %>
<spring:url value="/globalConfiguration/keystoreOrg" var="keystoreOrg"/>
<spring:url value="/globalConfiguration/keystoreSsl" var="keystoreSsl"/>
<spring:url value="/globalConfiguration/truststore" var="truststore"/>
<body onload="initializeRequired();">
<section class="main">
    <header></header>
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
                                                    placeholder="Password Keystore Organismo" autocomplete="off" maxlength="50"/>
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
                                                    placeholder="Password Keystore SSL" autocomplete="off" maxlength="50"/>
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
                                                    placeholder="Password Truststore" autocomplete="off" maxlength="50"/>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <form:label class="control-label col-sm-2" for="issuer_keystore"
                                                path="dirKeystoreOrg">Keystore Organismo
                                        <c:if test="${esAlta == true}"> *</c:if>
                                    </form:label>
                                    <div class="col-sm-10">
                                        <input type="file" id="keystoreOrgFile" name="keystoreOrgFile"
                                               placeholder="Keystore Organismo File"/>
                                        <input hidden="hidden"
                                               value="${dirKeystoreOrg}"/>
                                        <c:if test="${esAlta == false}">
                                            <a href="${keystoreOrg}?type=${type}"><img height="25px"
                                                                                       src="${pageContext.request.contextPath}/resources/images/icon-down-arrow.svg"
                                                                                       alt="Descargar Keystore Organismo"
                                                                                       title="Descargar Keystore Organismo"></a>
                                        </c:if>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <form:label class="control-label col-sm-2" for="ssl_keystore"
                                                path="dirKeystoreSsl">Keystore SSL
                                        <c:if test="${esAlta == true}"> *</c:if>
                                    </form:label>
                                    <div class="col-sm-10">
                                        <input type="file" id="keystoreSSLFile" name="keystoreSSLFile"
                                               placeholder="Keystore SSL File"/>
                                        <input hidden="hidden"
                                               value="${dirKeystoreSsl}"/>
                                        <c:if test="${esAlta == false}">
                                            <a href="${keystoreSsl}?type=${type}"><img height="25px"
                                                                                       src="${pageContext.request.contextPath}/resources/images/icon-down-arrow.svg"
                                                                                       alt="Descargar Keystore SSL"
                                                                                       title="Descargar Keystore SSL"></a>
                                        </c:if>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <form:label class="control-label col-sm-2" for="ssl_truststore"
                                                path="dirKeystore">Truststore SSL
                                        <c:if test="${esAlta == true}"> *</c:if>
                                    </form:label>
                                    <div class="col-sm-10">
                                        <input type="file" id="keystoreTruststoreFile" name="keystoreTruststoreFile"
                                               placeholder="Keystore Trust File"/>
                                        <input hidden="hidden"
                                               value="${dirKeystore}"/>
                                        <c:if test="${esAlta == false}">
                                            <a href="${truststore}?type=${type}"><img height="25px"
                                                                                      src="${pageContext.request.contextPath}/resources/images/icon-down-arrow.svg"
                                                                                      alt="Descargar Truststore"
                                                                                      title="Descargar Truststore"></a>
                                        </c:if>
                                    </div>
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
                                                placeholder="Tipo de token" maxlength="100"/>
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
        </div>
        </article>
    </div>

    </div>
</section>

<footer>
    <p>Powered by Pyxis PGE V. 3.0</p>
</footer>

</body>
</html>
