<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<nav class="navbar">
    <div class="container-fluid">
        <!-- Brand and toggle get grouped for better mobile display -->
        <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#main-menu"
                    aria-expanded="false">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
        </div>

        <!-- Collect the nav links, forms, and other content for toggling -->
        <div class="collapse navbar-collapse" id="main-menu">
            <ul class="nav navbar-nav">
                <li class="active"><a href="${pageContext.request.contextPath}">Inicio</a></li>
            </ul>
            <ul class="nav navbar-nav navbar-right">
                <li>
                    <a href="${pageContext.request.contextPath}/globalConfiguration?type=Testing"
                       title="Configuraci&oacute;n Global">
                        <img src="${pageContext.request.contextPath}/resources/images/icon-settings.svg">
                    </a>
                </li>
                <li>
                    <!-- Button trigger modal -->
                    <button type="button" class="btn-help" data-toggle="modal" data-target="#myModal">
                        <img src="${pageContext.request.contextPath}/resources/images/icon-help.svg">
                        <span class="rich-tool-tip" style="z-index: 99; visibility: visible; display: none;">
                      <span>Ayuda</span>
                      <span style="display:none"></span>
                    </span>
                    </button>
                </li>
                <li class="user-avatar">
                    <img src="${pageContext.request.contextPath}/resources/images/icon-user.svg">
                    <span>conector</span>
                </li>
                <li>
                    <form name="logout" id="logout" method="post" action="${pageContext.request.contextPath}/logout"
                          enctype="application/x-www-form-urlencoded">

                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                        <a href="javascript:document.getElementById('logout').submit()">
                            <img src="${pageContext.request.contextPath}/resources/images/icon-logout.svg">
                            <span class="rich-tool-tip" style="z-index: 99; visibility: visible; display: none;">
                                <span>(Salir)</span>
                                <span style="display:none"></span>
                            </span>
                        </a>
                    </form>
                </li>
            </ul>
        </div><!-- /.navbar-collapse -->
    </div><!-- /.container-fluid -->
</nav>

<%@include file="help.jsp" %>


<div id="info" class="alert alert-${css} alert-dismissible" role="alert" <c:if test="${empty msg}">style="display: none;"</c:if>>
    <button type="button" class="close" data-hide="alert" onclick="hideAlert();"
            aria-label="Close">
        <span aria-hidden="true">x</span>
    </button>
    <c:forEach items="${msg}" var="m" >
        <strong>${m}</strong></br>
    </c:forEach>
</div>

<script>
    function hideAlert() {
        $('#info').css('display', 'none');
        $('#info strong').remove();
        $('#info br').remove();
    }
</script>