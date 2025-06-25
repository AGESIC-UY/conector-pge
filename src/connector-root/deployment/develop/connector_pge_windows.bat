ECHO OFF
cls
echo --- CONNECTOR PGE ---
echo Elija un server para deployar la aplicación:
echo 1- Tomcat 8 (Default)
echo 2- Wildfly 11
echo.
set /P server=Introduzca un valor:
echo.

IF "%server%"=="" (
        set server=1
        echo Server por defecto aplicado: Tomcat 8.
        echo.
)


echo Elija un nivel de Logging:
echo 1- DEBUG
echo 2- INFO (Default)
echo 3- WARN
echo.
set /P logLevel=Introduzca un valor:
echo.

IF "%logLevel%"=="" (
        set logLevel=2
        echo Nivel por defecto aplicado: INFO.
        echo.
)

IF "%logLevel%"=="1" (
        SET LOG_LEVEL="DEBUG"
)
IF "%logLevel%"=="2" (
        SET LOG_LEVEL="INFO"
)
IF "%logLevel%"=="3" (
        SET LOG_LEVEL="WARN"
)


IF /I "%server%" EQU "1" (
	echo ##### TOMCAT 8
	cd apache-tomcat-8.5.23\bin\
	.\startup.bat
) ELSE IF /I "%server%" EQU "2" (
	echo ##### WILDFLY 11
	cd wildfly-11.0.0.Final/bin
	.\standalone.bat
)

