package org.apache.jsp;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import java.net.*;
import java.util.*;
import javax.management.*;
import javax.management.modelmbean.*;
import org.jboss.jmx.adaptor.control.Server;
import org.jboss.jmx.adaptor.control.AttrResultInfo;
import org.jboss.jmx.adaptor.model.*;
import java.lang.reflect.Array;
import org.jboss.util.propertyeditor.PropertyEditors;

public final class inspectMBean_jsp extends org.apache.jasper.runtime.HttpJspBase
    implements org.apache.jasper.runtime.JspSourceDependent {


   public String fixDescription(String desc)
   {
      if (desc == null || desc.equals(""))
      {
        return "(no description)";
      }
      return desc;
   }
   public String quoteName(String name)
   {
      String sname = name.replace("\"", "&quot;");
      sname = name.replace("\'", "&apos;");
      return sname;
   }

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

      out.write('\n');
      out.write("\n");
      out.write("<html>\n");
      out.write("<head>\n");
      out.write("   <title>MBean Inspector</title>\n");
      out.write("   <link rel=\"stylesheet\" href=\"style_master.css\" type=\"text/css\">\n");
      out.write("   <meta http-equiv=\"cache-control\" content=\"no-cache\">\n");
      out.write("</head>\n");
      out.write("<body>\n");
      out.write("\n");
      org.jboss.jmx.adaptor.model.MBeanData mbeanData = null;
      synchronized (request) {
        mbeanData = (org.jboss.jmx.adaptor.model.MBeanData) _jspx_page_context.getAttribute("mbeanData", PageContext.REQUEST_SCOPE);
        if (mbeanData == null){
          mbeanData = new org.jboss.jmx.adaptor.model.MBeanData();
          _jspx_page_context.setAttribute("mbeanData", mbeanData, PageContext.REQUEST_SCOPE);
        }
      }
      out.write('\n');

   if(mbeanData.getObjectName() == null)
   {

      out.write('\n');
      if (true) {
        _jspx_page_context.forward("/HtmlAdaptor?action=displayMBeans");
        return;
      }
      out.write('\n');

   }
   ObjectName objectName = mbeanData.getObjectName();
   String objectNameString = mbeanData.getName();
   String quotedObjectNameString = quoteName(mbeanData.getName());
   MBeanInfo mbeanInfo = mbeanData.getMetaData();
   MBeanAttributeInfo[] attributeInfo = mbeanInfo.getAttributes();
   MBeanOperationInfo[] operationInfo = mbeanInfo.getOperations();

      out.write("\n");
      out.write("\n");
      out.write("<table width=\"100%\">\n");
      out.write("   <table>\n");
      out.write("      <tr>\n");
      out.write("         <td><img src=\"images/logo.gif\" align=\"left\" border=\"0\" alt=\"JBoss\"></td>\n");
      out.write("         <td valign=\"middle\"><h1>JMX MBean View</h1></td>\n");
      out.write("      <tr/>\n");
      out.write("   </table>\n");
      out.write("   <ul>\n");
      out.write("   <table>\n");
      out.write("      <tr>\n");
      out.write("         <td>MBean Name:</td>\n");
      out.write("         <td><b>Domain Name:</b></td>\n");
      out.write("         <td>");
      out.print( objectName.getDomain() );
      out.write("</td>\n");
      out.write("      </tr>\n");

   Hashtable properties = objectName.getKeyPropertyList();
   Iterator it = properties.keySet().iterator();
   while( it.hasNext() )
   {
      String key = (String) it.next();
      String value = (String) properties.get( key );

      out.write("\n");
      out.write("      <tr><td></td><td><b>");
      out.print( key );
      out.write(": </b></td><td>");
      out.print( value );
      out.write("</td></tr>\n");

   }

      out.write("\n");
      out.write("      <tr><td>MBean Java Class:</td><td colspan=\"3\">");
      out.write(org.apache.jasper.runtime.JspRuntimeLibrary.toString((((org.jboss.jmx.adaptor.model.MBeanData)_jspx_page_context.findAttribute("mbeanData")).getClassName())));
      out.write("</td></tr>\n");
      out.write("   </table>\n");
      out.write("</ul>\n");
      out.write("<table cellpadding=\"5\">\n");
      out.write("   <tr>\n");
      out.write("      <td><a href='HtmlAdaptor?action=displayMBeans'>Back to Agent View</a></td>\n");
      out.write("\t  <td>\n");
      out.write("      <td><a href='HtmlAdaptor?action=inspectMBean&name=");
      out.print( URLEncoder.encode(request.getParameter("name")) );
      out.write("'>Refresh MBean View</a></td>\n");
      out.write("   </tr>\n");
      out.write("</table>\n");
      out.write("\n");
      out.write("<hr>\n");
      out.write("<h3>MBean description:</h3>\n");
      out.print( fixDescription(mbeanInfo.getDescription()));
      out.write("\n");
      out.write("\n");
      out.write("<hr>\n");
      out.write("<h3>List of MBean attributes:</h3>\n");
      out.write("\n");
      out.write("<form method=\"post\" action=\"HtmlAdaptor\">\n");
      out.write("   <input type=\"hidden\" name=\"action\" value=\"updateAttributes\">\n");
      out.write("   <input type=\"hidden\" name=\"name\" value='");
      out.print( quotedObjectNameString );
      out.write("'>\n");
      out.write("\t<table cellspacing=\"1\" cellpadding=\"1\" border=\"1\">\n");
      out.write("\t\t<tr class=\"AttributesHeader\">\n");
      out.write("\t\t    <th>Name</th>\n");
      out.write("\t\t    <th>Type</th>\n");
      out.write("\t\t    <th>Access</th>\n");
      out.write("\t\t    <th>Value</th>\n");
      out.write("\t\t    <th>Description</th>\n");
      out.write("\t\t</tr>\n");

   boolean hasWriteable = false;
   for(int a = 0; a < attributeInfo.length; a ++)
   {
      MBeanAttributeInfo attrInfo = attributeInfo[a];
      String attrName = attrInfo.getName();
      String attrType = attrInfo.getType();
      AttrResultInfo attrResult = Server.getMBeanAttributeResultInfo(objectNameString, attrInfo);
      String attrValue = attrResult.getAsText();
      String access = "";
      if( attrInfo.isReadable() )
         access += "R";
      if( attrInfo.isWritable() )
      {
         access += "W";
         hasWriteable = true;
      }
      String attrDescription = fixDescription(attrInfo.getDescription());

      out.write("\n");
      out.write("\t\t<tr>\n");
      out.write("\t\t    <td>");
      out.print( attrName );
      out.write("</td>\n");
      out.write("\t\t    <td>");
      out.print( attrType );
      out.write("</td>\n");
      out.write("\t\t    <td>");
      out.print( access );
      out.write("</td>\n");
      out.write("          <td>\n");

      if( attrInfo.isWritable() )
      {
         String readonly = attrResult.editor == null ? "readonly" : "";
         if( attrType.equals("boolean") || attrType.equals("java.lang.Boolean") )
         {
            // Boolean true/false radio boxes
            Boolean value = attrValue == null || "".equals( attrValue ) ? null : Boolean.valueOf(attrValue);
            String trueChecked = (value == Boolean.TRUE ? "checked" : "");
            String falseChecked = (value == Boolean.FALSE ? "checked" : "");
            String naChecked = value == null ? "checked" : "";

      out.write("\n");
      out.write("            <input type=\"radio\" name=\"");
      out.print( attrName );
      out.write("\" value=\"True\" ");
      out.print(trueChecked);
      out.write(">True\n");
      out.write("            <input type=\"radio\" name=\"");
      out.print( attrName );
      out.write("\" value=\"False\" ");
      out.print(falseChecked);
      out.write(">False\n");

            // For wrappers, enable a 'null' selection
            if ( attrType.equals( "java.lang.Boolean" ) && PropertyEditors.isNullHandlingEnabled() )
            {

      out.write("\n");
      out.write("            <input type=\"radio\" name=\"");
      out.print( attrName );
      out.write("\" value=\"\" ");
      out.print(naChecked);
      out.write(">Null\n");

            }
         }
         else if( attrInfo.isReadable() )
         {  // Text fields for read-write string values
            String avalue = (attrValue != null ? attrValue : "");
            if( attrType.equals("javax.management.ObjectName") )
               avalue = quoteName(avalue);

      out.write("\n");
      out.write("          <input type=\"text\" name=\"");
      out.print( attrName );
      out.write("\" value='");
      out.print( avalue );
      out.write('\'');
      out.write(' ');
      out.print( readonly );
      out.write('>');
      out.write('\n');
      out.write('\n');

         }
         else
         {  // Empty text fields for write-only

      out.write("\n");
      out.write("\t\t    <input type=\"text\" name=\"");
      out.print( attrName );
      out.write('"');
      out.write(' ');
      out.print( readonly );
      out.write('>');
      out.write('\n');

         }
      }
      else
      {
         if( attrType.equals("[Ljavax.management.ObjectName;") )
         {
            // Array of Object Names
            ObjectName[] names = (ObjectName[]) Server.getMBeanAttributeObject(objectNameString, attrName);
            if( names != null )
            {

      out.write("\n");
      out.write("                  <table>\n");

               for( int i = 0; i < names.length; i++ )
               {

      out.write("\n");
      out.write("                  <tr><td>\n");
      out.write("                  <a href=\"HtmlAdaptor?action=inspectMBean&name=");
      out.print( URLEncoder.encode(( names[ i ] + "" )) );
      out.write('"');
      out.write('>');
      out.print( ( names[ i ] + "" ) );
      out.write("</a>\n");
      out.write("                  </td></tr>\n");

               }

      out.write("\n");
      out.write("                  </table>\n");

            }
         }
         // Array of some objects
         else if( attrType.endsWith("[]") || attrType.startsWith("[L") )
         {
            Object arrayObject = Server.getMBeanAttributeObject(objectNameString, attrName);
            if (arrayObject != null)
            {

      out.write("\n");
      out.write("                  <table>\n");

               for (int i = 0; i < Array.getLength(arrayObject); ++i)
               {

      out.write("\n");
      out.write("                  <tr><td>");
      out.print(Array.get(arrayObject,i));
      out.write("</td></tr>\n");

               }

      out.write("\n");
      out.write("                  </table>\n");

            }
         }
         else
         {
            // Just the value string

      out.write("\n");
      out.write("\t\t    ");
      out.print( attrValue );
      out.write('\n');

         }
      }
      if( attrType.equals("javax.management.ObjectName") )
      {
         // Add a link to the mbean
         if( attrValue != null )
         {

      out.write("\n");
      out.write("         <a href=\"HtmlAdaptor?action=inspectMBean&name=");
      out.print( URLEncoder.encode(attrValue) );
      out.write("\">View MBean</a>\n");

         }
      }

      out.write("\n");
      out.write("         </td>\n");
      out.write("         <td>");
      out.print( attrDescription);
      out.write("</td>\n");
      out.write("\t\t</tr>\n");

   }

      out.write("\n");
      out.write("\t</table>\n");
 if( hasWriteable )
   {

      out.write("\n");
      out.write("\t<input type=\"submit\" value=\"Apply Changes\">\n");

   }

      out.write("\n");
      out.write("</form>\n");
      out.write("\n");
      out.write("<hr>\n");
      out.write("<h3>List of MBean operations:</h3>\n");

   for(int a = 0; a < operationInfo.length; a ++)
   {
      MBeanOperationInfo opInfo = operationInfo[a];
      boolean accept = true;
      if (opInfo instanceof ModelMBeanOperationInfo)
      {
         Descriptor desc = ((ModelMBeanOperationInfo)opInfo).getDescriptor();
         String role = (String)desc.getFieldValue("role");
         if ("getter".equals(role) || "setter".equals(role))
         {
            accept = false;
         }
      }
      if (accept)
      {
         MBeanParameterInfo[] sig = opInfo.getSignature();

      out.write("\n");
      out.write("<form method=\"post\" action=\"HtmlAdaptor\">\n");
      out.write("   <input type=\"hidden\" name=\"action\" value=\"invokeOp\">\n");
      out.write("   <input type=\"hidden\" name=\"name\" value='");
      out.print( quotedObjectNameString );
      out.write("'>\n");
      out.write("   <input type=\"hidden\" name=\"methodIndex\" value=\"");
      out.print( a );
      out.write("\">\n");
      out.write("   <hr align='left' width='80'>\n");
      out.write("   <h4>");
      out.print( opInfo.getReturnType() + " " + opInfo.getName() + "()" );
      out.write("</h4>\n");
      out.write("   <p>");
      out.print( fixDescription(opInfo.getDescription()));
      out.write("</p>\n");

         if( sig.length > 0 )
         {

      out.write("\n");
      out.write("\t<table cellspacing=\"2\" cellpadding=\"2\" border=\"1\">\n");
      out.write("\t\t<tr class=\"OperationHeader\">\n");
      out.write("\t\t\t<th>Param</th>\n");
      out.write("\t\t\t<th>ParamType</th>\n");
      out.write("\t\t\t<th>ParamValue</th>\n");
      out.write("\t\t\t<th>ParamDescription</th>\n");
      out.write("\t\t</tr>\n");

            for(int p = 0; p < sig.length; p ++)
            {
               MBeanParameterInfo paramInfo = sig[p];
               String pname = paramInfo.getName();
               String ptype = paramInfo.getType();
               if( pname == null || pname.length() == 0 || pname.equals(ptype) )
               {
                  pname = "arg"+p;
               }

      out.write("\n");
      out.write("\t\t<tr>\n");
      out.write("\t\t\t<td>");
      out.print( pname );
      out.write("</td>\n");
      out.write("\t\t   <td>");
      out.print( ptype );
      out.write("</td>\n");
      out.write("         <td> \n");

                if( ptype.equals("boolean") || ptype.equals("java.lang.Boolean") )
                {
                   // Boolean true/false radio boxes

      out.write("\n");
      out.write("            <input type=\"radio\" name=\"arg");
      out.print( p);
      out.write("\" value=\"True\"checked>True\n");
      out.write("            <input type=\"radio\" name=\"arg");
      out.print( p);
      out.write("\" value=\"False\">False\n");

                 }
                 else
                 {

      out.write("\n");
      out.write("            <input type=\"text\" name=\"arg");
      out.print( p);
      out.write('"');
      out.write('>');
      out.write('\n');

                  }

      out.write("\n");
      out.write("         </td>\n");
      out.write("         <td>");
      out.print( fixDescription(paramInfo.getDescription()));
      out.write("</td>\n");
      out.write("\t\t</tr>\n");

               }

      out.write("\n");
      out.write("\t</table>\n");

         }

      out.write("\n");
      out.write("\t<input type=\"submit\" value=\"Invoke\">\n");
      out.write("</form>\n");

      }
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
