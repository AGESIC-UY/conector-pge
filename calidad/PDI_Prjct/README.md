# Pruebas de performance para el Conector y el Ruteo de PDI

Framework desarrollado por Abstracta para realizar pruebas de performance a los componentes Conector y Ruteo de la PDI utilizando [Gatling 3.3.0](https://gatling.io/) y [Maven](https://maven.apache.org/) para el manejo de dependencias.

## Ejecución

### Opción 1 - Desde IntelliJ
1. Abrir el proyecto con IntelliJ.
2. Ejecutar el archivo Engine.scala.
3. Seleccionar una prueba.

### Opción 2 - Desde Gatling
1. Descargar Gatling https://gatling.io/open-source
2. Mover los archivos .scala a user-files/simulations
3. Mover los archivos de configuración (config.properties, gatling.conf, logback.xml, recorder.conf) a la carpeta conf.
4. Mover los archivos .txt y .csv a la carpeta user-files/resources
5. Ejecutar gatling.bat o gatling.sh.
6. Seleccionar una prueba.