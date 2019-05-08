<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@include file="head.jsp" %>

<html>
<body>
<section class="main">
    <header></header>

    <c:if test="${not empty msg}">
        <div class="alert alert-${css} alert-dismissible" role="alert">
            <button type="button" class="close" data-dismiss="alert"
                    aria-label="Close">
                <span aria-hidden="true">x</span>
            </button>
            <strong>${msg}</strong>
        </div>
    </c:if>

    <form name="login" action="<c:url value='/appLogin' />" method="POST">
        <article class="">
            <div class="loginForm">
                <table>
                    <tr>
                        <td>Usuario:</td>
                        <td><input type='text' name='username' value=''></td>
                    </tr>
                    <tr>
                        <td>Contrase&ntilde;a:</td>
                        <td><input type='password' name='password'/></td>
                    </tr>
                    <tr>
                        <td colspan='2'><input class="pull-right btn-input" name="submit" type="submit" value="ENTRAR"/></td>
                    </tr>
                </table>
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            </div>
        </article>
    </form>
</section>

<footer>
    <p>Powered by Pyxis PGE V. 3.0</p>
</footer>

</body>
</html>
