<%@ taglib prefix="th" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@include file="head.jsp" %>

<body>
    <section class="main">
        <%@include file="navbar.jsp" %>
        <div style="padding:45px; margin-top: -15px">
            <h3>Ha ocurrido un error inesperado.</h3>
            <h4> Haga click <a href="${pageContext.request.contextPath}">aqu&iacute;</a> y vuelva a la pantalla principal.</h4>
        </div>
    </section>
</body>

<%@include file="footer.jsp" %>