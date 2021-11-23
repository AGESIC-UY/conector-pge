FROM centos:latest
LABEL Author="Ivan Viamonte"
USER root

##Actualizo el sistema
RUN dnf update -y && dnf upgrade -y

##Copio los directorios tomcat y jre
COPY apache-tomcat-8.5.23 /opt/apache-tomcat-8.5.23
COPY jre-13.0.2 /opt/jre-13.0.2

##Configuro permisos y ejecuto modificaciones
RUN cd /opt/jre-13.0.2 && chmod -R 777 bin/*
RUN cd /opt/apache-tomcat-8.5.23/bin && chmod -R 777 startup.sh
RUN cd /opt/apache-tomcat-8.5.23/bin && chmod -R 777 catalina.sh
RUN cd /opt/apache-tomcat-8.5.23/bin && chmod -R 777 setenv.sh
RUN cd /opt/apache-tomcat-8.5.23/bin && chmod -R 777 shutdown.sh
RUN cd /opt/apache-tomcat-8.5.23/bin && sed -i -e 's/\r$//' startup.sh
RUN cd /opt/apache-tomcat-8.5.23/bin && sed -i -e 's/\r$//' catalina.sh
RUN cd /opt/apache-tomcat-8.5.23/bin && sed -i -e 's/\r$//' setenv.sh

##Exponer puertos de servicios
EXPOSE 9800 9700

## Iniciar tomcat
CMD cd /opt/apache-tomcat-8.5.23/bin/ && sh startup.sh && tail -f /opt/apache-tomcat-8.5.23/logs/catalina.out
