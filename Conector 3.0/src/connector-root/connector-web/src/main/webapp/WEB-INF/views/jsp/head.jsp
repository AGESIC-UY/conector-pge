<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=ISO-8859-1" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Conector PGE</title>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <!-- CSS -->
    <spring:url value="/resources/core/css/bootstrap.min.css" var="bootstrapCss"/>
    <spring:url value="/resources/stylesheet/theme.css" var="themeCss"/>
    <spring:url value="/resources/stylesheet/custom-theme.css" var="customThemeCss"/>
    <link href="${bootstrapCss}" rel="stylesheet"/>
    <link href="${themeCss}" rel="stylesheet"/>
    <link href="${customThemeCss}" rel="stylesheet"/>
    <!-- SmartWizard -->
    <spring:url value="/resources/stylesheet/wizard/smart_wizard.css" var="smart_wizard"/>
    <spring:url value="/resources/stylesheet/wizard/smart_wizard_theme_circles.css" var="smart_wizard_theme_circles"/>
    <spring:url value="/resources/stylesheet/wizard/smart_wizard_theme_arrows.css" var="smart_wizard_theme_arrows"/>
    <spring:url value="/resources/stylesheet/wizard/smart_wizard_theme_dots.css" var="smart_wizard_theme_dots"/>
    <link href="${smart_wizard}" rel="stylesheet"/>
    <link href="${smart_wizard_theme_circles}" rel="stylesheet"/>
    <link href="${smart_wizard_theme_arrows}" rel="stylesheet"/>
    <link href="${smart_wizard_theme_dots}" rel="stylesheet"/>

    <!-- JS -->
    <spring:url value="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js" var="jQueryMinJs"/>
    <spring:url value="/resources/core/js/bootstrap.min.js" var="bootstrapJs"/>
    <spring:url value="/resources/core/js/connector.js" var="connectorJs"/>
    <spring:url value="/resources/core/js/smartWizard.js" var="smartwizardJs"/>
    <script src="${jQueryMinJs}"></script>
    <script src="${bootstrapJs}"></script>
    <script src="${connectorJs}"></script>
    <script src="${smartwizardJs}"></script>
    <!-- SmartWizard -->
    <spring:url value="/resources/core/js/jquery.smartWizard.js" var="jquerySmartWizard"/>
    <script src="${jquerySmartWizard}"></script>


</head>
<body>

</body>
</html>
