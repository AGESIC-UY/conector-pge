#!/bin/bash
clear
echo "--- CONNECTOR PGE ---"
echo "Elija un server para deployar la aplicación:"
echo "1- Tomcat 8"
echo "2- Wildfly 9"
read server

if [ -z $server ] ; then 
        server=1
fi

if [ $server -eq 1 ] ; then
	echo "##### TOMCAT 8"
	cd apache-tomcat-8.5.23/bin/
	./startup.sh
elif [ $server -eq 2 ] ; then
	echo "##### WILDFLY 9"
	cd wildfly-9.0.2.Final/bin
	./standalone.sh
fi

