#!/bin/bash
clear
echo "--- CONNECTOR PGE ---"
echo "Elija un server para deployar la aplicación:"
echo "1- Tomcat 8 (Default)"
echo "2- Wildfly 11"
echo
read server
echo

if [ -z $server ] ; then
        server=1
        echo "Server por defecto aplicado: Tomcat 8."
        echo
fi

echo "Elija un nivel de Logging:"
echo "1- DEBUG"
echo "2- INFO (Default)"
echo "3- WARN"
echo

read logLevel
echo

if [ -z $logLevel ] ; then
        logLevel=2
        echo "Nivel por defecto aplicado: INFO."
        echo
fi

case $logLevel in
    1)
      export LOG_LEVEL="DEBUG"
    ;;
    2)
      export LOG_LEVEL="INFO"
    ;;
    3)
      export LOG_LEVEL="WARN"
    ;;
esac

if [ $server -eq 1 ] ; then
	echo "##### TOMCAT 8"
	cd apache-tomcat-8.5.23/bin/
  chmod -R 777 startup.sh
  chmod -R 777 catalina.sh
  chmod -R 777 setenv.sh
  chmod -R 777 shutdown.sh
  sed -i -e 's/\r$//' startup.sh
  sed -i -e 's/\r$//' catalina.sh
  sed -i -e 's/\r$//' setenv.sh
  sed -i -e 's/\r$//' shutdown.sh

	./startup.sh
elif [ $server -eq 2 ] ; then
	echo "##### WILDFLY 11"
	cd wildfly-11.0.0.Final/bin
	./standalone.sh
fi

