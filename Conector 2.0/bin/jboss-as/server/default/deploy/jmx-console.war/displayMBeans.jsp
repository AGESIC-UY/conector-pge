<%@page contentType="text/html"
   import="java.net.*,java.util.*,org.jboss.jmx.adaptor.model.*,java.io.*"
%>
<html>
<head>
    <%
         String bindAddress = "";
         String serverName = "";
         try
         {
            bindAddress = System.getProperty("jboss.bind.address", "");
            serverName = System.getProperty("jboss.server.name", "");
         }
         catch (SecurityException se) {}

         String hostname = "";
         try
         {
            hostname = InetAddress.getLocalHost().getHostName();
         }
         catch(IOException e)  {}

         String hostInfo = hostname;
         if (!bindAddress.equals(""))
         { 
            hostInfo = hostInfo + " (" + bindAddress + ")";
         }
   %>
   <title>JBoss JMX Management Console - <%= hostInfo %></title>
   <link rel="stylesheet" href="style_master.css" type="text/css">
   <meta http-equiv="cache-control" content="no-cache">
</head>
<body>
<table width="100%">
   <table>
      <tr>
         <td><img src="images/logo.gif" align="left" border="0" alt="JBoss"></td>
         <td valign="middle">
           <h1>JMX Agent View</h1>
           <h3><%= hostInfo %> - <%= serverName %></h3>
         </td>
      </tr>
   </table>
<hr>
<form action="HtmlAdaptor?action=displayMBeans" method="post" name="applyFilter" id="applyFilter">
ObjectName Filter (e.g. "jboss:*", "*:service=invoker,*")  :<input type="text" name="filter" size="40" value="<%= request.getAttribute("filter")%>" /><input type="submit" name="apply" value="ApplyFilter">
<%
 	if (request.getAttribute("filterError") != null) {
		out.println("<br/><span class='error'>"+request.getAttribute("filterError")+"</span>");
 	}
%>
</form>
<hr>
<%
   Iterator mbeans = (Iterator) request.getAttribute("mbeans");
   while( mbeans.hasNext() )
   {
      DomainData domainData = (DomainData) mbeans.next();
%>
   <h2 class='DomainName'><%= domainData.getDomainName() %></h2>
   <ul class='MBeanList'>
<%
      MBeanData[] data = domainData.getData();
      for(int d = 0; d < data.length; d ++)
      {
         String name = data[d].getObjectName().toString();
         String properties = data[d].getNameProperties();
%>
      <li><a href="HtmlAdaptor?action=inspectMBean&name=<%= URLEncoder.encode(name) %>"><%= URLDecoder.decode(properties) %></a></li>
<%
      }
%>
   </ul>
<%
   }
%>
</td></tr>
</table>
</body>
</html>
