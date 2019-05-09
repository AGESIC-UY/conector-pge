<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:set var="servletPath"
       value="${pageContext.request.servletPath}"/> <!-- /WEB-INF/views/jsp/index.jsp -->
<c:set value="${fn:split(servletPath,'/')}" var="stringAux"/>
<c:set value="${stringAux[fn:length(stringAux)-1]}" var="viewName"/>

<c:choose>
    <c:when test="${connector.id != null}">
        <c:set var="esAlta" value="false"/>
    </c:when>
    <c:otherwise>
        <c:set var="esAlta" value="true"/>
    </c:otherwise>
</c:choose>

<c:set value='
<h4>
    Descripci&oacute;n de los datos
</h4>

<p>
    A continuaci&oacute;n se detalla el significado de cada dato y el impacto de estos hacia el sistema.
</p>' var="fieldsDescription"/>
<c:set value='
                        <p>
                            Nombre: Nombre del Servicio. Se usa como identificaci&oacute;n interna del servicio y junto con el
                            Tipo deben ser &uacute;nicos.
                        </p>
                        <p>
                            Tipo : El tipo de servicio identifica si este es un servicio de Test o de Producci&oacute;n.
                        </p>
                        <p>
                            Descripci&oacute;n : Informaci&oacute;n descriptiva del servicio sin uso operativo.
                        </p>
                        <p>
                            Path : Es el path relativo en donde queda publicado el servicio. Por ejemplo, si el path es
                            /censo/CensoINEService entonces si el tipo es "Test" el servicio queda publicado en
                            http://server_host:9800/censo/CensoINEService y si el tipo es "Producci&oacute;n" el servicio queda
                            publicado en http://server_host:9700/censo/CensoINEService
                        </p>
                        <p>
                            Url : La Url que especifica el endpoint del servicio a invocar. Se obtiene del endpoint especificado
                            en el WSDL (pero puede ser modificada).
                        </p>
                        <p>
                            wsa:To : Este dato se agrega como Header del mensaje SOAP. Es un endpoint l&oacute;gico e indica cual
                            es el Web Service que se quiere invocar a trav&eacute;s de la plataforma.
                        </p>
                        <p>
                            Username : Usuario dentro del organismo que desea acceder al servicio final, este dato es utilizado
                            para la firma del mensaje SOAP.
                        </p>
                        <p>
                            Organismo : Nombre que representa al organismo consumidor, este dato es utilizado tambi&eacute;n para
                            la firma del mensaje SOAP
                        </p>
                        <p>
                            Tipo de Token : Dato utilizado para la firma del SOAP.
                        </p>
                        <p>
                            Ingresar credenciales de Username Token : Habilita el ingreso de usuario y contraseña para Username
                            Token.
                        </p>
                        <p>
                            Habilitar Configuraci&oacute;n Local : Permite al usuario definir los keystores y truststores que
                            utiliza el conector. En caso de no seleccionar este control, se utiliza los certificados globales.
                            </p>
                        <p>
                            Habilitar cache de Tokens : Permite cachear los tokens SAML pedidos a la plataforma, de manera de no
                            volver a pedir tokens mientras se tenga uno v&aacute;lido.Esto se puede habilitar solo cuando la hora
                            este sincronizada con el servidor NTP.
                        </p>
                        <p>
                            Alias del Keystore Organismo (Config Local) : Alias en donde se encuentra el certificado que se quiere
                            utilizar para la firma del SOAP. Este alias corresponde al "Keystore Organismo" (En el caso que sea un
                            conector con configuraci&oacute;n local habilitada)
                        </p>
                        <p>
                            Password Keystore Organismo (Config Local) : Password del "Keystore Organismo" (En el caso que sea un
                            conector con configuraci&oacute;n local habilitada)
                        </p>
                        <p>
                            Password Keystore SSL (Config Local) : Password utilizada para acceder al certificado del keystore
                            para el acceso por https. (En el caso que sea un conector con configuraci&oacute;n local habilitada)
                        </p>
                        <p>
                            Password Truststore (Config Local) : Password utilizada para acceder al certificado del trustore para
                            el acceso por https. (En el caso que sea un conector con configuraci&oacute;n local habilitada)
                        </p>
                        <p>
                            Keystore Organismo (Config Local) : Keystore del Organismo utilizado para la firma del SOAP (En el
                            caso que sea un conector con configuraci&oacute;n local habilitada)
                        </p>
                        <p>
                            Keystore SSL (Config Local) : Keystore utilizado para la comunicaci&oacute;n HTTPS (En el caso que sea
                            un conector con configuraci&oacute;n local habilitada)
                        </p>
                        <p>
                            Truststore SSL (Config Local) : Truststore utilizado para la comunicaci&oacute;n HTTPS (En el caso que
                            sea un conector con configuraci&oacute;n local habilitada)
                        </p>
                        <p>
                            WSDL : Archivo WSDL en donde est&aacute; especificado el endpoint al cual se va a invocar al servicio
                            y las operaciones que se pueden invocar. Este campo acepta archivos wsdl, as&iacute; como tambi&eacute;n
                            archivos zip que contengan el wsdl junto a sus dependencias (otros archivos wsdl o xsd).
                        </p>
                        <p>
                            Tag : Atributo para filtrar el conector
                        </p>
