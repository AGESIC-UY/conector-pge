package org.apache.jsp;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import java.net.*;
import java.util.*;
import org.jboss.jmx.adaptor.model.*;
import java.io.*;

public final class displayMBeans_jsp extends org.apache.jasper.runtime.HttpJspBase
    implements org.apache.jasper.runtime.JspSourceDependent {

  private static final JspFactory _jspxFactory = JspFactory.getDefaultFactory();

  private static java.util.List _jspx_dependants;

  private javax.el.ExpressionFactory _el_expressionfactory;
  private org.apache.AnnotationProcessor _jsp_annotationprocessor;

  public Object getDependants() {
    return _jspx_dependants;
  }

  public void _jspInit() {
    _el_expressionfactory = _jspxFactory.getJspApplicationContext(getServletConfig().getServletContext()).getExpressionFactory();
    _jsp_annotationprocessor = (org.apache.AnnotationProcessor) getServletConfig().getServletContext().getAttribute(org.apache.AnnotationProcessor.class.getName());
  }

  public void _jspDestroy() {
  }

  public void _jspService(HttpServletRequest request, HttpServletResponse response)
        throws java.io.IOException, ServletException {

    PageContext pageContext = null;
    HttpSession session = null;
    ServletContext application = null;
    ServletConfig config = null;
    JspWriter out = null;
    Object page = this;
    JspWriter _jspx_out = null;
    PageContext _jspx_page_context = null;


    try {
      response.setContentType("text/html");
      pageContext = _jspxFactory.getPageContext(this, request, response,
      			null, true, 8192, true);
      _jspx_page_context = pageContext;
      application = pageContext.getServletContext();
      config = pageContext.getServletConfig();
      session = pageContext.getSession();
      out = pageContext.getOut();
      _jspx_out = out;

      out.write("\n");
      out.write("<html>\n");
      out.write("<head>\n");
      out.write("    ");

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
   
      out.write("\n");
      out.write("   <title>JBoss JMX Management Console - ");
      out.print( hostInfo );
      out.write("</title>\n");
      out.write("   <link rel=\"stylesheet\" href=\"style_master.css\" type=\"text/css\">\n");
      out.write("   <meta http-equiv=\"cache-control\" content=\"no-cache\">\n");
      out.write("</head>\n");
      out.write("<body>\n");
      out.write("<table width=\"100%\">\n");
      out.write("   <table>\n");
      out.write("      <tr>\n");
      out.write("         <td><img src=\"images/logo.gif\" align=\"left\" border=\"0\" alt=\"JBoss\"></td>\n");
      out.write("         <td valign=\"middle\">\n");
      out.write("           <h1>JMX Agent View</h1>\n");
      out.write("           <h3>");
      out.print( hostInfo );
      out.write(' ');
      out.write('-');
      out.write(' ');
      out.print( serverName );
      out.write("</h3>\n");
      out.write("         </td>\n");
      out.write("      </tr>\n");
      out.write("   </table>\n");
      out.write("<hr>\n");
      out.write("<form action=\"HtmlAdaptor?action=displayMBeans\" method=\"post\" name=\"applyFilter\" id=\"applyFilter\">\n");
      out.write("ObjectName Filter (e.g. \"jboss:*\", \"*:service=invoker,*\")  :<input type=\"text\" name=\"filter\" size=\"40\" value=\"");
      out.print( request.getAttribute("filter"));
      out.write("\" /><input type=\"submit\" name=\"apply\" value=\"ApplyFilter\">\n");

 	if (request.getAttribute("filterError") != null) {
		out.println("<br/><span class='error'>"+request.getAttribute("filterError")+"</span>");
 	}

      out.write("\n");
      out.write("</form>\n");
      out.write("<hr>\n");

   Iterator mbeans = (Iterator) request.getAttribute("mbeans");
   while( mbeans.hasNext() )
   {
      DomainData domainData = (DomainData) mbeans.next();

      out.write("\n");
      out.write("   <h2 class='DomainName'>");
      out.print( domainData.getDomainName() );
      out.write("</h2>\n");
      out.write("   <ul class='MBeanList'>\n");

      MBeanData[] data = domainData.getData();
      for(int d = 0; d < data.length; d ++)
      {
         String name = data[d].getObjectName().toString();
         String properties = data[d].getNameProperties();

      out.write("\n");
      out.write("      <li><a href=\"HtmlAdaptor?action=inspectMBean&name=");
      out.print( URLEncoder.encode(name) );
      out.write('"');
      out.write('>');
      out.print( URLDecoder.decode(properties) );
      out.write("</a></li>\n");

      }

      out.write("\n");
      out.write("   </ul>\n");

   }

      out.write("\n");
      out.write("</td></tr>\n");
      out.write("</table>\n");
      out.write("</body>\n");
      out.write("</html>\n");
    } catch (Throwable t) {
      if (!(t instanceof SkipPageException)){
        out = _jspx_out;
        if (out != null && out.getBufferSize() != 0)
          try { out.clearBuffer(); } catch (java.io.IOException e) {}
        if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
      }
    } finally {
      _jspxFactory.releasePageContext(_jspx_page_context);
    }
  }
}
