ECHO OFF
cls
echo --- CONNECTOR PGE ---
echo Elija un server para deployar la aplicación:
echo 1- Tomcat 8
echo 2- Wildfly 9
set /P server=Introduzca un valor:

IF "%server%"=="" (
        set server=1
)

IF /I "%server%" EQU "1" (
	echo ##### TOMCAT 8
	cd apache-tomcat-8.5.23\bin\
	.\startup.bat
) ELSE IF /I "%server%" EQU "2" (
	echo ##### WILDFLY 9
	cd wildfly-9.0.2.Final\bin
	.\standalone.bat
)