'
       var="addOrModifyFields"/>
<!-- Modal -->
<div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="ayuda">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="ayuda">Ayuda</h4>
            </div>
            <div class="modal-body">
                <c:choose>
                    <c:when test="${viewName == 'index.jsp'}">
                        <h3>Inicio</h3>

                        <p>
                            En la p&aacute;gina de inicio se muestran los conectores creados en el sistema.
                        </p>

                        <p>
                            Esta p&aacute;gina tiene dos filtros, uno por tipo de conector para poder visualizar los
                            conectores definidos para "Test" o "Producci&oacute;n" y el otro por Tag, este atributo se le
                            asigna a cada conector con el fin de poder filtrar y
                            encontrar de manera r&aacute;pida los conectores relacionados con el mismo Tag.
                        </p>

                        <p>
                            En cada conector se permiten varias acciones (Ver, Editar, Borrar, obtener el WSDL y
                            obtener el xml de exportaci&oacute;n que luego puede ser importado por otro organismo).
                        </p>

                        <p>
                            Tambi&eacute;n hay un &aacute;rea de importaci&oacute;n en donde se debe seleccionar un archivo XML
                            que fue
                            generado por la misma aplicaci&oacute;n mediante la exportaci&oacute;n de un conector. La importaci&oacute;n
                            lee este archivo y crea un nuevo
                            conector con todos los datos del conector. Vale destacar que una vez importado el
                            conector, se debe editar el mismo para cargar las contrase&ntilde;as de los keystores y
                            truststores, en el caso de estar utilizando
                            configuraci&oacute;n local. Esto es necesario ya que las contrase&ntilde;as no forman parte de la
                            exportaci&oacute;n, por razones de seguridad.
                        </p>
                    </c:when>
                    <c:when test="${viewName == 'viewConnector.jsp'}">

                        <h3>Ver un conector</h3>

                        <p>
                            Se muestra los datos del Conector, permitiendo bajar los archivos y obtener el XML para la exportaci&oacute;n.
                        </p>

                        <p>
                            Al exportar un conector, vale destacar que en caso de utilizar la configuraci&oacute;n local, las
                            contraseñas de los keystores y truststores no son incluidas en la exportaci&oacute;n, por razones de
                            seguridad.
                        </p>

                        ${fieldsDescription}

                        <p>
                                ${addOrModifyFields}
                            Bot&oacute;n para descargar el archivo que se encuentra guardado (o sea, el que se guard&oacute; al
                            utilizar el bot&oacute;n de Salvar).
                        </p>

                    </c:when>

                    <c:when test="${esAlta == true && (viewName == 'edit.jsp' || viewName == 'edit2.jsp')}">
                        <h3>Nuevo Conector</h3>

                        <p>
                            Todos los datos del conector son requeridos.
                            En el ingreso de datos si no se ingresa uno de los campos requeridos al momento de apretar el bot&oacute;n
                            salvar, se volver&aacute; a mostrar la misma p&aacute;gina indicando cual es el campo en el cual no se
                            ingresaron
                            datos. Si se da este caso y se hab&iacute;a seleccionado un archivo, para cualquiera de los campos
                            Wsdl,
                            Keystore Organismo, Keystore SSL o Trustore SSL , este se perder&aacute; y se tendr&aacute; que volver
                            a seleccionar
                            el o los archivos.
                        </p>

                        <p>
                            La combinaci&oacute;n "Nombre" - "Tipo" tiene que ser &uacute;nica en el sistema, as&iacute; como
                            tambi&eacute;n la combinaci&oacute;n
                            "Path" - "Tipo".
                        </p>

                        ${fieldsDescription}

                        <p>
                                ${addOrModifyFields}
                            Al lado del campo de entrada para seleccionar el archivo WSDL, hay un bot&oacute;n de "refresh". Ese
                            hace que se cargue el archivo WSDL y se actualicen las operaciones del conector con las operaciones
                            especificadas en dicho WSDL. Dicho archivo no es guardado hasta que no se guarden los cambios.
                        </p>

                        <p>
                            Bot&oacute;n que permite cancelar la subida de un archivo, de manera de poder seleccionar otro.
                        </p>
                        <p>
                            Bot&oacute;n que permite cargar el archivo WSDL y actualiza las operaciones del conector con las
                            operaciones
                            especificadas en dicho WSDL. Notar que dicho archivo no es guardado hasta que no se guarden los
                            cambios.
                        </p>

                    </c:when>

                    <c:when test="${esAlta != true && (viewName == 'edit.jsp' || viewName == 'edit2.jsp')}">

                        <h3>Edici&oacute;n</h3>

                        <p>
                            En la edici&oacute;n se muestran los datos del conector, permitiendo su modificaci&oacute;n (a excepci&oacute;n
                            del "Nombre y "Tipo" que no pueden ser modificados luego de creado el servicio). Adem&aacute;s se
                            permite sobrescribir un archivo (ya sea un keystore, truststore, wsdl) al seleccionar uno nuevo.
                        </p>

                        <p>
                            La pantalla tambi&eacute;n permite borrar el conector (utilizando el bot&oacute;n de Borrar) y pasar
                            el conector a producci&oacute;n (bot&oacute;n Pasar a Producci&oacute;n). Esta &uacute;ltima
                            funcionalidad se encuentra habilitada para los conectores de tipo Test, y lo que hace es crear un
                            conector en Producci&oacute;n copiando todos los datos del conector de Test.
                        </p>

                        <p>
                            Los conectores de Producci&oacute;n no tienen habilitado el bot&oacute;n de Pasar a Producci&oacute;n,
                            pero en caso que hayan sido creados a partir de un conector de Testeo, se muestra el bot&oacute;n de
                            Test Asociado, de manera de poder navegar desde el conector de Producci&oacute;n a Testeo.
                        </p>

                        ${fieldsDescription}

                        <p>
                                ${addOrModifyFields}
                        <p>
                            Bot&oacute;n para descargar el archivo que se encuentra guardado (o sea, el que se guard&oacute; al
                            utilizar el bot&oacute;n de Salvar).
                        </p>
                        <p>
                            Bot&oacute;n que permite cancelar la subida de un archivo, de manera de poder seleccionar otro.
                        </p>
                        <p>
                            Bot&oacute;n para validar el Keystore que se encuentra guardado. Notar que el bot&oacute;n solo
                            aparece una vez que se haya salvado el conector con un archivo Keystore.
                        </p>
                        <p>
                            Bot&oacute;n para ver informaci&oacute;n de el Keystore/Trustore que se encuentra guardado. Notar que
                            el bot&oacute;n solo aparece una vez que se haya salvado el conector con alg&uacute;n Keystore o
                            Truststore.
                        </p>
                        <p>
                            Bot&oacute;n que permite cargar el archivo WSDL y actualiza las operaciones del conector con las
                            operaciones especificadas en dicho WSDL. Notar que dicho archivo no es guardado hasta que no se
                            guarden los cambios.
                        </p>

                    </c:when>
                    <c:when test="${viewName == 'globalConfiguration.jsp'}">

                        <h3>Configuraci&oacute;n Global</h3>

                        <p>
                            Permite definir la configuraci&oacute;n de keystores y truststores que utilizan los conectores que
                            tienen habilitada la opci&oacute;n de Configuraci&oacute;n Global.
                        </p>

                        ${fieldsDescription}

                        <p>
                            Alias del Keystore Organismo : Alias en donde se encuentra el certificado que se quiere utilizar para
                            la firma del SOAP. Este alias corresponde al "Keystore Organismo"
                        </p>
                        <p>
                            Password Keystore Organismo : Password del "Keystore Organismo"
                        </p>
                        <p>
                            Password Keystore SSL : Password utilizada para acceder al certificado del keystore para el acceso por
                            https.
                        </p>
                        <p>
                            Password Truststore : Password utilizada para acceder al certificado del trustore para el acceso por
                            https.
                        </p>
                        <p>
                            Keystore Organismo : Keystore del Organismo utilizado para la firma del SOAP
                        </p>
                        <p>
                            Keystore SSL : Keystore utilizado para la comunicaci&oacute;n HTTPS
                        </p>
                        <p>
                            Truststore SSL : Truststore utilizado para la comunicaci&oacute;n HTTPS
                        </p>
                        <p>
                            Bot&oacute;n para descargar el archivo que se encuentra guardado (o sea, el que se guard&oacute; al
                            utilizar el bot&oacute;n de Salvar).
                        </p>
                        <p>
                            Bot&oacute;n que permite cancelar la subida de un archivo, de manera de poder seleccionar otro.
                        </p>
                        <p>
                            Bot&oacute;n para validar el Keystore que se encuentra guardado. Notar que el bot&oacute;n solo
                            aparece una vez que se haya salvado la config. global con un archivo Keystore.
                        </p>
                        <p>
                            Bot&oacute;n para ver informaci&oacute;n de el Keystore/Trustore que se encuentra guardado. Notar que
                            el bot&oacute;n solo aparece una vez que se haya salvado la config. global con alg&uacute;n Keystore o
                            Truststore.
                        </p>

                    </c:when>
                    <c:otherwise>

                        <h2>No hay ayuda contextual para esta p&aacute;gina</h2>

                        <p>
                            Esta p&aacute;gina no tiene definida ayuda contextual.
                        </p>

                    </c:otherwise>
                </c:choose>

            </div>
        </div>
    </div>
</div>
