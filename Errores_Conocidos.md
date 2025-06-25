## Lista de Issues y Soluciones del Conector

**1- El conector no se ejecuta si hay algún espacio en el nombre de alguna carpeta en la ruta, o si se ejecuta como administrador (En Windows).**

* **Ejemplo de error:** Ruta "C:\Users\user\Desktop\Conector PDI\Conector"
* **Solución:** No colocar espacios en blanco en la ruta del conector.

**2- Al ingresar un valor incorrecto en el campo "wsa:To" en el conector, el conector muestra un error genérico, y al consumir el servicio, se muestra una Url de producción: servicios.pge.red.uy. Al cambiar el campo "wsa:To" y consumir el servicio de timestamp, se muestra un mensaje de error y una Url de producción.**

* **Solución:** Ingresar correctamente el valor del campo "wsa:To".

**3- Al configurar conexión global con campos "Keystore Organismo", "Keystore SSL" o Trustore vacíos da error 500.**

* **Issue:** 38 y 39
* **Solución:** Recargar la página y subir los archivos faltantes en la configuración global.

**4- Al subir en la configuración global credenciales (Password) de Keystore Organismo, Keystore SSL, Trustore inválidas y guardar, el sistema guarda los cambios en vez de mostrar un mensaje de error. Esto ocurre con los campos "Alias del Keystore Organismo", "Password del Keystore Organismo" y "Password del Keystore SSL". El sistema no alerta de esto.**

* **Issue:** 37
* **Solución:** Verificar que las contraseñas ingresadas para los Keystores y Trustore sean correctas antes de guardar la configuración global.

**5- Error al publicar servicio usando HTTPS.**

* **Issue:** 36
* **Solución:** Con HTTPS no aplican los puertos 9800 y 9700. En testing para HTTPS se publica en el puerto 8443 y en producción en el puerto 8553.

**6- El conector no permite la importación de conectores que hayan sido exportados con configuración global.**

* **Issue:** 35

**7- El contenido del campo wsa:action depende del contenido que contiene el WSDL.**

* **Nota:** Este punto describe un comportamiento esperado del conector.

**8- Al ingresar algún campo mal, por ejemplo la URL, es común que el conector devuelva el mensaje "Internal Error".**

* **Issue:** 33
* **Solución:** Revisar los logs del conector para obtener más detalles sobre el error y verificar los valores ingresados en los campos del conector, especialmente las URLs.

**9- Al estar creando un conector para un servicio, y se ingresa una contraseña incorrecta, en el formulario se borran los datos, en particular los archivos subidos.**

* **Issue:** 29
* **Solución:** Salir del formulario de creación del conector y volver a entrar para evitar la pérdida de datos al ingresar una contraseña incorrecta.

**10- UI-Desbordamiento de tabla de operaciones y rol
Issue 41

**11- En la pantalla de edición del servicio, si se da click en descargar wsdl, lo abre en la misma pestaña y no se puede volver atrás
* **Issue:** 23
* **Solución:** Descargar el wdls desde la pantalla principal dónde se muestran todos los servicios configurados.

**12- Si el servicio se pulica con Https, si se le da desargar wsdl desde la tabla de servicio (página inicial), se muestra algo incorrecto.
* **Issue:** 22
* **Solución:** Configurar provisoriamente el servicio como http, descargar el wsdl y luego volver a configurarlo como https.
La url del wsdl aparece correctamente y se puede copiar y pegar en soapui para realizar la prueba.

**13- Al importar un servicio que requiere usuario y contraseña, pero no habilitar el checkbox, el mensaje de error no es claro
* **Issue:** 21
* **Solución:** Error en la descripción de error (Manejo de errores)


**14 -Al ingresar una contraseña incorrecta del certificado, el error está en idioma inglés
* **Issue:** 18
* **Solución:** Error en la descripción (idioma) del error (Manejo de errores)

**15 -Comportamiento filtro de búsqueda
* **Issue:** 17
* **Solución:** Comportamiento conocido.

**16 -No hay un flujo establecido para eliminar la configuración global una vez que se fijó.
* **Issue:** 16
* **Solución:** No es posible eliminar una configuración global. Si un servicio no quiere utilizarla puede utilizar la local.

**17 -Pantalla "ha ocurrido un error inesperado"
* **Issue:** 14
* **Solución:** No poner en el campo path caracteres especiales.

**18 -Carpetas al mismo nivel cuando se descomprimen las carpetas
* **Issue:** 11
* **Solución:** Revisar que las carpetas tomcat y jre se ubiquen en el mimso nivel

**19 -Al definir el tiempo de espera en milisegundos en 0 local o globalmente, el servicio responde correctamente
* **Issue:** 10
* **Solución:** El valor 0 indique sin limite de tiempo en la respuesta.

**20 -Al ingresar una contraseña incorrecta en algunos certificados el error del servicio no es claro
* **Issue:** 09
* **Solución:** (Manejo de errores)

**21 -Una vez configurado correctamente el conector con "Habilitar Configuración de Certificado Local", el sistema ya no verifica los datos ingresados
* **Issue:** 09
* **Solución:** El algunos casos el conector no chequea las pass y alias

**22 -No se muestra la fecha de vencimiento de algunos certificados
* **Issue:** 05
* **Solución:** La fecha de vencimiento mostrada es la correspondiente al alias, o en su defecto a primer certificado que contiene el keystore

**23 -Al configurar un servicio con "Habilitar Configuración de Certificado Local" el servicio muestra una Url de producción
* **Issue:** 04
* **Solución:** Es posible que la espuesta del servicio figure parte del mensaje "servicios.pge.red.uy" incluso en testing.

**24 -Al seleccionar "Habilitar Configuración de Certificado Local" el sistema no utiliza los certificados locales
* **Issue:** 03
* **Solución:** Cuando se selecciona la opción de certificados locales, debe especificarse de forma local el STS y tipo de TOKEN
